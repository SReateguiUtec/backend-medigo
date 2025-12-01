package com.example.medigo.service;

import com.example.medigo.domain.HistorialMedico;
import com.example.medigo.domain.ImagenMedica;
import com.example.medigo.domain.Usuario;
import com.example.medigo.repository.HistorialMedicoRepository;
import com.example.medigo.repository.ImagenMedicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImagenMedicaService {

    private final ImagenMedicaRepository imagenMedicaRepository;
    private final HistorialMedicoRepository historialMedicoRepository;

    @Value("${file.upload.dir:uploads/medical-images}")
    private String uploadDir;

    @Transactional
    public ImagenMedica uploadImage(Long historialMedicoId, MultipartFile file, String description, Usuario uploadedBy) throws IOException {
        // Validar archivo
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Get historial medico
        HistorialMedico historialMedico = historialMedicoRepository.findById(historialMedicoId)
                .orElseThrow(() -> new IllegalArgumentException("Historial medico not found"));

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "historial-" + historialMedicoId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre unico
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Guardar archivo
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Record en la database
        ImagenMedica imagenMedica = ImagenMedica.builder()
                .historialMedico(historialMedico)
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .fileType(contentType)
                .fileSize(file.getSize())
                .uploadedBy(uploadedBy)
                .description(description)
                .build();

        return imagenMedicaRepository.save(imagenMedica);
    }

    public Optional<ImagenMedica> getImageById(Long id) {
        return imagenMedicaRepository.findById(id);
    }

    public List<ImagenMedica> getImagesByHistorial(Long historialMedicoId) {
        return imagenMedicaRepository.findByHistorialMedicoIdOrderByUploadedAtDesc(historialMedicoId);
    }

    public byte[] getImageFile(Long id) throws IOException {
        ImagenMedica imagen = imagenMedicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        Path filePath = Paths.get(imagen.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IOException("Image file not found on disk");
        }

        return Files.readAllBytes(filePath);
    }

    @Transactional
    public ImagenMedica updateAnnotations(Long id, String annotations) {
        ImagenMedica imagen = imagenMedicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        imagen.setAnnotations(annotations);
        return imagenMedicaRepository.save(imagen);
    }

    @Transactional
    public void deleteImage(Long id) throws IOException {
        ImagenMedica imagen = imagenMedicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        Path filePath = Paths.get(imagen.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        imagenMedicaRepository.delete(imagen);
    }
}
