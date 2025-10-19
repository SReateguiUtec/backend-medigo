package com.example.medigo.events;

import com.example.medigo.domain.Cita;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CitaCreadaEvent extends ApplicationEvent {
    private final Cita cita;

    public CitaCreadaEvent(Object source, Cita cita) {
        super(source);
        this.cita = cita;
    }
}