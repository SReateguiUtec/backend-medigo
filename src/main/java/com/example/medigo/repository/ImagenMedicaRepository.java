package com.example.medigo.repository;

import com.example.medigo.domain.ImagenMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagenMedicaRepository extends JpaRepository<ImagenMedica, Long> {
    
    List<ImagenMedica> findByHistorialMedicoId(Long historialMedicoId);
    
    List<ImagenMedica> findByUploadedById(Long uploadedById);
    
    List<ImagenMedica> findByHistorialMedicoIdOrderByUploadedAtDesc(Long historialMedicoId);
}
