package com.example.medigo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Paciente extends Usuario {

    @Column(length = 8, unique = true)
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Builder
    public Paciente(Long id,
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
                    LocalDate fechaNacimiento) {
        super(id, nombres, apellidos, email, password, edad, telefono, rutaFoto, rol, estadoCuenta, createdAt);
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
    }
}