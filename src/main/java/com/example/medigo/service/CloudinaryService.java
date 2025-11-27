package com.example.medigo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadImage(MultipartFile file, String userId) throws IOException {

    if (file.isEmpty()) {
        throw new IllegalArgumentException("El archivo está vacío");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
        throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido de 5MB");
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
        throw new IllegalArgumentException("Nombre de archivo inválido");
    }

    String fileExtension = getFileExtension(originalFilename).toLowerCase();
    if (!ALLOWED_FORMATS.contains(fileExtension)) {
        throw new IllegalArgumentException("Formato de archivo no permitido. Solo se permiten: " + String.join(", ", ALLOWED_FORMATS));
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
        throw new IllegalArgumentException("El archivo debe ser una imagen");
    }

    String publicId = "medigo/profile-photos/" + userId + "_" + System.currentTimeMillis();

    Map<String, Object> uploadParams = ObjectUtils.asMap(
            "public_id", publicId,
            "folder", "medigo/profile-photos",
            "transformation", "c_fill,g_face,h_400,w_400/q_auto,f_auto",
            "overwrite", true,
            "resource_type", "image"
    );

    Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
    return (String) uploadResult.get("secure_url");
}

    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null && !publicId.isEmpty()) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Cloudinary URL format:
        // https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];

            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");

            int lastDotIndex = withoutVersion.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return withoutVersion.substring(0, lastDotIndex);
            }

            return withoutVersion;
        } catch (Exception e) {
            System.err.println("Error extracting public_id from URL: " + e.getMessage());
            return null;
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}
