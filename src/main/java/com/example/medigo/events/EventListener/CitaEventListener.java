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

    @Async
    @EventListener
    public void handleCitaCreadaEvent(CitaCreadaEvent event) {
        log.info("Manejando CitaCreadaEvent para la cita ID: {}", event.getCita().getId());
        Cita cita = event.getCita();
        Medico medico = cita.getMedico();
        Paciente paciente = cita.getPaciente();

        Map<String, Object> variables = new HashMap<>();
        variables.put("nombreMedico", medico.getNombres());
        variables.put("nombrePaciente", paciente.getNombres() + " " + paciente.getApellidos());
        variables.put("fechaCita", cita.getFechaHora().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(new Locale("es", "ES"))));
        variables.put("horaCita", cita.getFechaHora().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(new Locale("es", "ES"))));

        emailService.sendEmailWithTemplate(medico.getEmail(), "¡Nueva Cita Agendada!", "cita-agendada-medico", variables);
    }
}