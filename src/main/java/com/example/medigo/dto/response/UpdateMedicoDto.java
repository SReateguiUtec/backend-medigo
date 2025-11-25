package com.example.medigo.dto.response;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMedicoDto {
    
    @Size(min = 2, max = 50)
    private String nombres;
    
    @Size(min = 2, max = 50)
    private String apellidos;
    
    @NotBlank(message = "Email es requerido")
    @Email
    private String email;
    
    @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos")
    private String dni;

    @NotNull
    @Size(min = 18, max = 90)
    private Integer edad;

    @NotBlank(message = "Teléfono no puede estar vacío")
    @Pattern(regexp = "^\\d{9}$", message = "Teléfono debe tener 9 dígitos")
    private String telefono;
    
    @NotBlank(message = "Número de colegiado es requerido")
    @Size(min = 1, max = 20, message = "Número de colegiado debe tener entre 1 y 20 caracteres")
    private String numeroColegiado;
    
    @Size(max = 500, message = "Bio no puede exceder 500 caracteres")
    private String bio;
}