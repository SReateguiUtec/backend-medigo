package com.example.medigo.dto.response;

import java.time.LocalDate;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePacienteDto {
    @Size(min = 2, max = 50)
    private String nombres;
    
    @Size(min = 2, max = 50)
    private String apellidos;
    
    @NotBlank(message = "Email no puede estar vacío")
    @Email
    private String email;

    @NotNull
    @Size(min = 18, max = 90)
    private Integer edad;

    @NotBlank(message = "Teléfono no puede estar vacío")
    @Pattern(regexp = "^\\d{9}$", message = "Teléfono debe tener 9 dígitos")
    private String telefono;
    
    @Past(message = "Fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;
    
    @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos")
    private String dni;
}