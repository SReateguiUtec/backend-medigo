package com.example.medigo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Medico extends Usuario {

    @Column(unique = true, length = 8)
    private String dni;

    @Column(unique = true, length = 8)
    private String numeroColegiado;

    private String bio;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "medico_especialidad",
            joinColumns = @JoinColumn(name = "medico_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id")
    )
    private Set<Especialidad> especialidades = new HashSet<>();

    @Builder
    public Medico(Long id,
                    String nombres,
                    String apellidos,
                    String email,
                    String password,
                    Integer edad,
                    String telefono,
                    String rutaFoto,
                    Rol rol,
                    EstadoCuenta estadoCuenta,
                    ZonedDateTime createdAt,
                    String dni,
                    String numeroColegiado) {

        super(id, nombres, apellidos, email, password, edad, telefono, rutaFoto, rol, estadoCuenta, createdAt);
        this.dni = dni;
        this.numeroColegiado = numeroColegiado;
    }
}