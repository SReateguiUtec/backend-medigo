package com.example.medigo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Cita")
@Builder
public class Cita {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "fecha_hora", nullable = false)
    private ZonedDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    @Column(name = "precio_consulta", precision = 10, scale = 2)
    private BigDecimal precioConsulta;

    @Column(name = "es_pagada")
    private Boolean esPagada = false;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        if (esPagada == null) {
            esPagada = false;
        }
    }
}
