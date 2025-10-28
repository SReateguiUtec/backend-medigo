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
@DisplayName("Tests del Repositorio de PaymentTransaction")
class PaymentTransactionRepositoryTest {

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
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    private Paciente testPaciente;
    private Medico testMedico;
    private Cita testCita;
    private PaymentTransaction testTransaction;

    @BeforeEach
    void setUp() {
        paymentTransactionRepository.deleteAll();
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
                .stripeSessionId("cs_test_12345")
                .build();

        testCita = citaRepository.save(testCita);

        testTransaction = PaymentTransaction.builder()
                .stripeSessionId("cs_test_12345")
                .stripePaymentIntentId("pi_test_12345")
                .cita(testCita)
                .paciente(testPaciente)
                .medico(testMedico)
                .amountTotal(new BigDecimal("50.00"))
                .platformCommission(new BigDecimal("2.50"))
                .medicoAmount(new BigDecimal("47.50"))
                .currency("PEN")
                .paymentStatus(PaymentStatus.PAID)
                .stripeStatus("succeeded")
                .metadata("{\"test\": true}")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save payment transaction when valid data provided")
    void shouldSavePaymentTransactionWhenValidDataProvided() {
        // When
        PaymentTransaction saved = paymentTransactionRepository.save(testTransaction);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStripeSessionId()).isEqualTo("cs_test_12345");
        assertThat(saved.getAmountTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(paymentTransactionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find payment transaction by stripe session id when session id exists")
    void shouldFindPaymentTransactionByStripeSessionIdWhenSessionIdExists() {
        // Given
        paymentTransactionRepository.save(testTransaction);

        // When
        Optional<PaymentTransaction> found = paymentTransactionRepository.findByStripeSessionId("cs_test_12345");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStripeSessionId()).isEqualTo("cs_test_12345");
    }

    @Test
    @DisplayName("Should return empty when stripe session id does not exist")
    void shouldReturnEmptyWhenStripeSessionIdDoesNotExist() {
        // When
        Optional<PaymentTransaction> found = paymentTransactionRepository.findByStripeSessionId("cs_nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find payment transactions by paciente when paciente has transactions")
    void shouldFindPaymentTransactionsByPacienteWhenPacienteHasTransactions() {
        // Given
        paymentTransactionRepository.save(testTransaction);

        // When
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByPacienteId(testPaciente.getId());

        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getPaciente().getId()).isEqualTo(testPaciente.getId());
    }

    @Test
    @DisplayName("Should find payment transactions by medico when medico has transactions")
    void shouldFindPaymentTransactionsByMedicoWhenMedicoHasTransactions() {
        // Given
        paymentTransactionRepository.save(testTransaction);

        // When
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByMedicoId(testMedico.getId());

        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getMedico().getId()).isEqualTo(testMedico.getId());
    }

    @Test
    @DisplayName("Should find payment transactions by cita when cita has transaction")
    void shouldFindPaymentTransactionsByCitaWhenCitaHasTransaction() {
        // Given
        paymentTransactionRepository.save(testTransaction);

        // When
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByCitaId(testCita.getId());

        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getCita().getId()).isEqualTo(testCita.getId());
    }

    @Test
    @DisplayName("Should return empty list when paciente has no transactions")
    void shouldReturnEmptyListWhenPacienteHasNoTransactions() {
        // When
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByPacienteId(testPaciente.getId());

        // Then
        assertThat(transactions).isEmpty();
    }

    @Test
    @DisplayName("Should update payment transaction when modify existing")
    void shouldUpdatePaymentTransactionWhenModifyExisting() {
        // Given
        PaymentTransaction saved = paymentTransactionRepository.save(testTransaction);

        // When
        saved.setPaymentStatus(PaymentStatus.REFUNDED);
        saved.setStripeStatus("refunded");
        saved.setUpdatedAt(ZonedDateTime.now());
        paymentTransactionRepository.save(saved);

        // Then
        Optional<PaymentTransaction> updated = paymentTransactionRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(updated.get().getStripeStatus()).isEqualTo("refunded");
    }

    @Test
    @DisplayName("Should delete payment transaction when delete is called")
    void shouldDeletePaymentTransactionWhenDeleteIsCalled() {
        // Given
        PaymentTransaction saved = paymentTransactionRepository.save(testTransaction);
        Long id = saved.getId();

        // When
        paymentTransactionRepository.delete(saved);

        // Then
        assertThat(paymentTransactionRepository.count()).isEqualTo(0);
        assertThat(paymentTransactionRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should maintain relationship with cita when saved")
    void shouldMaintainRelationshipWithCitaWhenSaved() {
        // Given
        paymentTransactionRepository.save(testTransaction);

        // When
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByCitaId(testCita.getId());

        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getCita()).isNotNull();
        assertThat(transactions.get(0).getCita().getId()).isEqualTo(testCita.getId());
    }

    @Test
    @DisplayName("Should calculate platform commission correctly when provided")
    void shouldCalculatePlatformCommissionCorrectlyWhenProvided() {
        // Given
        BigDecimal totalAmount = new BigDecimal("100.00");
        BigDecimal commission = new BigDecimal("5.00");
        BigDecimal medicoAmount = new BigDecimal("95.00");

        testTransaction.setAmountTotal(totalAmount);
        testTransaction.setPlatformCommission(commission);
        testTransaction.setMedicoAmount(medicoAmount);

        // When
        PaymentTransaction saved = paymentTransactionRepository.save(testTransaction);

        // Then
        assertThat(saved.getPlatformCommission()).isEqualByComparingTo(commission);
        assertThat(saved.getMedicoAmount()).isEqualByComparingTo(medicoAmount);
        assertThat(commission.add(medicoAmount)).isEqualTo(totalAmount);
    }

    @Test
    @DisplayName("Should handle multiple transactions for same paciente")
    void shouldHandleMultipleTransactionsForSamePaciente() {
        // Given
        paymentTransactionRepository.save(testTransaction);

        PaymentTransaction transaction2 = PaymentTransaction.builder()
                .stripeSessionId("cs_test_67890")
                .stripePaymentIntentId("pi_test_67890")
                .cita(testCita)
                .paciente(testPaciente)
                .medico(testMedico)
                .amountTotal(new BigDecimal("75.00"))
                .platformCommission(new BigDecimal("3.75"))
                .medicoAmount(new BigDecimal("71.25"))
                .currency("PEN")
                .paymentStatus(PaymentStatus.PENDING)
                .stripeStatus("processing")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
        paymentTransactionRepository.save(transaction2);

        // When
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByPacienteId(testPaciente.getId());

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(paymentTransactionRepository.count()).isEqualTo(2);
    }
}

