package com.example.medigo.events.Scheduler;

import com.example.medigo.domain.PaymentStatus;
import com.example.medigo.domain.PaymentTransaction;
import com.example.medigo.email.EmailService;
import com.example.medigo.repository.MedicoRepository;
import com.example.medigo.repository.PaymentTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class PagoMedicosScheduler {
    private final EmailService emailService;
    private final MedicoRepository medicoRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    
    @Value("${medigo.admin.email:admin@medigo.com}")
    private String adminEmail;

    // Run every 15 days (15 * 24 * 60 * 60 * 1000 = 1,296,000,000 milliseconds)
    @Scheduled(fixedRate = 1296000000)
    public void enviarRecordatorioPago() {
        log.info("Ejecutando tarea programada de recordatorio de pago a médicos...");
        
        try {
            // Calculate cutoff date (15 days ago)
            ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(15);
            
            // Find all paid transactions that are eligible for payout
            List<PaymentTransaction> eligibleTransactions = paymentTransactionRepository.findPaidTransactionsEligibleForPayout(cutoffDate);
            
            if (eligibleTransactions.isEmpty()) {
                log.info("No hay transacciones elegibles para pago en este momento.");
                return;
            }
            
            // Group transactions by doctor and calculate totals
            Map<Long, BigDecimal> doctorPayments = new HashMap<>();
            Map<Long, String> doctorNames = new HashMap<>();
            Map<Long, String> doctorEmails = new HashMap<>();
            
            for (PaymentTransaction transaction : eligibleTransactions) {
                Long medicoId = transaction.getMedico().getId();
                BigDecimal amount = transaction.getMedicoAmount();
                
                // Skip if already cancelled
                if (transaction.getPaymentStatus() == PaymentStatus.CANCELLED) {
                    continue;
                }
                
                // Accumulate payment amounts per doctor
                doctorPayments.merge(medicoId, amount, BigDecimal::add);
                doctorNames.put(medicoId, transaction.getMedico().getNombres() + " " + transaction.getMedico().getApellidos());
                doctorEmails.put(medicoId, transaction.getMedico().getEmail());
                
                // Mark transaction as cancelled to avoid duplicate processing
                transaction.setPaymentStatus(PaymentStatus.CANCELLED);
                paymentTransactionRepository.save(transaction);
            }
            
            // Send consolidated report to admin
            sendAdminPaymentReport(doctorPayments, doctorNames);
            
            // Send individual notifications to doctors
            sendDoctorNotifications(doctorPayments, doctorNames, doctorEmails);
            
            log.info("Tarea completada: recordatorios enviados a {} médicos.", doctorPayments.size());
            
        } catch (Exception e) {
            log.error("Error al ejecutar tarea programada de pagos a médicos: {}", e.getMessage(), e);
        }
    }
    
    private void sendAdminPaymentReport(Map<Long, BigDecimal> doctorPayments, Map<Long, String> doctorNames) {
        if (doctorPayments.isEmpty()) {
            return;
        }
        
        StringBuilder report = new StringBuilder();
        report.append("Reporte de Pagos a Médicos - MediGO\n\n");
        report.append("Fecha: ").append(ZonedDateTime.now().toLocalDate()).append("\n\n");
        report.append("Los siguientes médicos tienen pagos pendientes:\n\n");
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entry : doctorPayments.entrySet()) {
            Long medicoId = entry.getKey();
            BigDecimal amount = entry.getValue();
            String doctorName = doctorNames.get(medicoId);
            
            report.append("- ").append(doctorName).append(": $").append(amount).append("\n");
            totalAmount = totalAmount.add(amount);
        }
        
        report.append("\nTotal a pagar: $").append(totalAmount).append("\n");
        report.append("\nPor favor, procese estos pagos a la brevedad.\n\n");
        report.append("Saludos,\nSistema MediGO");
        
        try {
            emailService.sendEmail(adminEmail, "Reporte de Pagos a Médicos - MediGO", report.toString());
            log.info("Reporte de pagos enviado al administrador: {}", adminEmail);
        } catch (Exception e) {
            log.error("Error al enviar reporte de pagos al administrador: {}", e.getMessage(), e);
        }
    }
    
    private void sendDoctorNotifications(Map<Long, BigDecimal> doctorPayments, Map<Long, String> doctorNames, Map<Long, String> doctorEmails) {
        for (Map.Entry<Long, BigDecimal> entry : doctorPayments.entrySet()) {
            Long medicoId = entry.getKey();
            BigDecimal amount = entry.getValue();
            String doctorName = doctorNames.get(medicoId);
            String doctorEmail = doctorEmails.get(medicoId);
            
            try {
                String subject = "Notificación de Pago - MediGO";
                String message = "Estimado " + doctorName + ",\n\n" +
                        "Le informamos que se ha procesado un pago por el monto de $" + amount + 
                        " correspondiente a sus consultas médicas.\n\n" +
                        "El pago será depositado en su cuenta en los próximos días.\n\n" +
                        "Gracias por su trabajo.\n\n" +
                        "Saludos,\nEquipo MediGO";
                
                emailService.sendEmail(doctorEmail, subject, message);
                log.info("Notificación de pago enviada a: {}", doctorEmail);
            } catch (Exception e) {
                log.error("Error al enviar notificación de pago a {}: {}", doctorEmail, e.getMessage(), e);
            }
        }
    }
}