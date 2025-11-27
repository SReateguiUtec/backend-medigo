package com.example.medigo.repository;

import com.example.medigo.domain.DiaSemana;
import com.example.medigo.domain.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {

    List<HorarioMedico> findByMedicoIdAndActivoTrue(Long medicoId);

    @Query("SELECT h FROM HorarioMedico h WHERE h.medico.id = :medicoId AND h.diaSemana = :diaSemana AND h.activo = true")
    List<HorarioMedico> findByMedicoIdAndDiaSemanaAndActivoTrue(
            @Param("medicoId") Long medicoId,
            @Param("diaSemana") DiaSemana diaSemana);

    void deleteByMedicoIdAndId(Long medicoId, Long horarioId);
}
