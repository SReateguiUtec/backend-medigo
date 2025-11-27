package com.example.medigo.controller;

import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.CreateCheckoutSessionRequest;
import com.example.medigo.dto.response.CheckoutSessionResponse;
import com.example.medigo.dto.response.PaymentStatusResponse;
import com.example.medigo.service.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/checkout/session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();
        Long pacienteId = usuario.getId();

        log.info("Creando sesión de checkout para paciente: {} y cita: {}", pacienteId, request.getCitaId());

        CheckoutSessionResponse response = stripePaymentService.createCheckoutSession(request, pacienteId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkout/status/{sessionId}")
    public ResponseEntity<PaymentStatusResponse> getCheckoutStatus(@PathVariable String sessionId) {
        PaymentStatusResponse response = stripePaymentService.getPaymentStatus(sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("=== WEBHOOK RECIBIDO ===");
        log.info("Webhook recibido de Stripe");

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            System.out.println("Evento verificado: " + event.getType());
        } catch (SignatureVerificationException e) {
            System.err.println("ERROR: Firma inválida - " + e.getMessage());
            log.error("Firma de webhook inválida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            System.err.println("ERROR: Error al verificar webhook - " + e.getMessage());
            e.printStackTrace();
            log.error("Error al procesar webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        try {
            System.out.println("Procesando evento tipo: " + event.getType());
            
            switch (event.getType()) {
                case "checkout.session.completed":
                    System.out.println("=== PROCESANDO PAGO EXITOSO ===");
                    
                    // Obtener el objeto de datos del evento
                    StripeObject stripeObject = event.getData().getObject();
                    System.out.println("Tipo de objeto: " + stripeObject.getClass().getName());
                    
                    // Convertir a Session
                    Session session = (Session) stripeObject;
                    
                    String sessionId = session.getId();
                    System.out.println("Session ID: " + sessionId);
                    log.info("Procesando pago exitoso para sesión: {}", sessionId);
                    
                    stripePaymentService.processSuccessfulPayment(sessionId);
                    System.out.println("=== PAGO PROCESADO EXITOSAMENTE ===");
                    break;

                case "checkout.session.expired":
                    System.out.println("=== SESIÓN EXPIRADA ===");
                    
                    StripeObject expiredObject = event.getData().getObject();
                    Session expiredSession = (Session) expiredObject;
                    
                    System.out.println("Session expirada ID: " + expiredSession.getId());
                    log.info("Sesión expirada: {}", expiredSession.getId());
                    stripePaymentService.handleExpiredSession(expiredSession.getId());
                    break;

                default:
                    System.out.println("Evento no manejado: " + event.getType());
                    log.info("Evento no manejado: {}", event.getType());
            }

            System.out.println("=== WEBHOOK PROCESADO OK ===");
            return ResponseEntity.ok("Webhook procesado");

        } catch (Exception e) {
            System.err.println("=== ERROR AL PROCESAR EVENTO ===");
            System.err.println("Tipo de error: " + e.getClass().getName());
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            log.error("Error al procesar evento de webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando webhook: " + e.getMessage());
        }
    }
}
