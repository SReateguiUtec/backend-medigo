package com.example.medigo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;

import com.example.medigo.dto.response.UpdateEstadoCuentaDto;
import com.example.medigo.service.ProfileService;
import com.example.medigo.service.CloudinaryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final CloudinaryService cloudinaryService;

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
            @RequestBody Map<String, Object> updates) { // ✅ Cambiado de Object a Map
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

    // Cambiar foto de perfil
    @PostMapping("/me/photo")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<Object> uploadProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            String email = userDetails.getUsername();

            // Get current profile to check for existing photo
            Object currentProfile = profileService.getUserProfile(email);
            String oldPhotoUrl = null;

            // Extract old photo URL using reflection to handle both Paciente and Medico
            try {
                oldPhotoUrl = (String) currentProfile.getClass().getMethod("getRutaFoto").invoke(currentProfile);
            } catch (Exception e) {
                // Ignore if method doesn't exist
            }

            // Delete old photo if exists
            if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                cloudinaryService.deleteImage(oldPhotoUrl);
            }

            // Upload new photo
            String photoUrl = cloudinaryService.uploadImage(file, email);

            // Update profile with new photo URL
            Object updatedProfile = profileService.updateProfilePhoto(email, photoUrl);

            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error al subir la foto: " + e.getMessage()));
        }
    }

    // Eliminar foto de perfil
    @DeleteMapping("/me/photo")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<Object> deleteProfilePhoto(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();

            // Delete photo and get old URL
            String oldPhotoUrl = profileService.deleteProfilePhoto(email);

            // Delete from Cloudinary if exists
            if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                cloudinaryService.deleteImage(oldPhotoUrl);
            }

            // Get updated profile
            Object updatedProfile = profileService.getUserProfile(email);

            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error al eliminar la foto: " + e.getMessage()));
        }
    }
}
