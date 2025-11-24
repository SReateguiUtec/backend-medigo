package com.example.medigo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.medigo.dto.response.UpdateEstadoCuentaDto;
import com.example.medigo.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Ver mi propio perfil
    @GetMapping("/me")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<Object> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Object profile = profileService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }
    
    // Editar mi propio perfil
    @PatchMapping("/me")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<Object> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> updates) {  // ✅ Cambiado de Object a Map
        String email = userDetails.getUsername();
        Object updatedProfile = profileService.updateUserProfile(email, updates);
        return ResponseEntity.ok(updatedProfile);
    }
    
    // Editar mi estado de cuenta
    @PatchMapping("/me/status")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<Object> updateAccountStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateEstadoCuentaDto statusDto) {
        String email = userDetails.getUsername();
        Object updatedProfile = profileService.updateAccountStatus(email, statusDto);
        return ResponseEntity.ok(updatedProfile);
    }

    // Cambiar foto de perfil (implementar)
    // Eliminar foto de perfil (implementar)
    // Cambiar mi propia contraseña (implementar)
}
