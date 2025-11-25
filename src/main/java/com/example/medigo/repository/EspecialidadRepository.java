package com.example.medigo.repository;

import com.example.medigo.domain.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    @Query("SELECT e FROM Especialidad e WHERE e.nombre_especialidad = :nombre")
    Optional<Especialidad> findByNombre(@Param("nombre") String nombre);
}
