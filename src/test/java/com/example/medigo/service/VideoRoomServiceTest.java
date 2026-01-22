package com.example.medigo.service;

import com.example.medigo.config.WherebyConfig;
import com.example.medigo.domain.*;
import com.example.medigo.dto.response.JoinVideoRoomResponseDto;
import com.example.medigo.dto.response.VideoRoomResponseDto;
import com.example.medigo.exceptions.InvalidCredentialsException;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.VideoRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests del Servicio de Salas de Video")
class VideoRoomServiceTest {

    @Mock
    private VideoRoomRepository videoRoomRepository;

    @Mock
    private CitaService citaService;

    @Mock
    private WherebyConfig wherebyConfig; // Changed from DailyConfig to WherebyConfig

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private VideoRoomService videoRoomService;

    private Cita testCita;
    private Paciente testPaciente;
    private Medico testMedico;
    private VideoRoom testVideoRoom;
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

        // Setup Cita
        testCita = new Cita();
        testCita.setId(1L);
        testCita.setPaciente(testPaciente);
        testCita.setMedico(testMedico);
        testCita.setFechaHora(ZonedDateTime.now().plusHours(1));
        testCita.setEstado(EstadoCita.CONFIRMADA); // This is important for the video room creation
        testCita.setEsPagada(true);

        // Setup VideoRoom
        testVideoRoom = VideoRoom.builder()
                .id(1L)
                .roomName("medigo-cita-1-1234567890")
                .roomUrl("https://test.whereby.com/medigo-cita-1-1234567890")
                .cita(testCita)
                .expiresAt(ZonedDateTime.now().plusHours(24))
                .status("ACTIVE")
                // Removed dailyRoomId, patientToken, and doctorToken as they're not needed for
                // Whereby
                .recordingEnabled(false)
                .build();

        // Setup Usuarios
        testUsuarioPaciente = new Usuario();
        testUsuarioPaciente.setId(1L);
        testUsuarioPaciente.setRol(Rol.PACIENTE);

        testUsuarioMedico = new Usuario();
        testUsuarioMedico.setId(2L);
        testUsuarioMedico.setRol(Rol.MEDICO);

