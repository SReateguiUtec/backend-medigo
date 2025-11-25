package com.example.medigo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Medico extends Usuario {

    @Column(unique = true, length = 8)
    private String dni;

    @Column(unique = true, length = 20)
    private String numeroColegiado;

    private String bio;

    @Column(name = "precio_consulta", precision = 10, scale = 2)
    private BigDecimal precioConsulta;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "medico_especialidad",
            joinColumns = @JoinColumn(name = "medico_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id")
    )
    @JsonIgnore
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