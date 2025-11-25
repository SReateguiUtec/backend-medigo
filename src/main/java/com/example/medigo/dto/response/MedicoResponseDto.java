package com.example.medigo.dto.response;

import com.example.medigo.domain.Especialidad;
import com.example.medigo.domain.Rol;
import com.example.medigo.domain.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicoResponseDto {
    
    private String nombres;
    private String apellidos;
    private String email;
    private Integer edad;
    private Rol rol;
    private String telefono;
    private String dni;
    private String numeroColegiado;
    private String bio;
    private BigDecimal precioConsulta;
    private Set<Especialidad> especialidades;
    private ZonedDateTime createdAt;
    private EstadoCuenta estadoCuenta;
}