package com.example.medigo.events.Scheduler;

import com.example.medigo.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PagoMedicosScheduler {

    private final EmailService emailService;

    @Scheduled(fixedRate = 1296000000)
    public void enviarRecordatorioPago() {
        log.info("Ejecutando tarea programada de recordatorio de pago a médicos...");

        try {
            String adminEmail = "admin@medigo.com";
            String subject = "Recordatorio: Pago a médicos pendiente - MediGO";
            String message = "Estimado administrador,\n\n" +
                    "Este es un recordatorio automático de que hoy corresponde realizar los pagos a los médicos registrados.\n" +
                    "Por favor, revise las transacciones pendientes en el sistema.\n\n" +
                    "Saludos,\nEquipo MediGO";

            emailService.sendEmail(adminEmail, subject, message);
            log.info("Correo de recordatorio enviado al administrador: {}", adminEmail);

        } catch (Exception e) {
            log.error("Error al enviar el correo de recordatorio al admin: {}", e.getMessage());
        }
    }
}
