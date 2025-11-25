package com.example.medigo.service;

import com.example.medigo.domain.Medico;
import com.example.medigo.dto.response.MedicoSearchResponseDto;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final MedicoRepository medicoRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<MedicoSearchResponseDto> getAllMedicos(Pageable pageable) {
        log.info("Buscando todos los médicos con paginación: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Medico> medicos = medicoRepository.findAll(pageable);
        return medicos.map(medico -> {
            MedicoSearchResponseDto dto = modelMapper.map(medico, MedicoSearchResponseDto.class);
            dto.setNumeroColegiado(medico.getNumeroColegiado());
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<MedicoSearchResponseDto> searchMedicosByNombre(String query, Pageable pageable) {
        log.info("Buscando médicos por nombre/apellido: query='{}', page={}, size={}",
                query, pageable.getPageNumber(), pageable.getPageSize());

        Page<Medico> medicos = medicoRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                query, query, pageable);
        return medicos.map(medico -> {
            MedicoSearchResponseDto dto = modelMapper.map(medico, MedicoSearchResponseDto.class);
            dto.setNumeroColegiado(medico.getNumeroColegiado());
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public MedicoSearchResponseDto getMedicoByEmail(String email) {
        log.info("Buscando médico por email: {}", email);

        Medico medico = medicoRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con email: " + email));

        MedicoSearchResponseDto dto = modelMapper.map(medico, MedicoSearchResponseDto.class);
        dto.setNumeroColegiado(medico.getNumeroColegiado());
        return dto;
    }

    @Transactional(readOnly = true)
    public MedicoSearchResponseDto getMedicoById(Long medicoId) {
        log.info("Buscando médico por ID: {}", medicoId);

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + medicoId));

        log.info("Médico encontrado: {} {}, numeroColegiado en entidad: {}",
                medico.getNombres(), medico.getApellidos(), medico.getNumeroColegiado());

        MedicoSearchResponseDto dto = modelMapper.map(medico, MedicoSearchResponseDto.class);
        dto.setNumeroColegiado(medico.getNumeroColegiado());

        log.info("DTO creado, numeroColegiado en DTO: {}", dto.getNumeroColegiado());

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<MedicoSearchResponseDto> getMedicosByEspecialidad(Long especialidadId, Pageable pageable) {
        log.info("Buscando médicos por especialidad ID: {}, page={}, size={}",
                especialidadId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Medico> medicos = medicoRepository.findByEspecialidadesId(especialidadId, pageable);
        return medicos.map(medico -> {
            MedicoSearchResponseDto dto = modelMapper.map(medico, MedicoSearchResponseDto.class);
            dto.setNumeroColegiado(medico.getNumeroColegiado());
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<MedicoSearchResponseDto> getMedicosByEspecialidadNombre(String nombreEspecialidad, Pageable pageable) {
        log.info("Buscando médicos por especialidad nombre: {}, page={}, size={}",
                nombreEspecialidad, pageable.getPageNumber(), pageable.getPageSize());

        Page<Medico> medicos = medicoRepository.findByEspecialidadNombre(nombreEspecialidad, pageable);
        return medicos.map(medico -> {
            MedicoSearchResponseDto dto = modelMapper.map(medico, MedicoSearchResponseDto.class);
            dto.setNumeroColegiado(medico.getNumeroColegiado());
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<MedicoSearchResponseDto> getMedicosByPrecioRange(BigDecimal minPrecio,
            BigDecimal maxPrecio,
            Pageable pageable) {
        log.info("Buscando médicos por rango de precio: min={}, max={}, page={}, size={}",
                minPrecio, maxPrecio, pageable.getPageNumber(), pageable.getPageSize());

        Page<Medico> medicos = medicoRepository.findByPrecioConsultaBetween(minPrecio, maxPrecio, pageable);
        return medicos.map(medico -> {
            MedicoSearchResponseDto dto = modelMapper.map(medico, MedicoSearchResponseDto.class);
            dto.setNumeroColegiado(medico.getNumeroColegiado());
            return dto;
        });
    }
}
