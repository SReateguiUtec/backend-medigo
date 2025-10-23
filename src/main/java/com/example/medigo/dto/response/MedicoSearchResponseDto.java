package com.example.medigo.dto.response;

import com.example.medigo.domain.Especialidad;
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
public class MedicoSearchResponseDto {
    
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private Integer edad;
    private String telefono;
    private String rutaFoto;
    private String bio;
    private BigDecimal precioConsulta;
    private Set<Especialidad> especialidades;
    private String numeroColegiado;
    private EstadoCuenta estadoCuenta;
    private ZonedDateTime createdAt;
}
