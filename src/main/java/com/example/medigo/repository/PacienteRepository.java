package com.example.medigo.repository;

import com.example.medigo.domain.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Boolean existsByEmail(String email);
    Boolean existsByDni(String dni);
}
