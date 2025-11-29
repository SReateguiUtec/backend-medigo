package com.example.medigo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIConsultaResponse {
    private String respuesta;
    private String disclaimer;
    private Integer consultasRestantes;
    private Long tiempoHastaReset; // en segundos
}
