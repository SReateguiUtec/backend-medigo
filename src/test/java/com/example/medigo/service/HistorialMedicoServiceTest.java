package com.example.medigo.service;

import com.example.medigo.domain.Cita;
import com.example.medigo.domain.HistorialMedico;
import com.example.medigo.repository.CitaRepository;
import com.example.medigo.repository.HistorialMedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistorialMedicoServiceTest {

    @Mock
    private HistorialMedicoRepository historialMedicoRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private HistorialMedicoService historialMedicoService;

    private Cita cita;
    private HistorialMedico historial;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cita = new Cita();
        cita.setId(1L);

        historial = HistorialMedico.builder()
                .id(1L)
                .cita(cita)
                .diagnostico("Gripe común")
                .receta("Paracetamol 500mg")
                .notas("Controlar fiebre")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    void testCreateHistorialSuccess() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(historialMedicoRepository.existsByCitaId(1L)).thenReturn(false);
        when(historialMedicoRepository.save(any(HistorialMedico.class))).thenReturn(historial);

        HistorialMedico result = historialMedicoService.create(1L, historial);

        assertNotNull(result);
        assertEquals("Gripe común", result.getDiagnostico());
        verify(historialMedicoRepository, times(1)).save(any(HistorialMedico.class));
    }

    @Test
    void testCreateHistorialAlreadyExists() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(historialMedicoRepository.existsByCitaId(1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> historialMedicoService.create(1L, historial));
    }

    @Test
    void testGetById() {
        when(historialMedicoRepository.findById(1L)).thenReturn(Optional.of(historial));
        Optional<HistorialMedico> result = historialMedicoService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals("Gripe común", result.get().getDiagnostico());
    }
}
