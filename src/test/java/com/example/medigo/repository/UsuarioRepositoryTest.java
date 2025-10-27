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

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Tests del Repositorio de Usuario")
class UsuarioRepositoryTest {

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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private Paciente testPaciente;

    @BeforeEach
    void setUp() {
        pacienteRepository.deleteAll();
        usuarioRepository.deleteAll();

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
    }

    @Test
    @DisplayName("Should find usuario by email when email exists")
    void shouldFindUsuarioByEmailWhenEmailExists() {
        // Given
        pacienteRepository.save(testPaciente);

        // When
        Optional<Usuario> found = usuarioRepository.findByEmail("juan.perez@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(found.get().getNombres()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        // When
        Optional<Usuario> found = usuarioRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        // Given
        pacienteRepository.save(testPaciente);

        // When
        Boolean exists = usuarioRepository.existsByEmail("juan.perez@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        Boolean exists = usuarioRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find usuario by telefono when telefono exists")
    void shouldFindUsuarioByTelefonoWhenTelefonoExists() {
        // Given
        pacienteRepository.save(testPaciente);

        // When
        Optional<Usuario> found = usuarioRepository.findByTelefono("987654321");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTelefono()).isEqualTo("987654321");
    }

    @Test
    @DisplayName("Should return empty when telefono does not exist")
    void shouldReturnEmptyWhenTelefonoDoesNotExist() {
        // When
        Optional<Usuario> found = usuarioRepository.findByTelefono("000000000");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should save usuario when valid data provided")
    void shouldSaveUsuarioWhenValidDataProvided() {
        // When
        Paciente saved = pacienteRepository.save(testPaciente);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update usuario when modify existing")
    void shouldUpdateUsuarioWhenModifyExisting() {
        // Given
        Paciente saved = pacienteRepository.save(testPaciente);
        Long id = saved.getId();

        // When
        saved.setNombres("Juan Carlos");
        pacienteRepository.save(saved);

        // Then
        Optional<Usuario> updated = usuarioRepository.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().getNombres()).isEqualTo("Juan Carlos");
    }

    @Test
    @DisplayName("Should delete usuario when delete is called")
    void shouldDeleteUsuarioWhenDeleteIsCalled() {
        // Given
        Paciente saved = pacienteRepository.save(testPaciente);

        // When
        pacienteRepository.delete(saved);

        // Then
        assertThat(usuarioRepository.count()).isEqualTo(0);
        assertThat(usuarioRepository.findById(saved.getId())).isEmpty();
    }
}
