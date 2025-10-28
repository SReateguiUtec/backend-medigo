package com.example.medigo.events.Scheduler;

import com.example.medigo.email.EmailService;
import com.example.medigo.repository.MedicoRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PagoMedicosScheduler {
    private final EmailService emailService;
    private final MedicoRepository medicoRepository;

    @Scheduled(fixedRate = 1296000000)
    public void enviarRecordatorioPago() {
        log.info("Ejecutando tarea programada de recordatorio de pago a médicos...");

        medicoRepository.findAll().forEach(medico -> {
            try{
                String subject = "Recordatorio de pago - MediGO";
                String message = "Estimado " + medico.getNombres() + ",\n\n" +
                        "Le recordamos que su pago está pendiente.\n" +
                        "Por favor, verifique con el área administrativa.\n\n" +
                        "Saludos,\nEquipo MediGO";

                emailService.sendEmail(medico.getEmail(), subject, message);
                log.info("Correo enviado a: {}", medico.getEmail());
            } catch (Exception e){
                log.error("Error al enviar correo a {}: {}", medico.getEmail(), e.getMessage());
            }

        });
        log.info("Tarea completada: recordatorios enviados a todos los médicos registrados.");
    }
}
