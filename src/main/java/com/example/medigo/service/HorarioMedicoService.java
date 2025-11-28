package com.example.medigo.service;

import com.example.medigo.domain.*;
import com.example.medigo.dto.request.CreateHorarioMedicoRequest;
import com.example.medigo.dto.response.HorarioMedicoResponse;
import com.example.medigo.dto.response.SlotDisponibleResponse;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.CitaRepository;
import com.example.medigo.repository.HorarioMedicoRepository;
import com.example.medigo.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HorarioMedicoService {

    private final HorarioMedicoRepository horarioMedicoRepository;
    private final MedicoRepository medicoRepository;
    private final CitaRepository citaRepository;

    @Transactional
    public HorarioMedicoResponse createHorario(Long medicoId, CreateHorarioMedicoRequest request) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado"));

        // Verificar si ya existe un horario para ese día
        List<HorarioMedico> horariosExistentes = horarioMedicoRepository
                .findByMedicoIdAndDiaSemanaAndActivoTrue(medicoId, request.getDiaSemana());

        if (!horariosExistentes.isEmpty()) {
            throw new IllegalStateException(
                    "Ya existe un horario configurado para " + request.getDiaSemana().getNombre() +
                            ". Por favor, elimina el horario existente antes de crear uno nuevo.");
        }

        HorarioMedico horario = HorarioMedico.builder()
                .medico(medico)
                .diaSemana(request.getDiaSemana())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .duracionCita(request.getDuracionCita())
                .activo(true)
                .build();

        HorarioMedico saved = horarioMedicoRepository.save(horario);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HorarioMedicoResponse> getHorariosByMedico(Long medicoId) {
        return horarioMedicoRepository.findByMedicoIdAndActivoTrue(medicoId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHorario(Long medicoId, Long horarioId) {
        horarioMedicoRepository.deleteByMedicoIdAndId(medicoId, horarioId);
    }

    @Transactional(readOnly = true)
    public List<SlotDisponibleResponse> getSlotsDisponibles(Long medicoId, LocalDate fecha) {
        // Convertir LocalDate a DiaSemana
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        DiaSemana diaSemana = convertToDiaSemana(dayOfWeek);

        // Obtener horarios del médico para ese día
        List<HorarioMedico> horarios = horarioMedicoRepository
                .findByMedicoIdAndDiaSemanaAndActivoTrue(medicoId, diaSemana);

        if (horarios.isEmpty()) {
            return new ArrayList<>();
        }

        // Obtener citas ya agendadas para ese día using ZonedDateTime
        ZonedDateTime inicioDia = fecha.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime finDia = fecha.atTime(23, 59, 59).atZone(ZoneId.systemDefault());
        List<Cita> citasAgendadas = citaRepository
                .findByMedicoIdAndFechaHoraBetweenAndEstadoNotCancelada(medicoId, inicioDia, finDia);

        List<SlotDisponibleResponse> slots = new ArrayList<>();

        // Para cada horario configurado, generar slots
        for (HorarioMedico horario : horarios) {
            LocalTime horaActual = horario.getHoraInicio();
            LocalTime horaFin = horario.getHoraFin();
            Integer duracion = horario.getDuracionCita();

            while (horaActual.plusMinutes(duracion).isBefore(horaFin) ||
                    horaActual.plusMinutes(duracion).equals(horaFin)) {
                // Use ZonedDateTime instead of LocalDateTime
                ZonedDateTime slotDateTime = fecha.atTime(horaActual).atZone(ZoneId.systemDefault());

                // Verificar si el slot está ocupado (compare only date and time, ignoring
                // nanoseconds)
                boolean ocupado = citasAgendadas.stream()
                        .anyMatch(cita -> cita.getFechaHora().toInstant().getEpochSecond() == slotDateTime.toInstant()
                                .getEpochSecond());

                slots.add(SlotDisponibleResponse.builder()
                        .fechaHora(slotDateTime)
                        .disponible(!ocupado)
                        .build());

                horaActual = horaActual.plusMinutes(duracion);
            }
        }

        return slots;
    }

    private DiaSemana convertToDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private HorarioMedicoResponse mapToResponse(HorarioMedico horario) {
        return HorarioMedicoResponse.builder()
                .id(horario.getId())
                .diaSemana(horario.getDiaSemana())
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .duracionCita(horario.getDuracionCita())
                .activo(horario.getActivo())
                .build();
    }
}
