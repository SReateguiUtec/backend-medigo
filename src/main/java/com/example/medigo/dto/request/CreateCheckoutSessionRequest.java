package com.example.medigo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutSessionRequest {

    @NotNull(message = "El ID de la cita es requerido")
    private Long citaId;

    @NotNull(message = "La URL de origen es requerida")
    private String originUrl;

    private Map<String, String> metadata;
}