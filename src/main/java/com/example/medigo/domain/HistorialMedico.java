package com.example.medigo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "Historial_Medico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    @Column(nullable = false, length = 1000)
    private String diagnostico;

    @Column(length = 1000)
    private String receta;

    @Column(length = 2000)
    private String notas;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}