package com.example.medigo.repository;

import com.example.medigo.domain.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Boolean existsByDni(String dni);
    Boolean existsByNumeroColegiado(String numeroColegiado);
    
    // Búsqueda por nombre o apellido (parcial, ignora mayúsculas/minúsculas)
    Page<Medico> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String nombres, String apellidos, Pageable pageable);
    
    // Búsqueda exacta por email
    Optional<Medico> findByEmail(String email);
    
    // Filtrar por especialidad
    Page<Medico> findByEspecialidadesId(Long especialidadId, Pageable pageable);
    
    // Filtrar por rango de precios
    Page<Medico> findByPrecioConsultaBetween(BigDecimal minPrecio, BigDecimal maxPrecio, Pageable pageable);

}
