package com.example.medigo.service;

import com.example.medigo.domain.*;
import com.example.medigo.dto.request.CreateCitaRequestDto;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.CitaRepository;
import com.example.medigo.repository.MedicoRepository;
import com.example.medigo.repository.PacienteRepository;
import com.example.medigo.events.CitaCreadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Cita findCitaById(Long citaId) {
        return citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + citaId));
    }

    @Transactional
    public Cita saveCita(Cita cita) {
        return citaRepository.save(cita);
    }

    @Transactional
    public Cita createCita(CreateCitaRequestDto request, Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId));

        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Médico no encontrado con ID: " + request.getMedicoId()));

        if (medico.getPrecioConsulta() == null || medico.getPrecioConsulta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El médico seleccionado no tiene un precio de consulta válido.");
        }

        // Verificar si ya existe una cita para este médico en esta fecha/hora
        boolean existeCita = citaRepository.existsByMedicoIdAndFechaHoraAndEstadoNotCancelada(
                request.getMedicoId(),
                request.getFechaHora());

        if (existeCita) {
            throw new IllegalStateException(
                    "El médico ya tiene una cita agendada en este horario. Por favor seleccione otra fecha u hora.");
        }

        Cita nuevaCita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fechaHora(request.getFechaHora())
                .estado(EstadoCita.PENDIENTE)
                .esPagada(false)
                .precioConsulta(medico.getPrecioConsulta()) // Se copia el precio actual del médico
                .build();

        Cita savedCita = citaRepository.save(nuevaCita);
        log.info("Nueva cita creada con ID: {} para paciente {} con médico {}", savedCita.getId(), paciente.getId(),
                medico.getId());

        eventPublisher.publishEvent(new CitaCreadaEvent(this, savedCita));
        return savedCita;
    }

    @Transactional
    public Cita cancelCita(Long citaId, Usuario usuario) {

        Cita cita = findCitaById(citaId);

        boolean isPacienteInCita = usuario.getRol() == Rol.PACIENTE
                && cita.getPaciente().getId().equals(usuario.getId());
        boolean isMedicoInCita = usuario.getRol() == Rol.MEDICO && cita.getMedico().getId().equals(usuario.getId());

        if (!isPacienteInCita && !isMedicoInCita) {
            throw new AccessDeniedException("No tiene permiso para cancelar esta cita.");
        }

        if (cita.getEstado() != EstadoCita.PENDIENTE && cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new IllegalStateException("La cita no puede ser cancelada porque su estado es: " + cita.getEstado());
        }

        if (Boolean.TRUE.equals(cita.getEsPagada())) {
            // TODO: Implementar logica de reembolso con Stripe antes de cancelar
            throw new IllegalStateException(
                    "No se puede cancelar una cita que ya ha sido confirmada del pago. Contacte a soporte para un reembolso.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Transactional(readOnly = true)
    public Cita getCitaDetails(Long citaId, Usuario usuario) {

        Cita cita = findCitaById(citaId);

        boolean isPacienteInCita = usuario.getRol() == Rol.PACIENTE
                && cita.getPaciente().getId().equals(usuario.getId());
        boolean isMedicoInCita = usuario.getRol() == Rol.MEDICO && cita.getMedico().getId().equals(usuario.getId());

        if (!isPacienteInCita && !isMedicoInCita) {
            throw new AccessDeniedException("No tiene permiso para ver los detalles de esta cita.");
        }

        return cita;
    }

    @Transactional(readOnly = true)
    public List<Cita> findCitasByPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    @Transactional(readOnly = true)
    public List<Cita> findCitasByMedico(Long medicoId) {
        return citaRepository.findByMedicoId(medicoId);
    }
}
