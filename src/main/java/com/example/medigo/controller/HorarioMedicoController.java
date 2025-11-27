package com.example.medigo.controller;

import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.CreateHorarioMedicoRequest;
import com.example.medigo.dto.response.HorarioMedicoResponse;
import com.example.medigo.dto.response.SlotDisponibleResponse;
import com.example.medigo.service.HorarioMedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
@Slf4j
public class HorarioMedicoController {

    private final HorarioMedicoService horarioMedicoService;

    @PostMapping("/{medicoId}/horarios")
    public ResponseEntity<HorarioMedicoResponse> createHorario(
            @PathVariable Long medicoId,
            @Valid @RequestBody CreateHorarioMedicoRequest request,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Verificar que el usuario es el médico
        if (!usuario.getId().equals(medicoId)) {
            return ResponseEntity.status(403).build();
        }

        HorarioMedicoResponse response = horarioMedicoService.createHorario(medicoId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{medicoId}/horarios")
    public ResponseEntity<List<HorarioMedicoResponse>> getHorarios(@PathVariable Long medicoId) {
        List<HorarioMedicoResponse> horarios = horarioMedicoService.getHorariosByMedico(medicoId);
        return ResponseEntity.ok(horarios);
    }

    @DeleteMapping("/{medicoId}/horarios/{horarioId}")
    public ResponseEntity<Void> deleteHorario(
            @PathVariable Long medicoId,
            @PathVariable Long horarioId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Verificar que el usuario es el médico
        if (!usuario.getId().equals(medicoId)) {
            return ResponseEntity.status(403).build();
        }

        horarioMedicoService.deleteHorario(medicoId, horarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{medicoId}/slots-disponibles")
    public ResponseEntity<List<SlotDisponibleResponse>> getSlotsDisponibles(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        log.info("Obteniendo slots disponibles para médico {} en fecha {}", medicoId, fecha);
        List<SlotDisponibleResponse> slots = horarioMedicoService.getSlotsDisponibles(medicoId, fecha);
        return ResponseEntity.ok(slots);
    }
}