        // Setup WherebyConfig
        when(wherebyConfig.getApiKey()).thenReturn("test-api-key");
        when(wherebyConfig.getApiUrl()).thenReturn("https://api.whereby.dev/v1");
    }

    @Test
    @DisplayName("Should create video room for cita when no existing room and cita exists")
    void shouldCreateVideoRoomForCitaWhenNoExistingRoomAndCitaExists() {
        // Given
        String wherebyApiResponse = "{\"roomName\":\"medigo-cita-1-1234567890\",\"roomUrl\":\"https://test.whereby.com/medigo-cita-1-1234567890\"}";

        // Create a cita with CONFIRMADA state and time that has already started (to
        // pass timing validation)
        Cita confirmedCita = new Cita();
        confirmedCita.setId(1L);
        confirmedCita.setPaciente(testPaciente);
        confirmedCita.setMedico(testMedico);
        confirmedCita.setFechaHora(ZonedDateTime.now().minusMinutes(10)); // Started 10 minutes ago
        confirmedCita.setEstado(EstadoCita.CONFIRMADA); // This will pass the validation
        confirmedCita.setEsPagada(true);

        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.empty());
        when(citaService.findCitaById(1L)).thenReturn(confirmedCita); // Return the confirmedCita
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(wherebyApiResponse, HttpStatus.OK));
        when(videoRoomRepository.save(any(VideoRoom.class))).thenReturn(testVideoRoom);

        // When
        VideoRoom result = videoRoomService.createVideoRoomForCita(1L);

        // Then
        assertNotNull(result);
        assertEquals("medigo-cita-1-1234567890", result.getRoomName());
        assertEquals("ACTIVE", result.getStatus());
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
        verify(citaService, times(1)).findCitaById(1L);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
        verify(videoRoomRepository, times(1)).save(any(VideoRoom.class));
    }

    @Test
    @DisplayName("Should return existing video room when already exists and not expired")
    void shouldReturnExistingVideoRoomWhenAlreadyExistsAndNotExpired() {
        // Given
        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When
        VideoRoom result = videoRoomService.createVideoRoomForCita(1L);

        // Then
        assertNotNull(result);
        assertEquals(testVideoRoom, result);
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
        // Note: The method returns early when a room exists, so these are never called
        verify(citaService, never()).findCitaById(anyLong());
        verify(restTemplate, never()).exchange(anyString(), any(), any(), (Class<?>) any());
        verify(videoRoomRepository, never()).save(any(VideoRoom.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when existing room is expired")
    void shouldThrowIllegalStateExceptionWhenExistingRoomIsExpired() {
        // Given
        VideoRoom expiredRoom = VideoRoom.builder()
                .id(1L)
                .roomName("medigo-cita-1-1234567890")
                .roomUrl("https://test.whereby.com/medigo-cita-1-1234567890")
                .cita(testCita)
                .expiresAt(ZonedDateTime.now().minusHours(1)) // Expired
                .status("ACTIVE")
                // Removed dailyRoomId, patientToken, and doctorToken as they're not needed for
                // Whereby
                .recordingEnabled(false)
                .build();

        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(expiredRoom));
        when(videoRoomRepository.save(expiredRoom)).thenAnswer(invocation -> {
            VideoRoom room = invocation.getArgument(0);
            room.setStatus("EXPIRED"); // Simulate the status change in the service
            return room;
        });

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            videoRoomService.createVideoRoomForCita(1L);
        });

        // The method throws an exception before reaching the repository calls for the
        // new room creation
        // So we only verify the calls that were actually made
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
        verify(videoRoomRepository, times(1)).save(expiredRoom);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when video room does not exist for cita")
    void shouldThrowResourceNotFoundExceptionWhenVideoRoomDoesNotExistForCita() {
        // Given
        when(videoRoomRepository.findByCitaId(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            videoRoomService.getVideoRoomByCitaId(999L);
        });

        verify(videoRoomRepository, times(1)).findByCitaId(999L);
    }

    @Test
    @DisplayName("Should get video room by cita ID when exists and not expired")
    void shouldGetVideoRoomByCitaIdWhenExistsAndNotExpired() {
        // Given
        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When
        VideoRoom result = videoRoomService.getVideoRoomByCitaId(1L);

        // Then
        assertNotNull(result);
        assertEquals(testVideoRoom, result);
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when getting expired video room")
    void shouldThrowIllegalStateExceptionWhenGettingExpiredVideoRoom() {
        // Given
        VideoRoom expiredRoom = VideoRoom.builder()
                .id(1L)
                .roomName("medigo-cita-1-1234567890")
                .roomUrl("https://test.whereby.com/medigo-cita-1-1234567890")
                .cita(testCita)
                .expiresAt(ZonedDateTime.now().minusHours(1)) // Expired
                .status("ACTIVE")
                // Removed dailyRoomId, patientToken, and doctorToken as they're not needed for
                // Whereby
                .recordingEnabled(false)
                .build();

        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(expiredRoom));
        when(videoRoomRepository.save(expiredRoom)).thenAnswer(invocation -> {
            VideoRoom room = invocation.getArgument(0);
            room.setStatus("EXPIRED"); // Simulate the status change in the service
            return room;
        });

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            videoRoomService.getVideoRoomByCitaId(1L);
        });

        verify(videoRoomRepository, times(1)).findByCitaId(1L);
        verify(videoRoomRepository, times(1)).save(expiredRoom);
    }

    @Test
    @DisplayName("Should return null token when user is paciente in cita (Whereby doesn't need separate tokens)")
    void shouldReturnNullTokenWhenUserIsPacienteInCita() {
        // Given
        testUsuarioPaciente.setId(1L); // Same as paciente in cita
        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When
        String result = videoRoomService.getAccessToken(1L, testUsuarioPaciente);

        // Then
        assertNull(result); // Whereby doesn't need separate tokens
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should return null token when user is medico in cita (Whereby doesn't need separate tokens)")
    void shouldReturnNullTokenWhenUserIsMedicoInCita() {
        // Given
        testUsuarioMedico.setId(2L); // Same as medico in cita
        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When
        String result = videoRoomService.getAccessToken(1L, testUsuarioMedico);

        // Then
        assertNull(result); // Whereby doesn't need separate tokens
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user is not in cita")
    void shouldThrowInvalidCredentialsExceptionWhenUserIsNotInCita() {
        // Given
        Usuario unauthorizedUser = new Usuario();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setRol(Rol.PACIENTE);
        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            videoRoomService.getAccessToken(1L, unauthorizedUser);
        });

        verify(videoRoomRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should create video room response DTO when video room exists")
    void shouldCreateVideoRoomResponseDtoWhenVideoRoomExists() {
        // Given
        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When
        VideoRoomResponseDto result = videoRoomService.createVideoRoomResponseDto(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("medigo-cita-1-1234567890", result.getRoomName());
        assertEquals(1L, result.getCitaId());
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should create join response DTO for paciente when video room exists")
    void shouldCreateJoinResponseDtoForPacienteWhenVideoRoomExists() {
        // Given
        testUsuarioPaciente.setId(1L); // Same as paciente in cita

        // Update the cita's fechaHora to be in the past (within the 1-hour window)
        testCita.setFechaHora(ZonedDateTime.now().minusMinutes(10));

        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When
        JoinVideoRoomResponseDto result = videoRoomService.createJoinResponseDto(1L, testUsuarioPaciente);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("https://test.whereby.com/medigo-cita-1-1234567890", result.getRoomUrl());
        assertNull(result.getToken()); // Whereby doesn't need tokens
        assertFalse(result.getIsDoctor());
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should create join response DTO for medico when video room exists")
    void shouldCreateJoinResponseDtoForMedicoWhenVideoRoomExists() {
        // Given
        testUsuarioMedico.setId(2L); // Same as medico in cita

        // Update the cita's fechaHora to be in the past (within the 1-hour window)
        testCita.setFechaHora(ZonedDateTime.now().minusMinutes(10));

        when(videoRoomRepository.findByCitaId(1L)).thenReturn(Optional.of(testVideoRoom));

        // When
        JoinVideoRoomResponseDto result = videoRoomService.createJoinResponseDto(1L, testUsuarioMedico);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("https://test.whereby.com/medigo-cita-1-1234567890", result.getRoomUrl());
        assertNull(result.getToken()); // Whereby doesn't need tokens
        assertTrue(result.getIsDoctor());
        verify(videoRoomRepository, times(1)).findByCitaId(1L);
    }

    @Test
    @DisplayName("Should create error join response DTO")
    void shouldCreateErrorJoinResponseDto() {
        // When
        JoinVideoRoomResponseDto result = videoRoomService.createErrorJoinResponseDto("Test error message");

        // Then
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("Test error message", result.getMessage());
    }
}