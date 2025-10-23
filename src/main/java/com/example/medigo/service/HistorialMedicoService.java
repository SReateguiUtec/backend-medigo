package com.example.medigo.service;

import com.example.medigo.domain.HistorialMedico;
import com.example.medigo.domain.Cita;
import com.example.medigo.repository.CitaRepository;
import com.example.medigo.repository.HistorialMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistorialMedicoService {

    private final HistorialMedicoRepository historialMedicoRepository;
    private final CitaRepository citaRepository;

    public List<HistorialMedico> getAll() {
        return historialMedicoRepository.findAll();
    }

    public Optional<HistorialMedico> getById(Long id) {
        return historialMedicoRepository.findById(id);
    }

    public Optional<HistorialMedico> getByCitaId(Long citaId) {
        return historialMedicoRepository.findByCitaId(citaId);
    }

    public HistorialMedico create(Long citaId, HistorialMedico historialRequest) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con id: " + citaId));

        if (historialMedicoRepository.existsByCitaId(citaId)) {
            throw new RuntimeException("Ya existe un historial médico para esta cita");
        }

        historialRequest.setCita(cita);
        return historialMedicoRepository.save(historialRequest);
    }

    public HistorialMedico update(Long id, HistorialMedico newData) {
        HistorialMedico historial = historialMedicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historial no encontrado con id: " + id));

        historial.setDiagnostico(newData.getDiagnostico());
        historial.setReceta(newData.getReceta());
        historial.setNotas(newData.getNotas());

        return historialMedicoRepository.save(historial);
    }

    public void delete(Long id) {
        historialMedicoRepository.deleteById(id);
    }
}
