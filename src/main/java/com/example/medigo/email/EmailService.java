package com.example.medigo.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            log.info("Enviando correo simple a: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            javaMailSender.send(message);
            log.info("Correo simple enviado exitosamente a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar correo simple a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al enviar correo simple", e);
        }
    }

    @Async
    public void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("Enviando correo con template '{}' a: {}", templateName, to);
            
            // Verificar que la dirección de correo no esté vacía
            if (to == null || to.trim().isEmpty()) {
                log.warn("Dirección de correo vacía o nula. No se enviará el correo.");
                return;
            }
            
            Context context = new Context();
            context.setVariables(variables);

            String htmlBody = templateEngine.process(templateName, context);
            log.debug("Template procesado correctamente para: {}", to);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "El Equipo de MediGO");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            javaMailSender.send(message);
            log.info("Correo con template enviado exitosamente a: {}", to);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error al enviar correo con template a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Fallo al enviar el correo HTML", e);
        } catch (Exception e) {
            log.error("Error inesperado al procesar template para {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al procesar template de correo", e);
        }
    }
}