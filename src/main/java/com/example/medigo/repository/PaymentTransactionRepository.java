package com.example.medigo.repository;

import com.example.medigo.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByStripeSessionId(String stripeSessionId);

    List<PaymentTransaction> findByPacienteId(Long pacienteId);

    List<PaymentTransaction> findByMedicoId(Long medicoId);

    List<PaymentTransaction> findByCitaId(Long citaId);
}