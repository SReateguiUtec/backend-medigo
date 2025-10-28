package com.example.medigo.service;

import com.example.medigo.domain.*;
import com.example.medigo.dto.request.CreateCitaRequestDto;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.CitaRepository;
import com.example.medigo.repository.MedicoRepository;
import com.example.medigo.repository.PacienteRepository;
import com.example.medigo.events.CitaCreadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests del Servicio de Citas")
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CitaService citaService;

    private Cita testCita;
    private Paciente testPaciente;
    private Medico testMedico;
    private Usuario testUsuarioPaciente;
    private Usuario testUsuarioMedico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup Paciente
        testPaciente = new Paciente();
        testPaciente.setId(1L);
        testPaciente.setNombres("Juan");
        testPaciente.setApellidos("Pérez");
        testPaciente.setEmail("juan@test.com");
        testPaciente.setRol(Rol.PACIENTE);

        // Setup Medico
        testMedico = new Medico();
        testMedico.setId(2L);
        testMedico.setNombres("Dr. Carlos");
        testMedico.setApellidos("García");
        testMedico.setEmail("carlos@test.com");
        testMedico.setRol(Rol.MEDICO);
        testMedico.setPrecioConsulta(new BigDecimal("100.00"));

        // Setup Cita
        testCita = new Cita();
        testCita.setId(1L);
        testCita.setPaciente(testPaciente);
        testCita.setMedico(testMedico);
        testCita.setFechaHora(ZonedDateTime.now().plusDays(1));
        testCita.setEstado(EstadoCita.PENDIENTE);
        testCita.setEsPagada(false);
        testCita.setPrecioConsulta(new BigDecimal("100.00"));

        // Setup Usuarios for authorization tests
        testUsuarioPaciente = new Usuario();
        testUsuarioPaciente.setId(1L);
        testUsuarioPaciente.setRol(Rol.PACIENTE);

        testUsuarioMedico = new Usuario();
        testUsuarioMedico.setId(2L);
        testUsuarioMedico.setRol(Rol.MEDICO);
    }

    @Test
    @DisplayName("Should find cita by ID when cita exists")
    void shouldFindCitaByIdWhenCitaExists() {
        // Given
        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));

        // When
        Cita result = citaService.findCitaById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(citaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when cita does not exist by ID")
    void shouldThrowResourceNotFoundExceptionWhenCitaDoesNotExistById() {
        // Given
        when(citaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            citaService.findCitaById(999L);
        });
        
        verify(citaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should save cita successfully")
    void shouldSaveCitaSuccessfully() {
        // Given
        when(citaRepository.save(testCita)).thenReturn(testCita);

        // When
        Cita result = citaService.saveCita(testCita);

        // Then
        assertNotNull(result);
        assertEquals(testCita, result);
        verify(citaRepository, times(1)).save(testCita);
    }

    @Test
    @DisplayName("Should create cita when valid data provided")
    void shouldCreateCitaWhenValidDataProvided() {
        // Given
        CreateCitaRequestDto request = new CreateCitaRequestDto();
        request.setMedicoId(2L);
        request.setFechaHora(ZonedDateTime.now().plusDays(1));

        Cita savedCita = new Cita();
        savedCita.setId(1L);
        savedCita.setPaciente(testPaciente);
        savedCita.setMedico(testMedico);
        savedCita.setFechaHora(ZonedDateTime.now().plusDays(1));
        savedCita.setEstado(EstadoCita.PENDIENTE);
        savedCita.setEsPagada(false);
        savedCita.setPrecioConsulta(new BigDecimal("100.00"));

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(testPaciente));
        when(medicoRepository.findById(2L)).thenReturn(Optional.of(testMedico));
        when(citaRepository.save(any(Cita.class))).thenReturn(savedCita);

        // When
        Cita result = citaService.createCita(request, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(EstadoCita.PENDIENTE, result.getEstado());
        verify(pacienteRepository, times(1)).findById(1L);
        verify(medicoRepository, times(1)).findById(2L);
        verify(citaRepository, times(1)).save(any(Cita.class));
        verify(eventPublisher, times(1)).publishEvent(any(CitaCreadaEvent.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating cita with non-existent paciente")
    void shouldThrowResourceNotFoundExceptionWhenCreatingCitaWithNonExistentPaciente() {
        // Given
        CreateCitaRequestDto request = new CreateCitaRequestDto();
        request.setMedicoId(2L);
        request.setFechaHora(ZonedDateTime.now().plusDays(1));

        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            citaService.createCita(request, 1L);
        });
        
        verify(pacienteRepository, times(1)).findById(1L);
        verify(medicoRepository, never()).findById(anyLong());
        verify(citaRepository, never()).save(any(Cita.class));
        verify(eventPublisher, never()).publishEvent(any(CitaCreadaEvent.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating cita with non-existent medico")
    void shouldThrowResourceNotFoundExceptionWhenCreatingCitaWithNonExistentMedico() {
        // Given
        CreateCitaRequestDto request = new CreateCitaRequestDto();
        request.setMedicoId(2L);
        request.setFechaHora(ZonedDateTime.now().plusDays(1));

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(testPaciente));
        when(medicoRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            citaService.createCita(request, 1L);
        });
        
        verify(pacienteRepository, times(1)).findById(1L);
        verify(medicoRepository, times(1)).findById(2L);
        verify(citaRepository, never()).save(any(Cita.class));
        verify(eventPublisher, never()).publishEvent(any(CitaCreadaEvent.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when creating cita with medico without valid price")
    void shouldThrowIllegalStateExceptionWhenCreatingCitaWithMedicoWithoutValidPrice() {
        // Given
        CreateCitaRequestDto request = new CreateCitaRequestDto();
        request.setMedicoId(2L);
        request.setFechaHora(ZonedDateTime.now().plusDays(1));

        Medico medicoWithoutPrice = new Medico();
        medicoWithoutPrice.setId(2L);
        medicoWithoutPrice.setNombres("Dr. Carlos");
        medicoWithoutPrice.setApellidos("García");
        medicoWithoutPrice.setEmail("carlos@test.com");
        medicoWithoutPrice.setRol(Rol.MEDICO);
        medicoWithoutPrice.setPrecioConsulta(BigDecimal.ZERO); // Invalid price

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(testPaciente));
        when(medicoRepository.findById(2L)).thenReturn(Optional.of(medicoWithoutPrice));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            citaService.createCita(request, 1L);
        });
        
        verify(pacienteRepository, times(1)).findById(1L);
        verify(medicoRepository, times(1)).findById(2L);
        verify(citaRepository, never()).save(any(Cita.class));
        verify(eventPublisher, never()).publishEvent(any(CitaCreadaEvent.class));
    }

    @Test
    @DisplayName("Should cancel cita when user is paciente in cita and cita is pending")
    void shouldCancelCitaWhenUserIsPacienteInCitaAndCitaIsPending() {
        // Given
        testCita.setEstado(EstadoCita.PENDIENTE);
        testCita.setEsPagada(false);
        testUsuarioPaciente.setId(1L); // Same as paciente in cita

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));
        when(citaRepository.save(testCita)).thenReturn(testCita);

        // When
        Cita result = citaService.cancelCita(1L, testUsuarioPaciente);

        // Then
        assertNotNull(result);
        assertEquals(EstadoCita.CANCELADA, result.getEstado());
        verify(citaRepository, times(1)).findById(1L);
        verify(citaRepository, times(1)).save(testCita);
    }

    @Test
    @DisplayName("Should cancel cita when user is medico in cita and cita is confirmed")
    void shouldCancelCitaWhenUserIsMedicoInCitaAndCitaIsConfirmed() {
        // Given
        testCita.setEstado(EstadoCita.CONFIRMADA);
        testCita.setEsPagada(false);
        testUsuarioMedico.setId(2L); // Same as medico in cita

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));
        when(citaRepository.save(testCita)).thenReturn(testCita);

        // When
        Cita result = citaService.cancelCita(1L, testUsuarioMedico);

        // Then
        assertNotNull(result);
        assertEquals(EstadoCita.CANCELADA, result.getEstado());
        verify(citaRepository, times(1)).findById(1L);
        verify(citaRepository, times(1)).save(testCita);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user is not in cita")
    void shouldThrowAccessDeniedExceptionWhenUserIsNotInCita() {
        // Given
        Usuario unauthorizedUser = new Usuario();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setRol(Rol.PACIENTE);

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));

        // When & Then
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            citaService.cancelCita(1L, unauthorizedUser);
        });
        
        verify(citaRepository, times(1)).findById(1L);
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when canceling completed cita")
    void shouldThrowIllegalStateExceptionWhenCancelingCompletedCita() {
        // Given
        testCita.setEstado(EstadoCita.COMPLETADA);
        testUsuarioPaciente.setId(1L); // Same as paciente in cita

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            citaService.cancelCita(1L, testUsuarioPaciente);
        });
        
        verify(citaRepository, times(1)).findById(1L);
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when canceling paid cita")
    void shouldThrowIllegalStateExceptionWhenCancelingPaidCita() {
        // Given
        testCita.setEstado(EstadoCita.CONFIRMADA);
        testCita.setEsPagada(true); // Paid cita
        testUsuarioPaciente.setId(1L); // Same as paciente in cita

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            citaService.cancelCita(1L, testUsuarioPaciente);
        });
        
        verify(citaRepository, times(1)).findById(1L);
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Should get cita details when user is paciente in cita")
    void shouldGetCitaDetailsWhenUserIsPacienteInCita() {
        // Given
        testUsuarioPaciente.setId(1L); // Same as paciente in cita

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));

        // When
        Cita result = citaService.getCitaDetails(1L, testUsuarioPaciente);

        // Then
        assertNotNull(result);
        assertEquals(testCita, result);
        verify(citaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should get cita details when user is medico in cita")
    void shouldGetCitaDetailsWhenUserIsMedicoInCita() {
        // Given
        testUsuarioMedico.setId(2L); // Same as medico in cita

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));

        // When
        Cita result = citaService.getCitaDetails(1L, testUsuarioMedico);

        // Then
        assertNotNull(result);
        assertEquals(testCita, result);
        verify(citaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when getting details for cita user is not in")
    void shouldThrowAccessDeniedExceptionWhenGettingDetailsForCitaUserIsNotIn() {
        // Given
        Usuario unauthorizedUser = new Usuario();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setRol(Rol.PACIENTE);

        when(citaRepository.findById(1L)).thenReturn(Optional.of(testCita));

        // When & Then
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            citaService.getCitaDetails(1L, unauthorizedUser);
        });
        
        verify(citaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should find citas by paciente ID")
    void shouldFindCitasByPacienteId() {
        // Given
        when(citaRepository.findByPacienteId(1L)).thenReturn(java.util.Collections.singletonList(testCita));

        // When
        java.util.List<Cita> result = citaService.findCitasByPaciente(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCita, result.get(0));
        verify(citaRepository, times(1)).findByPacienteId(1L);
    }

    @Test
    @DisplayName("Should find citas by medico ID")
    void shouldFindCitasByMedicoId() {
        // Given
        when(citaRepository.findByMedicoId(2L)).thenReturn(java.util.Collections.singletonList(testCita));

        // When
        java.util.List<Cita> result = citaService.findCitasByMedico(2L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCita, result.get(0));
        verify(citaRepository, times(1)).findByMedicoId(2L);
    }
}