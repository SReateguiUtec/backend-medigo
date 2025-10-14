package com.example.medigo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class EmailValidationConfig {

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${app.name}")
    private String appName;

    @Bean
    public ApplicationRunner validateEmailConfig() {
        return args -> {
            log.info("Validando configuración de correo...");

            if (mailUsername == null || mailUsername.trim().isEmpty()) {
                log.error("MAIL_SMPT_USERNAME no está configurado");
            } else {
                log.info("Email username configurado: {}", mailUsername);
            }

            if (mailPassword == null || mailPassword.trim().isEmpty()) {
                log.error("MAIL_SMPT_PASSWORD no está configurado");
            } else {
                log.info("Email password está configurado");
            }

            log.info("App name configurado: {}", appName);
            log.info("Configuración de correo validada");
        };
    }
}
