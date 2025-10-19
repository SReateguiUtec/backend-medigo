package com.example.medigo.controller;

import com.example.medigo.domain.Cita;
import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.CreateCitaRequestDto;
import com.example.medigo.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Cita> createCita(
            @Valid @RequestBody CreateCitaRequestDto request,
            @AuthenticationPrincipal Usuario paciente) {
        Cita nuevaCita = citaService.createCita(request, paciente.getId());
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    @PatchMapping("/{citaId}/cancel")
    @PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO')")
    public ResponseEntity<Cita> cancelCita(
            @PathVariable Long citaId,
            @AuthenticationPrincipal Usuario usuario) {
        Cita citaCancelada = citaService.cancelCita(citaId, usuario);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/{citaId}")
    @PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO')")
    public ResponseEntity<Cita> getCitaDetails(
            @PathVariable Long citaId,
            @AuthenticationPrincipal Usuario usuario) {
        Cita cita = citaService.getCitaDetails(citaId, usuario);
        return ResponseEntity.ok(cita);
    }
}