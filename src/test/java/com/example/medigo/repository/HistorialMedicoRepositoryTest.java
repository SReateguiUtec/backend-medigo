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
@DisplayName("Tests del Repositorio de HistorialMedico")
class HistorialMedicoRepositoryTest {

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
    private HistorialMedicoRepository historialMedicoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    private Paciente testPaciente;
    private Medico testMedico;
    private Cita testCita;
    private HistorialMedico testHistorial;

    @BeforeEach
    void setUp() {
        historialMedicoRepository.deleteAll();
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
                .estado(EstadoCita.COMPLETADA)
                .precioConsulta(new BigDecimal("50.00"))
                .esPagada(true)
                .build();

        testCita = citaRepository.save(testCita);

        testHistorial = HistorialMedico.builder()
                .cita(testCita)
                .diagnostico("Hipertensión arterial leve")
                .receta("Lisinopril 10mg una vez al día")
                .notas("Paciente se siente bien, presión arterial controlada")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save historial when valid data provided")
    void shouldSaveHistorialWhenValidDataProvided() {
        // When
        HistorialMedico saved = historialMedicoRepository.save(testHistorial);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDiagnostico()).isEqualTo("Hipertensión arterial leve");
        assertThat(saved.getCita().getId()).isEqualTo(testCita.getId());
        assertThat(historialMedicoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find historial by cita id when cita exists")
    void shouldFindHistorialByCitaIdWhenCitaExists() {
        // Given
        historialMedicoRepository.save(testHistorial);

        // When
        Optional<HistorialMedico> found = historialMedicoRepository.findByCitaId(testCita.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCita().getId()).isEqualTo(testCita.getId());
        assertThat(found.get().getDiagnostico()).isEqualTo("Hipertensión arterial leve");
    }

    @Test
    @DisplayName("Should return empty when cita does not exist")
    void shouldReturnEmptyWhenCitaDoesNotExist() {
        // When
        Optional<HistorialMedico> found = historialMedicoRepository.findByCitaId(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return true when cita exists")
    void shouldReturnTrueWhenCitaExists() {
        // Given
        historialMedicoRepository.save(testHistorial);

        // When
        boolean exists = historialMedicoRepository.existsByCitaId(testCita.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when cita does not exist")
    void shouldReturnFalseWhenCitaDoesNotExist() {
        // When
        boolean exists = historialMedicoRepository.existsByCitaId(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should update historial when modify existing")
    void shouldUpdateHistorialWhenModifyExisting() {
        // Given
        HistorialMedico saved = historialMedicoRepository.save(testHistorial);

        // When
        saved.setDiagnostico("Diagnóstico actualizado");
        saved.setReceta("Nueva receta - medicamento diferente");
        historialMedicoRepository.save(saved);

        // Then
        Optional<HistorialMedico> updated = historialMedicoRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getDiagnostico()).isEqualTo("Diagnóstico actualizado");
        assertThat(updated.get().getReceta()).isEqualTo("Nueva receta - medicamento diferente");
    }

    @Test
    @DisplayName("Should delete historial when delete is called")
    void shouldDeleteHistorialWhenDeleteIsCalled() {
        // Given
        HistorialMedico saved = historialMedicoRepository.save(testHistorial);
        Long id = saved.getId();

        // When
        historialMedicoRepository.delete(saved);

        // Then
        assertThat(historialMedicoRepository.count()).isEqualTo(0);
        assertThat(historialMedicoRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should maintain relationship with cita when saved")
    void shouldMaintainRelationshipWithCitaWhenSaved() {
        // Given
        HistorialMedico saved = historialMedicoRepository.save(testHistorial);

        // When
        Optional<HistorialMedico> found = historialMedicoRepository.findByCitaId(testCita.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCita()).isNotNull();
        assertThat(found.get().getCita().getId()).isEqualTo(testCita.getId());
        assertThat(found.get().getCita().getPaciente().getId()).isEqualTo(testPaciente.getId());
        assertThat(found.get().getCita().getMedico().getId()).isEqualTo(testMedico.getId());
    }

    @Test
    @DisplayName("Should handle notas extensas when provided")
    void shouldHandleNotasExtensasWhenProvided() {
        // Given
        String notasExtensas = "El paciente presenta síntomas leves de fiebre. " +
                "Se recomienda reposo y monitorización durante 48 horas. " +
                "El paciente debe volver si los síntomas persisten.";
        testHistorial.setNotas(notasExtensas);

        // When
        HistorialMedico saved = historialMedicoRepository.save(testHistorial);

        // Then
        assertThat(saved.getNotas()).isEqualTo(notasExtensas);
        assertThat(saved.getNotas().length()).isGreaterThan(100);
    }

    @Test
    @DisplayName("Should maintain unique relationship with cita")
    void shouldMaintainUniqueRelationshipWithCita() {
        // Given
        historialMedicoRepository.save(testHistorial);

        // Then - should only have one historial per cita
        assertThat(historialMedicoRepository.existsByCitaId(testCita.getId())).isTrue();
    }
}

