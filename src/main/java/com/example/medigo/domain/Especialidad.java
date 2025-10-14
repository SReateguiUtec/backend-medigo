package com.example.medigo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Especialidad")
@Builder
public class Especialidad {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre_especialidad;

    @ManyToMany(mappedBy = "especialidades", fetch = FetchType.LAZY)
    private Set<Medico> medicos = new HashSet<>();

    private String descripcion;
}
