package com.example.medigo.dto.response;

import com.example.medigo.domain.Rol;
import com.example.medigo.domain.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponseDto {
    
    private String nombres;
    private String apellidos;
    private String email;
    private Integer edad;
    private Rol rol;
    private String dni;
    private String telefono;
    private LocalDate fechaNacimiento;
    private ZonedDateTime createdAt;
    private EstadoCuenta estadoCuenta;
}