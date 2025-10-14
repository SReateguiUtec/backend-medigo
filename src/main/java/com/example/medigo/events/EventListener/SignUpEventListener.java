package com.example.medigo.events.EventListener;

import com.example.medigo.domain.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.medigo.email.EmailService;
import com.example.medigo.events.SignUpEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SignUpEventListener {

    private final EmailService emailService;

    @Value("${app.name}")
    private String appName;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @EventListener
    @Async
    public void handleSignUpEvent(SignUpEvent event) {
        Usuario usuario = event.getUsuario();
        log.info("Nuevo registro. Preparando correo de bienvenida para: {}", usuario.getEmail());

        Map<String, Object> variables = new HashMap<>();
        variables.put("appName", appName);
        variables.put("userName", usuario.getNombres() != null ? usuario.getNombres() : "Nuevo Usuario");
        variables.put("loginUrl", frontendUrl + "/login");
        
        try {
            emailService.sendEmailWithTemplate(
                    usuario.getEmail(),
                    "¡Bienvenido a " + appName + "!",
                    "welcome",
                    variables);
            log.info("Correo de bienvenida enviado exitosamente a {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar el correo de bienvenida a {}: {}", usuario.getEmail(), e.getMessage(), e);
        }
    }
}
