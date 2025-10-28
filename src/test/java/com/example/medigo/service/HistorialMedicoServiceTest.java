package com.example.medigo.service;

import com.example.medigo.domain.Cita;
import com.example.medigo.domain.HistorialMedico;
import com.example.medigo.repository.CitaRepository;
import com.example.medigo.repository.HistorialMedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests del Servicio de Historial Médico")
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
    @DisplayName("Should get all historial medicos")
    void shouldGetAllHistorialMedicos() {
        // Given
        List<HistorialMedico> historialList = Collections.singletonList(historial);
        when(historialMedicoRepository.findAll()).thenReturn(historialList);

        // When
        List<HistorialMedico> result = historialMedicoService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(historial, result.get(0));
        verify(historialMedicoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get historial medico by ID when exists")
    void shouldGetHistorialMedicoByIdWhenExists() {
        // Given
        when(historialMedicoRepository.findById(1L)).thenReturn(Optional.of(historial));

        // When
        Optional<HistorialMedico> result = historialMedicoService.getById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(historial, result.get());
        verify(historialMedicoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when historial medico by ID does not exist")
    void shouldReturnEmptyWhenHistorialMedicoByIdDoesNotExist() {
        // Given
        when(historialMedicoRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<HistorialMedico> result = historialMedicoService.getById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(historialMedicoRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get historial medico by cita ID when exists")
    void shouldGetHistorialMedicoByCitaIdWhenExists() {
        // Given
        when(historialMedicoRepository.findByCitaId(1L)).thenReturn(Optional.of(historial));

        // When
        Optional<HistorialMedico> result = historialMedicoService.getByCitaId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(historial, result.get());
        verify(historialMedicoRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should create historial medico when cita exists and no historial exists for cita")
    void shouldCreateHistorialMedicoWhenCitaExistsAndNoHistorialExistsForCita() {
        // Given
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(historialMedicoRepository.existsByCitaId(1L)).thenReturn(false);
        when(historialMedicoRepository.save(any(HistorialMedico.class))).thenReturn(historial);

        // When
        HistorialMedico result = historialMedicoService.create(1L, historial);

        // Then
        assertNotNull(result);
        assertEquals("Gripe común", result.getDiagnostico());
        verify(citaRepository, times(1)).findById(1L);
        verify(historialMedicoRepository, times(1)).existsByCitaId(1L);
        verify(historialMedicoRepository, times(1)).save(any(HistorialMedico.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when creating historial for non-existent cita")
    void shouldThrowRuntimeExceptionWhenCreatingHistorialForNonExistentCita() {
        // Given
        when(citaRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historialMedicoService.create(1L, historial);
        });
        
        assertEquals("Cita no encontrada con id: 1", exception.getMessage());
        verify(citaRepository, times(1)).findById(1L);
        verify(historialMedicoRepository, never()).existsByCitaId(anyLong());
        verify(historialMedicoRepository, never()).save(any(HistorialMedico.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when creating historial that already exists for cita")
    void shouldThrowRuntimeExceptionWhenCreatingHistorialThatAlreadyExistsForCita() {
        // Given
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(historialMedicoRepository.existsByCitaId(1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historialMedicoService.create(1L, historial);
        });
        
        assertEquals("Ya existe un historial médico para esta cita", exception.getMessage());
        verify(citaRepository, times(1)).findById(1L);
        verify(historialMedicoRepository, times(1)).existsByCitaId(1L);
        verify(historialMedicoRepository, never()).save(any(HistorialMedico.class));
    }

    @Test
    @DisplayName("Should update historial medico when exists")
    void shouldUpdateHistorialMedicoWhenExists() {
        // Given
        HistorialMedico updatedHistorial = HistorialMedico.builder()
                .id(1L)
                .cita(cita)
                .diagnostico("Neumonía")
                .receta("Antibióticos")
                .notas("Reposo absoluto")
                .createdAt(ZonedDateTime.now())
                .build();

        when(historialMedicoRepository.findById(1L)).thenReturn(Optional.of(historial));
        when(historialMedicoRepository.save(historial)).thenReturn(updatedHistorial);

        // When
        HistorialMedico result = historialMedicoService.update(1L, updatedHistorial);

        // Then
        assertNotNull(result);
        assertEquals("Neumonía", result.getDiagnostico());
        assertEquals("Antibióticos", result.getReceta());
        assertEquals("Reposo absoluto", result.getNotas());
        verify(historialMedicoRepository, times(1)).findById(1L);
        verify(historialMedicoRepository, times(1)).save(historial);
    }

    @Test
    @DisplayName("Should throw RuntimeException when updating non-existent historial")
    void shouldThrowRuntimeExceptionWhenUpdatingNonExistentHistorial() {
        // Given
        when(historialMedicoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historialMedicoService.update(999L, historial);
        });
        
        assertEquals("Historial no encontrado con id: 999", exception.getMessage());
        verify(historialMedicoRepository, times(1)).findById(999L);
        verify(historialMedicoRepository, never()).save(any(HistorialMedico.class));
    }

    @Test
    @DisplayName("Should delete historial medico by ID")
    void shouldDeleteHistorialMedicoById() {
        // When
        historialMedicoService.delete(1L);

        // Then
        verify(historialMedicoRepository, times(1)).deleteById(1L);
    }
}