package com.example.medigo.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsuarioDto {
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    private String nombres;
    
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    private String apellidos;
    
    @NotBlank(message = "Email no puede estar vacío")
    @Email(message = "Email debe ser válido")
    private String email;
}