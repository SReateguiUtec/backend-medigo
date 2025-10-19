package com.example.medigo.service;

import com.example.medigo.domain.*;
import com.example.medigo.dto.request.CreateCheckoutSessionRequest;
import com.example.medigo.dto.response.CheckoutSessionResponse;
import com.example.medigo.dto.response.PaymentStatusResponse;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.PaymentTransactionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CitaService citaService;

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.platform.commission:0.05}")
    private BigDecimal platformCommission;

    private static final String CURRENCY = "pen";

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request, Long pacienteId) {
        try {
            Stripe.apiKey = stripeSecretKey;

            Cita cita = citaService.findCitaById(request.getCitaId());

            if (!cita.getPaciente().getId().equals(pacienteId)) {
                throw new IllegalArgumentException("Esta cita no pertenece al paciente actual");
            }
            if (cita.getEstado() != EstadoCita.PENDIENTE) {
                throw new IllegalArgumentException("Esta cita ya fue procesada");
            }
            if (Boolean.TRUE.equals(cita.getEsPagada())) {
                throw new IllegalArgumentException("Esta cita ya fue pagada");
            }
            BigDecimal precioConsulta = cita.getMedico().getPrecioConsulta();
            if (precioConsulta == null || precioConsulta.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El médico no tiene un precio de consulta configurado");
            }

            // Calcular montos
            BigDecimal comision = precioConsulta.multiply(platformCommission).setScale(2, RoundingMode.HALF_UP);
            BigDecimal montoTotal = precioConsulta.add(comision).setScale(2, RoundingMode.HALF_UP);
            BigDecimal montoMedico = precioConsulta.setScale(2, RoundingMode.HALF_UP);

            // Convertir a centavos para Stripe
            Long amountInCents = montoTotal.multiply(BigDecimal.valueOf(100)).longValue();

            // Crear URLs de éxito y cancelación
            String successUrl = request.getOriginUrl() + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = request.getOriginUrl() + "/payment/cancel";

            Map<String, String> metadata = new HashMap<>();
            metadata.put("cita_id", String.valueOf(cita.getId()));
            metadata.put("paciente_id", String.valueOf(pacienteId));
            metadata.put("medico_id", String.valueOf(cita.getMedico().getId()));
            metadata.put("precio_consulta", precioConsulta.toString());
            metadata.put("comision_plataforma", comision.toString());
            metadata.put("monto_medico", montoMedico.toString());

            if (request.getMetadata() != null) {
                metadata.putAll(request.getMetadata());
            }

            // Crear sesión de checkout en Stripe
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCurrency(CURRENCY)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(CURRENCY)
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Consulta Médica - " + cita.getMedico().getNombres() + " " + cita.getMedico().getApellidos())
                                                                    .setDescription("Consulta médica programada para " + cita.getFechaHora())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putAllMetadata(metadata)
                    .build();

            Session session = Session.create(params);

            // Guardar transacción pendiente
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .stripeSessionId(session.getId())
                    .cita(cita)
                    .paciente(cita.getPaciente())
                    .medico(cita.getMedico())
                    .amountTotal(montoTotal)
                    .platformCommission(comision)
                    .medicoAmount(montoMedico)
                    .currency(CURRENCY.toUpperCase())
                    .paymentStatus(PaymentStatus.PENDING)
                    .stripeStatus(session.getStatus())
                    .build();

            paymentTransactionRepository.save(transaction);

            // Actualizar la cita con el session ID
            cita.setStripeSessionId(session.getId());
            citaService.saveCita(cita);

            log.info("Checkout session creada: {} para cita: {}", session.getId(), cita.getId());

            return CheckoutSessionResponse.builder()
                    .url(session.getUrl())
                    .sessionId(session.getId())
                    .message("Sesión de pago creada exitosamente")
                    .build();

        } catch (StripeException e) {
            log.error("Error al crear sesión de Stripe: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear sesión de pago: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(String sessionId) {
        try {
            Stripe.apiKey = stripeSecretKey;

            // Buscar la transacción en la base de datos
            PaymentTransaction transaction = paymentTransactionRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));

            // Obtener el estado desde Stripe
            Session session = Session.retrieve(sessionId);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("cita_id", String.valueOf(transaction.getCita().getId()));
            metadata.put("payment_status", transaction.getPaymentStatus().toString());

            return PaymentStatusResponse.builder()
                    .status(session.getStatus())
                    .paymentStatus(session.getPaymentStatus())
                    .amountTotal(transaction.getAmountTotal())
                    .currency(transaction.getCurrency())
                    .metadata(metadata)
                    .citaId(transaction.getCita().getId())
                    .build();

        } catch (StripeException e) {
            log.error("Error al obtener estado de pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar estado de pago: " + e.getMessage());
        }
    }

    @Transactional
    public void processSuccessfulPayment(String sessionId) {
        try {
            Stripe.apiKey = stripeSecretKey;

            PaymentTransaction transaction = paymentTransactionRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));

            // Verificar si ya fue procesada
            if (transaction.getPaymentStatus() == PaymentStatus.PAID) {
                log.warn("La transacción {} ya fue procesada anteriormente", sessionId);
                return;
            }

            // Obtener la sesión de Stripe para confirmar el pago
            Session session = Session.retrieve(sessionId);

            if ("paid".equals(session.getPaymentStatus())) {
                // Actualizar transacción
                transaction.setPaymentStatus(PaymentStatus.PAID);
                transaction.setStripeStatus(session.getStatus());
                transaction.setStripePaymentIntentId(session.getPaymentIntent());
                transaction.setPaidAt(ZonedDateTime.now());
                paymentTransactionRepository.save(transaction);

                // Actualizar cita
                Cita cita = transaction.getCita();
                cita.setEsPagada(true);
                cita.setEstado(EstadoCita.CONFIRMADA);
                citaService.saveCita(cita);

                log.info("Pago procesado exitosamente para cita: {}", cita.getId());
            }

        } catch (StripeException e) {
            log.error("Error al procesar pago exitoso: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar pago: " + e.getMessage());
        }
    }

    @Transactional
    protected void handleExpiredSession(String sessionId) {
        paymentTransactionRepository.findByStripeSessionId(sessionId)
                .ifPresent(transaction -> {
                    if (transaction.getPaymentStatus() == PaymentStatus.PENDING) {
                        transaction.setPaymentStatus(PaymentStatus.EXPIRED);
                        paymentTransactionRepository.save(transaction);
                        log.info("Sesión expirada: {}", sessionId);
                    }
                });
    }
}