package com.example.medigo.events;

import com.example.medigo.domain.Usuario;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SignUpEvent extends ApplicationEvent {
    private final Usuario usuario;

    public SignUpEvent(Object source, Usuario usuario) {
        super(source);
        this.usuario = usuario;
    }
}
