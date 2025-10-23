package com.example.medigo.controller;

import com.example.medigo.domain.HistorialMedico;
import com.example.medigo.service.HistorialMedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial-medico")
@RequiredArgsConstructor
public class HistorialMedicoController {

    private final HistorialMedicoService historialMedicoService;

    @GetMapping
    public ResponseEntity<List<HistorialMedico>> getAll() {
        return ResponseEntity.ok(historialMedicoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialMedico> getById(@PathVariable Long id) {
        return historialMedicoService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cita/{citaId}")
    public ResponseEntity<HistorialMedico> getByCita(@PathVariable Long citaId) {
        return historialMedicoService.getByCitaId(citaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/cita/{citaId}")
    public ResponseEntity<HistorialMedico> create(
            @PathVariable Long citaId,
            @RequestBody HistorialMedico historial) {
        return ResponseEntity.ok(historialMedicoService.create(citaId, historial));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialMedico> update(
            @PathVariable Long id,
            @RequestBody HistorialMedico historial) {
        return ResponseEntity.ok(historialMedicoService.update(id, historial));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        historialMedicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
