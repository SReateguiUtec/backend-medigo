package com.example.medigo.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class CreateCitaRequestDto {

    @NotNull(message = "El ID del médico no puede ser nulo")
    private Long medicoId;

    @NotNull(message = "La fecha y hora no pueden ser nulas")
    @Future(message = "La fecha de la cita debe ser en el futuro")
    private ZonedDateTime fechaHora;
}