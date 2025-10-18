package com.example.medigo.dto.response;

import com.example.medigo.domain.EstadoCuenta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEstadoCuentaDto {
    
    @NotNull(message = "El estado de la cuenta no puede ser nulo")
    private EstadoCuenta estadoCuenta;
}