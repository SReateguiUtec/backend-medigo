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

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Tests del Repositorio de Paciente")
class PacienteRepositoryTest {

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
    private PacienteRepository pacienteRepository;

    private Paciente testPaciente;

    @BeforeEach
    void setUp() {
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
                .fechaNacimiento(LocalDate.of(1994, 1, 15))
                .build();
    }

    @Test
    @DisplayName("Should save paciente when valid data provided")
    void shouldSavePacienteWhenValidDataProvided() {
        // When
        Paciente saved = pacienteRepository.save(testPaciente);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDni()).isEqualTo("12345678");
        assertThat(saved.getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(pacienteRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        // Given
        pacienteRepository.save(testPaciente);

        // When
        Boolean exists = pacienteRepository.existsByEmail("juan.perez@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        Boolean exists = pacienteRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when dni exists")
    void shouldReturnTrueWhenDniExists() {
        // Given
        pacienteRepository.save(testPaciente);

        // When
        Boolean exists = pacienteRepository.existsByDni("12345678");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when dni does not exist")
    void shouldReturnFalseWhenDniDoesNotExist() {
        // When
        Boolean exists = pacienteRepository.existsByDni("00000000");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should update paciente when modify existing")
    void shouldUpdatePacienteWhenModifyExisting() {
        // Given
        Paciente saved = pacienteRepository.save(testPaciente);

        // When
        saved.setNombres("Juan Carlos");
        saved.setEdad(31);
        pacienteRepository.save(saved);

        // Then
        Paciente updated = pacienteRepository.findById(saved.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getNombres()).isEqualTo("Juan Carlos");
        assertThat(updated.getEdad()).isEqualTo(31);
    }

    @Test
    @DisplayName("Should delete paciente when delete is called")
    void shouldDeletePacienteWhenDeleteIsCalled() {
        // Given
        Paciente saved = pacienteRepository.save(testPaciente);
        Long id = saved.getId();

        // When
        pacienteRepository.delete(saved);

        // Then
        assertThat(pacienteRepository.count()).isEqualTo(0);
        assertThat(pacienteRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple pacientes with different emails")
    void shouldHandleMultiplePacientesWithDifferentEmails() {
        // Given
        pacienteRepository.save(testPaciente);

        Paciente paciente2 = Paciente.builder()
                .nombres("María")
                .apellidos("García")
                .email("maria.garcia@example.com")
                .password("pass123")
                .edad(25)
                .telefono("987654322")
                .rol(Rol.PACIENTE)
                .estadoCuenta(EstadoCuenta.ACTIVADA)
                .createdAt(ZonedDateTime.now())
                .dni("87654321")
                .build();
        pacienteRepository.save(paciente2);

        // When
        Boolean exists1 = pacienteRepository.existsByEmail("juan.perez@example.com");
        Boolean exists2 = pacienteRepository.existsByEmail("maria.garcia@example.com");

        // Then
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
        assertThat(pacienteRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle multiple pacientes with different dni")
    void shouldHandleMultiplePacientesWithDifferentDni() {
        // Given
        pacienteRepository.save(testPaciente);

        Paciente paciente2 = Paciente.builder()
                .nombres("Pedro")
                .apellidos("Sánchez")
                .email("pedro.sanchez@example.com")
                .password("pass123")
                .edad(35)
                .telefono("987654323")
                .rol(Rol.PACIENTE)
                .estadoCuenta(EstadoCuenta.ACTIVADA)
                .createdAt(ZonedDateTime.now())
                .dni("11111111")
                .build();
        pacienteRepository.save(paciente2);

        // When
        Boolean exists1 = pacienteRepository.existsByDni("12345678");
        Boolean exists2 = pacienteRepository.existsByDni("11111111");

        // Then
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
    }

    @Test
    @DisplayName("Should maintain unique constraint on email")
    void shouldMaintainUniqueConstraintOnEmail() {
        // Given
        pacienteRepository.save(testPaciente);

        // Then - should only have one with that email
        assertThat(pacienteRepository.existsByEmail("juan.perez@example.com")).isTrue();
    }

    @Test
    @DisplayName("Should maintain fecha nacimiento when provided")
    void shouldMaintainFechaNacimientoWhenProvided() {
        // Given
        testPaciente.setFechaNacimiento(LocalDate.of(1990, 5, 20));

        // When
        Paciente saved = pacienteRepository.save(testPaciente);

        // Then
        assertThat(saved.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 5, 20));
    }
}

