package com.example.medigo.repository;

import com.example.medigo.domain.Cita;
import com.example.medigo.domain.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPacienteId(Long pacienteId);

    List<Cita> findByMedicoId(Long medicoId);

    List<Cita> findByEstado(EstadoCita estado);

    Optional<Cita> findByStripeSessionId(String stripeSessionId);

    List<Cita> findByPacienteIdAndEstado(Long pacienteId, EstadoCita estado);

    List<Cita> findByMedicoIdAndEstado(Long medicoId, EstadoCita estado);
}
