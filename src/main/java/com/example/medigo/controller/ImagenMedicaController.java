package com.example.medigo.controller;

import com.example.medigo.domain.ImagenMedica;
import com.example.medigo.domain.Usuario;
import com.example.medigo.service.ImagenMedicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes-medicas")
@RequiredArgsConstructor
public class ImagenMedicaController {

    private final ImagenMedicaService imagenMedicaService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('MEDICO') or hasRole('PACIENTE')")
    public ResponseEntity<ImagenMedica> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("historialMedicoId") Long historialMedicoId,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            ImagenMedica imagen = imagenMedicaService.uploadImage(historialMedicoId, file, description, usuario);
            return ResponseEntity.ok(imagen);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEDICO') or hasRole('PACIENTE') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            ImagenMedica imagen = imagenMedicaService.getImageById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found"));
            
            byte[] imageBytes = imagenMedicaService.getImageFile(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(imagen.getFileType()));
            headers.setContentLength(imageBytes.length);
            headers.set("Content-Disposition", "inline; filename=\"" + imagen.getFileName() + "\"");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/metadata/{id}")
    @PreAuthorize("hasRole('MEDICO') or hasRole('PACIENTE') or hasRole('ADMIN')")
    public ResponseEntity<ImagenMedica> getImageMetadata(@PathVariable Long id) {
        return imagenMedicaService.getImageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/historial/{historialId}")
    @PreAuthorize("hasRole('MEDICO') or hasRole('PACIENTE') or hasRole('ADMIN')")
    public ResponseEntity<List<ImagenMedica>> getImagesByHistorial(@PathVariable Long historialId) {
        List<ImagenMedica> imagenes = imagenMedicaService.getImagesByHistorial(historialId);
        return ResponseEntity.ok(imagenes);
    }

    @PutMapping("/{id}/annotations")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ImagenMedica> updateAnnotations(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String annotations = request.get("annotations");
            ImagenMedica updated = imagenMedicaService.updateAnnotations(id, annotations);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MEDICO') or hasRole('ADMIN') or hasRole('PACIENTE')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        try {
            imagenMedicaService.deleteImage(id);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
