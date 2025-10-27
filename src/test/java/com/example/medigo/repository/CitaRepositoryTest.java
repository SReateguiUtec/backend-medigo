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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Tests del Repositorio de Cita")
class CitaRepositoryTest {

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
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    private Paciente testPaciente;
    private Medico testMedico;
    private Cita testCita;

    @BeforeEach
    void setUp() {
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
                .estado(EstadoCita.PENDIENTE)
                .precioConsulta(new BigDecimal("50.00"))
                .esPagada(false)
                .build();
    }

    @Test
    @DisplayName("Should save cita when valid data provided")
    void shouldSaveCitaWhenValidDataProvided() {
        // When
        Cita saved = citaRepository.save(testCita);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPaciente().getId()).isEqualTo(testPaciente.getId());
        assertThat(saved.getMedico().getId()).isEqualTo(testMedico.getId());
        assertThat(citaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find citas by paciente when paciente has citas")
    void shouldFindCitasByPacienteWhenPacienteHasCitas() {
        // Given
        citaRepository.save(testCita);
        
        // Create another cita for the same patient
        Cita cita2 = Cita.builder()
                .paciente(testPaciente)
                .medico(testMedico)
                .fechaHora(ZonedDateTime.now().plusDays(2))
                .estado(EstadoCita.CONFIRMADA)
                .precioConsulta(new BigDecimal("50.00"))
                .esPagada(true)
                .build();
        citaRepository.save(cita2);

        // When
        List<Cita> citas = citaRepository.findByPacienteId(testPaciente.getId());

        // Then
        assertThat(citas).hasSize(2);
        assertThat(citas).extracting(Cita::getPaciente)
                .allMatch(p -> p.getId().equals(testPaciente.getId()));
    }

    @Test
    @DisplayName("Should find citas by medico when medico has citas")
    void shouldFindCitasByMedicoWhenMedicoHasCitas() {
        // Given
        citaRepository.save(testCita);

        // When
        List<Cita> citas = citaRepository.findByMedicoId(testMedico.getId());

        // Then
        assertThat(citas).hasSize(1);
        assertThat(citas.get(0).getMedico().getId()).isEqualTo(testMedico.getId());
    }

    @Test
    @DisplayName("Should find citas by estado when estado matches")
    void shouldFindCitasByEstadoWhenEstadoMatches() {
        // Given
        citaRepository.save(testCita);
        
        Cita citaConfirmada = Cita.builder()
                .paciente(testPaciente)
                .medico(testMedico)
                .fechaHora(ZonedDateTime.now().plusDays(3))
                .estado(EstadoCita.CONFIRMADA)
                .precioConsulta(new BigDecimal("50.00"))
                .esPagada(true)
                .build();
        citaRepository.save(citaConfirmada);

        // When
        List<Cita> citasPendientes = citaRepository.findByEstado(EstadoCita.PENDIENTE);
        List<Cita> citasConfirmadas = citaRepository.findByEstado(EstadoCita.CONFIRMADA);

        // Then
        assertThat(citasPendientes).hasSize(1);
        assertThat(citasConfirmadas).hasSize(1);
        assertThat(citasPendientes.get(0).getEstado()).isEqualTo(EstadoCita.PENDIENTE);
        assertThat(citasConfirmadas.get(0).getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
    }

    @Test
    @DisplayName("Should find cita by stripe session id when session id exists")
    void shouldFindCitaByStripeSessionIdWhenSessionIdExists() {
        // Given
        testCita.setStripeSessionId("cs_test_12345");
        citaRepository.save(testCita);

        // When
        Optional<Cita> found = citaRepository.findByStripeSessionId("cs_test_12345");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStripeSessionId()).isEqualTo("cs_test_12345");
    }

    @Test
    @DisplayName("Should find citas by paciente and estado when both match")
    void shouldFindCitasByPacienteAndEstadoWhenBothMatch() {
        // Given
        citaRepository.save(testCita);
        
        Cita citaConfirmada = Cita.builder()
                .paciente(testPaciente)
                .medico(testMedico)
                .fechaHora(ZonedDateTime.now().plusDays(3))
                .estado(EstadoCita.CONFIRMADA)
                .precioConsulta(new BigDecimal("50.00"))
                .esPagada(true)
                .build();
        citaRepository.save(citaConfirmada);

        // When
        List<Cita> citas = citaRepository.findByPacienteIdAndEstado(
                testPaciente.getId(), EstadoCita.PENDIENTE);

        // Then
        assertThat(citas).hasSize(1);
        assertThat(citas.get(0).getEstado()).isEqualTo(EstadoCita.PENDIENTE);
        assertThat(citas.get(0).getPaciente().getId()).isEqualTo(testPaciente.getId());
    }

    @Test
    @DisplayName("Should find citas by medico and estado when both match")
    void shouldFindCitasByMedicoAndEstadoWhenBothMatch() {
        // Given
        citaRepository.save(testCita);

        // When
        List<Cita> citas = citaRepository.findByMedicoIdAndEstado(
                testMedico.getId(), EstadoCita.PENDIENTE);

        // Then
        assertThat(citas).hasSize(1);
        assertThat(citas.get(0).getMedico().getId()).isEqualTo(testMedico.getId());
        assertThat(citas.get(0).getEstado()).isEqualTo(EstadoCita.PENDIENTE);
    }

    @Test
    @DisplayName("Should return empty list when paciente has no citas")
    void shouldReturnEmptyListWhenPacienteHasNoCitas() {
        // Given - no citas saved

        // When
        List<Cita> citas = citaRepository.findByPacienteId(testPaciente.getId());

        // Then
        assertThat(citas).isEmpty();
    }

    @Test
    @DisplayName("Should update cita when modify existing")
    void shouldUpdateCitaWhenModifyExisting() {
        // Given
        Cita saved = citaRepository.save(testCita);
        
        // When
        saved.setEstado(EstadoCita.CONFIRMADA);
        saved.setEsPagada(true);
        citaRepository.save(saved);

        // Then
        Optional<Cita> updated = citaRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
        assertThat(updated.get().getEsPagada()).isTrue();
    }

    @Test
    @DisplayName("Should delete cita when delete is called")
    void shouldDeleteCitaWhenDeleteIsCalled() {
        // Given
        Cita saved = citaRepository.save(testCita);

        // When
        citaRepository.delete(saved);

        // Then
        assertThat(citaRepository.count()).isEqualTo(0);
        assertThat(citaRepository.findById(saved.getId())).isEmpty();
    }
}
