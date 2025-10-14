package com.example.medigo.repository;

import com.example.medigo.domain.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Boolean existsByDni(String dni);
    Boolean existsByNumeroColegiado(String numeroColegiado);
}
