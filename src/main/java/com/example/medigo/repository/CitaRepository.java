package com.example.medigo.repository;

import com.example.medigo.domain.Cita;
import com.example.medigo.domain.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
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

        // Verificar si existe una cita para un médico en una fecha/hora específica
        // (excluyendo canceladas y pendientes no pagadas)
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cita c " +
                        "WHERE c.medico.id = :medicoId " +
                        "AND c.fechaHora = :fechaHora " +
                        "AND c.estado != 'CANCELADA' " +
                        "AND (c.estado != 'PENDIENTE' OR c.esPagada = true)")
        boolean existsByMedicoIdAndFechaHoraAndEstadoNotCancelada(
                        @Param("medicoId") Long medicoId,
                        @Param("fechaHora") ZonedDateTime fechaHora);

        // Buscar citas en un rango de fechas (excluyendo canceladas y pendientes no pagadas)
        @Query("SELECT c FROM Cita c WHERE c.medico.id = :medicoId " +
                        "AND c.fechaHora BETWEEN :inicio AND :fin " +
                        "AND c.estado != 'CANCELADA' " +
                        "AND (c.estado != 'PENDIENTE' OR c.esPagada = true)")
        List<Cita> findByMedicoIdAndFechaHoraBetweenAndEstadoNotCancelada(
                        @Param("medicoId") Long medicoId,
                        @Param("inicio") ZonedDateTime inicio,
                        @Param("fin") ZonedDateTime fin);
}