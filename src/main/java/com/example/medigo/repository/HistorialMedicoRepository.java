package com.example.medigo.repository;

import com.example.medigo.domain.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {
    Optional<HistorialMedico> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);

    List<HistorialMedico> findByCitaPacienteIdOrderByCreatedAtDesc(Long pacienteId);
}
