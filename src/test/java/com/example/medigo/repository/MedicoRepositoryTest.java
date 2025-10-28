package com.example.medigo.repository;

import com.example.medigo.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@DisplayName("Tests del Repositorio de Medico")
class MedicoRepositoryTest {

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
    private MedicoRepository medicoRepository;

    private Medico testMedico;

    @BeforeEach
    void setUp() {
        medicoRepository.deleteAll();

        testMedico = new Medico();
        testMedico.setNombres("Dr. Carlos");
        testMedico.setApellidos("García López");
        testMedico.setEmail("carlos.garcia@example.com");
        testMedico.setPassword("password123");
        testMedico.setEdad(45);
        testMedico.setTelefono("987654322");
        testMedico.setRol(Rol.MEDICO);
        testMedico.setEstadoCuenta(EstadoCuenta.ACTIVADA);
        testMedico.setCreatedAt(ZonedDateTime.now());
        testMedico.setDni("87654321");
        testMedico.setNumeroColegiado("CO12345");
    }

    @Test
    @DisplayName("Should save medico when valid data provided")
    void shouldSaveMedicoWhenValidDataProvided() {
        // When
        Medico saved = medicoRepository.save(testMedico);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDni()).isEqualTo("87654321");
        assertThat(saved.getNumeroColegiado()).isEqualTo("CO12345");
        assertThat(saved.getEmail()).isEqualTo("carlos.garcia@example.com");
        assertThat(medicoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return true when dni exists")
    void shouldReturnTrueWhenDniExists() {
        // Given
        medicoRepository.save(testMedico);

        // When
        Boolean exists = medicoRepository.existsByDni("87654321");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when dni does not exist")
    void shouldReturnFalseWhenDniDoesNotExist() {
        // When
        Boolean exists = medicoRepository.existsByDni("00000000");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when numero colegiado exists")
    void shouldReturnTrueWhenNumeroColegiadoExists() {
        // Given
        medicoRepository.save(testMedico);

        // When
        Boolean exists = medicoRepository.existsByNumeroColegiado("CO12345");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when numero colegiado does not exist")
    void shouldReturnFalseWhenNumeroColegiadoDoesNotExist() {
        // When
        Boolean exists = medicoRepository.existsByNumeroColegiado("CO99999");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find medicos by name containing ignore case")
    void shouldFindMedicosByNameContainingIgnoreCase() {
        // Given
        medicoRepository.save(testMedico);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> medicos = medicoRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                "carlos", "garcia", pageable);

        // Then
        assertThat(medicos.getContent()).hasSize(1);
        assertThat(medicos.getContent().get(0).getEmail()).isEqualTo("carlos.garcia@example.com");
    }

    @Test
    @DisplayName("Should find medico by email when email exists")
    void shouldFindMedicoByEmailWhenEmailExists() {
        // Given
        medicoRepository.save(testMedico);

        // When
        Optional<Medico> found = medicoRepository.findByEmail("carlos.garcia@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("carlos.garcia@example.com");
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        // When
        Optional<Medico> found = medicoRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find medicos by price range when prices match")
    void shouldFindMedicosByPriceRangeWhenPricesMatch() {
        // Given
        testMedico.setPrecioConsulta(new BigDecimal("100.00"));
        medicoRepository.save(testMedico);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> medicos = medicoRepository.findByPrecioConsultaBetween(
                new BigDecimal("50.00"), new BigDecimal("150.00"), pageable);

        // Then
        assertThat(medicos.getContent()).hasSize(1);
        assertThat(medicos.getContent().get(0).getPrecioConsulta()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should not find medicos outside price range")
    void shouldNotFindMedicosOutsidePriceRange() {
        // Given
        testMedico.setPrecioConsulta(new BigDecimal("100.00"));
        medicoRepository.save(testMedico);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> medicos = medicoRepository.findByPrecioConsultaBetween(
                new BigDecimal("150.00"), new BigDecimal("200.00"), pageable);

        // Then
        assertThat(medicos.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should update medico when modify existing")
    void shouldUpdateMedicoWhenModifyExisting() {
        // Given
        Medico saved = medicoRepository.save(testMedico);

        // When
        saved.setBio("Médico especialista con 20 años de experiencia");
        saved.setPrecioConsulta(new BigDecimal("120.00"));
        medicoRepository.save(saved);

        // Then
        Medico updated = medicoRepository.findById(saved.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getBio()).isEqualTo("Médico especialista con 20 años de experiencia");
        assertThat(updated.getPrecioConsulta()).isEqualTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("Should delete medico when delete is called")
    void shouldDeleteMedicoWhenDeleteIsCalled() {
        // Given
        Medico saved = medicoRepository.save(testMedico);
        Long id = saved.getId();

        // When
        medicoRepository.delete(saved);

        // Then
        assertThat(medicoRepository.count()).isEqualTo(0);
        assertThat(medicoRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple medicos with different emails")
    void shouldHandleMultipleMedicosWithDifferentEmails() {
        // Given
        medicoRepository.save(testMedico);

        Medico medico2 = new Medico();
        medico2.setNombres("Dra. María");
        medico2.setApellidos("Fernández Ruiz");
        medico2.setEmail("maria.fernandez@example.com");
        medico2.setPassword("pass123");
        medico2.setEdad(38);
        medico2.setTelefono("987654323");
        medico2.setRol(Rol.MEDICO);
        medico2.setEstadoCuenta(EstadoCuenta.ACTIVADA);
        medico2.setCreatedAt(ZonedDateTime.now());
        medico2.setDni("11111111");
        medico2.setNumeroColegiado("CO54321");
        medicoRepository.save(medico2);

        // When & Then
        Optional<Medico> found1 = medicoRepository.findByEmail("carlos.garcia@example.com");
        Optional<Medico> found2 = medicoRepository.findByEmail("maria.fernandez@example.com");

        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(medicoRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle multiple medicos with different dni")
    void shouldHandleMultipleMedicosWithDifferentDni() {
        // Given
        medicoRepository.save(testMedico);

        Medico medico2 = new Medico();
        medico2.setNombres("Dr. Pedro");
        medico2.setApellidos("Martínez Sánchez");
        medico2.setEmail("pedro.martinez@example.com");
        medico2.setPassword("pass123");
        medico2.setEdad(50);
        medico2.setTelefono("987654324");
        medico2.setRol(Rol.MEDICO);
        medico2.setEstadoCuenta(EstadoCuenta.ACTIVADA);
        medico2.setCreatedAt(ZonedDateTime.now());
        medico2.setDni("22222222");
        medico2.setNumeroColegiado("CO98765");
        medicoRepository.save(medico2);

        // When
        Boolean exists1 = medicoRepository.existsByDni("87654321");
        Boolean exists2 = medicoRepository.existsByDni("22222222");

        // Then
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple medicos with different numero colegiado")
    void shouldHandleMultipleMedicosWithDifferentNumeroColegiado() {
        // Given
        medicoRepository.save(testMedico);

        Medico medico2 = new Medico();
        medico2.setNombres("Dr. Luis");
        medico2.setApellidos("González Torres");
        medico2.setEmail("luis.gonzalez@example.com");
        medico2.setPassword("pass123");
        medico2.setEdad(42);
        medico2.setTelefono("987654325");
        medico2.setRol(Rol.MEDICO);
        medico2.setEstadoCuenta(EstadoCuenta.ACTIVADA);
        medico2.setCreatedAt(ZonedDateTime.now());
        medico2.setDni("33333333");
        medico2.setNumeroColegiado("CO11111");
        medicoRepository.save(medico2);

        // When
        Boolean exists1 = medicoRepository.existsByNumeroColegiado("CO12345");
        Boolean exists2 = medicoRepository.existsByNumeroColegiado("CO11111");

        // Then
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
    }
}