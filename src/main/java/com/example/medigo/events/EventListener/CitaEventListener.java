package com.example.medigo.events.EventListener;

import com.example.medigo.domain.Cita;
import com.example.medigo.domain.Medico;
import com.example.medigo.domain.Paciente;
import com.example.medigo.email.EmailService;
import com.example.medigo.events.CitaCreadaEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitaEventListener {

    private final EmailService emailService;

    @Async("emailExecutor")
    @EventListener
    public void handleCitaCreadaEvent(CitaCreadaEvent event) {
        try {
            log.info("Manejando CitaCreadaEvent para la cita ID: {}", event.getCita().getId());
            Cita cita = event.getCita();
            Medico medico = cita.getMedico();
            Paciente paciente = cita.getPaciente();
            
            log.info("Enviando correo al médico: {}", medico.getEmail());
            // Enviar correo al médico
            Map<String, Object> variablesMedico = new HashMap<>();
            variablesMedico.put("nombreMedico", medico.getNombres());
            variablesMedico.put("nombrePaciente", paciente.getNombres() + " " + paciente.getApellidos());
            variablesMedico.put("fechaCita", cita.getFechaHora().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(new Locale("es", "ES"))));
            variablesMedico.put("horaCita", cita.getFechaHora().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(new Locale("es", "ES"))));

            emailService.sendEmailWithTemplate(medico.getEmail(), "¡Nueva Cita Agendada!", "cita-agendada-medico", variablesMedico);
            log.info("Correo enviado exitosamente al médico: {}", medico.getEmail());

            log.info("Enviando correo al paciente: {}", paciente.getEmail());
            // Enviar correo al paciente
            Map<String, Object> variablesPaciente = new HashMap<>();
            variablesPaciente.put("nombrePaciente", paciente.getNombres());
            variablesPaciente.put("nombreMedico", medico.getNombres() + " " + medico.getApellidos());
            variablesPaciente.put("especialidad", medico.getEspecialidades().isEmpty() ? "General" : medico.getEspecialidades().iterator().next().getNombre_especialidad());
            variablesPaciente.put("fechaCita", cita.getFechaHora().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(new Locale("es", "ES"))));
            variablesPaciente.put("horaCita", cita.getFechaHora().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(new Locale("es", "ES"))));
            variablesPaciente.put("precio", "S/ " + (cita.getPrecioConsulta() != null ? cita.getPrecioConsulta().setScale(2, BigDecimal.ROUND_HALF_UP) : "0.00"));

            emailService.sendEmailWithTemplate(paciente.getEmail(), "Confirmación de Cita Médica", "cita-agendada-paciente", variablesPaciente);
            log.info("Correo enviado exitosamente al paciente: {}", paciente.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar correos para la cita: {}", event.getCita().getId(), e);
        }
    }
}