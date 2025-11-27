package com.example.medigo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDisponibleResponse {
    private ZonedDateTime fechaHora;
    private Boolean disponible;
}