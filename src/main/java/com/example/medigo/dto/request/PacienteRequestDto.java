package com.example.medigo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PacienteRequestDto {

    @NotBlank
    @Size(max = 50, message = "Los nombres no pueden tener más de 50 caracteres")
    private String nombres;

    @Size(max = 50, message = "Los apellidos no pueden tener más de 50 caracteres")
    private String apellidos;

    @Email
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}