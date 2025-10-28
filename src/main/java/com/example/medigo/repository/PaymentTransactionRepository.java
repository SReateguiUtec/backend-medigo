package com.example.medigo.repository;

import com.example.medigo.domain.PaymentTransaction;
import com.example.medigo.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByStripeSessionId(String stripeSessionId);

    List<PaymentTransaction> findByPacienteId(Long pacienteId);

    List<PaymentTransaction> findByMedicoId(Long medicoId);

    List<PaymentTransaction> findByCitaId(Long citaId);
    
    List<PaymentTransaction> findByPaymentStatusAndPaidAtBefore(PaymentStatus paymentStatus, ZonedDateTime paidAt);
    
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentStatus = com.example.medigo.domain.PaymentStatus.PAID AND " +
           "(pt.updatedAt <= :cutoffDate OR pt.paidAt <= :cutoffDate)")
    List<PaymentTransaction> findPaidTransactionsEligibleForPayout(@Param("cutoffDate") ZonedDateTime cutoffDate);
}