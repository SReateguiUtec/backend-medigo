package com.example.medigo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historial-medico")
public class HistorialMedicoController {

    // Si es medico, ver el historial de sus pacientes (en el front hacemos un dashboard)
    // Si es medico, ver el historial de un paciente en especifico
    // Si es paciente, ver su propio historial de acuerdo al medico que lo atendio
}
