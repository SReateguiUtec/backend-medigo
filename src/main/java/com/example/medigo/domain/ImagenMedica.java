package com.example.medigo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "Imagen_Medica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historial_medico_id", nullable = false)
    private HistorialMedico historialMedico;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, length = 50)
    private String fileType; // image/jpeg, image/png, etc.

    @Column(nullable = false)
    private Long fileSize; // in bytes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Usuario uploadedBy;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String annotations; // JSON string

    @Column(nullable = false)
    private ZonedDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = ZonedDateTime.now();
    }
}
