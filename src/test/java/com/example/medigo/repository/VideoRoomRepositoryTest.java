package com.example.medigo.repository;

import com.example.medigo.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Tests del Repositorio de VideoRoom")
class VideoRoomRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private VideoRoomRepository videoRoomRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    private Paciente testPaciente;
    private Medico testMedico;
    private Cita testCita;
    private VideoRoom testVideoRoom;

    @BeforeEach
    void setUp() {
        videoRoomRepository.deleteAll();
        citaRepository.deleteAll();
        medicoRepository.deleteAll();
        pacienteRepository.deleteAll();

        testPaciente = Paciente.builder()
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan.perez@example.com")
                .password("password123")
                .edad(30)
                .telefono("987654321")
                .rol(Rol.PACIENTE)
                .estadoCuenta(EstadoCuenta.ACTIVADA)
                .createdAt(ZonedDateTime.now())
                .dni("12345678")
                .build();

        testMedico = Medico.builder()
                .nombres("Dr. Carlos")
                .apellidos("García")
                .email("carlos.garcia@example.com")
                .password("password123")
                .edad(45)
                .telefono("987654322")
                .rol(Rol.MEDICO)
                .estadoCuenta(EstadoCuenta.ACTIVADA)
                .createdAt(ZonedDateTime.now())
                .dni("87654321")
                .numeroColegiado("CO12345")
                .build();

        testPaciente = pacienteRepository.save(testPaciente);
        testMedico = medicoRepository.save(testMedico);

        testCita = Cita.builder()
                .paciente(testPaciente)
                .medico(testMedico)
                .fechaHora(ZonedDateTime.now().plusDays(1))
                .estado(EstadoCita.CONFIRMADA)
                .precioConsulta(new BigDecimal("50.00"))
                .esPagada(true)
                .build();

        testCita = citaRepository.save(testCita);

        testVideoRoom = VideoRoom.builder()
                .roomName("medigo-cita-1-test")
                .roomUrl("https://test-domain.daily.co/medigo-cita-1-test")
                .cita(testCita)
                .createdAt(ZonedDateTime.now())
                .expiresAt(ZonedDateTime.now().plusDays(1))
                .status("ACTIVE")
                .dailyRoomId("test-room-123")
                .patientToken("test-patient-token")
                .doctorToken("test-doctor-token")
                .recordingEnabled(true)
                .build();
    }

    @Test
    @DisplayName("Should save video room when valid data provided")
    void shouldSaveVideoRoomWhenValidDataProvided() {
        // When
        VideoRoom saved = videoRoomRepository.save(testVideoRoom);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRoomName()).isEqualTo("medigo-cita-1-test");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(videoRoomRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find video room by room name when name exists")
    void shouldFindVideoRoomByRoomNameWhenNameExists() {
        // Given
        videoRoomRepository.save(testVideoRoom);

        // When
        Optional<VideoRoom> found = videoRoomRepository.findByRoomName("medigo-cita-1-test");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRoomName()).isEqualTo("medigo-cita-1-test");
    }

    @Test
    @DisplayName("Should return empty when room name does not exist")
    void shouldReturnEmptyWhenRoomNameDoesNotExist() {
        // When
        Optional<VideoRoom> found = videoRoomRepository.findByRoomName("nonexistent-room");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find video room by cita id when cita exists")
    void shouldFindVideoRoomByCitaIdWhenCitaExists() {
        // Given
        videoRoomRepository.save(testVideoRoom);

        // When
        Optional<VideoRoom> found = videoRoomRepository.findByCitaId(testCita.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCita().getId()).isEqualTo(testCita.getId());
    }

    @Test
    @DisplayName("Should return empty when cita does not exist")
    void shouldReturnEmptyWhenCitaDoesNotExist() {
        // When
        Optional<VideoRoom> found = videoRoomRepository.findByCitaId(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update video room when modify existing")
    void shouldUpdateVideoRoomWhenModifyExisting() {
        // Given
        VideoRoom saved = videoRoomRepository.save(testVideoRoom);

        // When
        saved.setStatus("EXPIRED");
        saved.setRecordingEnabled(false);
        videoRoomRepository.save(saved);

        // Then
        Optional<VideoRoom> updated = videoRoomRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo("EXPIRED");
        assertThat(updated.get().getRecordingEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should delete video room when delete is called")
    void shouldDeleteVideoRoomWhenDeleteIsCalled() {
        // Given
        VideoRoom saved = videoRoomRepository.save(testVideoRoom);

        // When
        videoRoomRepository.delete(saved);

        // Then
        assertThat(videoRoomRepository.count()).isEqualTo(0);
        assertThat(videoRoomRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should maintain relationship with cita when saved")
    void shouldMaintainRelationshipWithCitaWhenSaved() {
        // Given
        videoRoomRepository.save(testVideoRoom);

        // When
        Optional<VideoRoom> found = videoRoomRepository.findByCitaId(testCita.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCita()).isNotNull();
        assertThat(found.get().getCita().getId()).isEqualTo(testCita.getId());
    }

    @Test
    @DisplayName("Should handle multiple video rooms for different citas")
    void shouldHandleMultipleVideoRoomsForDifferentCitas() {
        // Given
        videoRoomRepository.save(testVideoRoom);

        Cita cita2 = Cita.builder()
                .paciente(testPaciente)
                .medico(testMedico)
                .fechaHora(ZonedDateTime.now().plusDays(2))
                .estado(EstadoCita.CONFIRMADA)
                .precioConsulta(new BigDecimal("60.00"))
                .esPagada(true)
                .build();
        cita2 = citaRepository.save(cita2);

        VideoRoom videoRoom2 = VideoRoom.builder()
                .roomName("medigo-cita-2-test")
                .roomUrl("https://test-domain.daily.co/medigo-cita-2-test")
                .cita(cita2)
                .createdAt(ZonedDateTime.now())
                .expiresAt(ZonedDateTime.now().plusDays(1))
                .status("ACTIVE")
                .dailyRoomId("test-room-456")
                .patientToken("test-patient-token-2")
                .doctorToken("test-doctor-token-2")
                .recordingEnabled(true)
                .build();
        videoRoomRepository.save(videoRoom2);

        // When
        Optional<VideoRoom> found1 = videoRoomRepository.findByCitaId(testCita.getId());
        Optional<VideoRoom> found2 = videoRoomRepository.findByCitaId(cita2.getId());

        // Then
        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found1.get().getRoomName()).isEqualTo("medigo-cita-1-test");
        assertThat(found2.get().getRoomName()).isEqualTo("medigo-cita-2-test");
        assertThat(videoRoomRepository.count()).isEqualTo(2);
    }
}

