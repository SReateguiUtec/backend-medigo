package com.example.medigo.controller;

import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.CreateCheckoutSessionRequest;
import com.example.medigo.dto.response.CheckoutSessionResponse;
import com.example.medigo.dto.response.PaymentStatusResponse;
import com.example.medigo.service.StripePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final StripePaymentService stripePaymentService;

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
}
