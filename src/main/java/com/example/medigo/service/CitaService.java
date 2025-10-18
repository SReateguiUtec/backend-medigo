package com.example.medigo.service;

import com.example.medigo.domain.Cita;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitaService {

    private final CitaRepository citaRepository;

    @Transactional(readOnly = true)
    public Cita findCitaById(Long citaId) {
        return citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + citaId));
    }

    @Transactional
    public Cita saveCita(Cita cita) {
        return citaRepository.save(cita);
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
