package com.example.medigo.dto.response;

import com.example.medigo.domain.EstadoCuenta;
import com.example.medigo.domain.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDto {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private Rol rol;
    private EstadoCuenta estadoCuenta;
    private ZonedDateTime createdAt;
}