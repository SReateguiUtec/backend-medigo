package com.example.medigo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Paciente extends Usuario {

    @Column(unique = true, length = 8)
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
