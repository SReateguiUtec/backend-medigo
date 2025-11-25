package com.example.medigo.controller;

import com.example.medigo.dto.response.MedicoSearchResponseDto;
import com.example.medigo.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // Listar todos los médicos con paginación
    @GetMapping("/medicos")
    public ResponseEntity<Page<MedicoSearchResponseDto>> getAllMedicos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombres") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MedicoSearchResponseDto> medicos = searchService.getAllMedicos(pageable);

        return ResponseEntity.ok(medicos);
    }

    // Buscar médicos por nombre o apellido
    @GetMapping("/medicos/search")
    public ResponseEntity<Page<MedicoSearchResponseDto>> searchMedicosByNombre(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombres") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MedicoSearchResponseDto> medicos = searchService.searchMedicosByNombre(q, pageable);

        return ResponseEntity.ok(medicos);
    }

    // Buscar médico exacto por email
    @GetMapping("/medicos/email/{email}")
    public ResponseEntity<MedicoSearchResponseDto> getMedicoByEmail(@PathVariable String email) {
        MedicoSearchResponseDto medico = searchService.getMedicoByEmail(email);
        return ResponseEntity.ok(medico);
    }

    // Obtener detalle de un médico por ID
    @GetMapping("/medicos/{medicoId}")
    public ResponseEntity<MedicoSearchResponseDto> getMedicoById(@PathVariable Long medicoId) {
        MedicoSearchResponseDto medico = searchService.getMedicoById(medicoId);
        return ResponseEntity.ok(medico);
    }

    // Filtrar médicos por especialidad
    @GetMapping("/medicos/especialidad/{especialidadId}")
    public ResponseEntity<Page<MedicoSearchResponseDto>> getMedicosByEspecialidad(
            @PathVariable Long especialidadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombres") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MedicoSearchResponseDto> medicos = searchService.getMedicosByEspecialidad(especialidadId, pageable);

        return ResponseEntity.ok(medicos);
    }

    // Filtrar médicos por nombre de especialidad
    @GetMapping("/medicos/especialidad/nombre/{nombreEspecialidad}")
    public ResponseEntity<Page<MedicoSearchResponseDto>> getMedicosByEspecialidadNombre(
            @PathVariable String nombreEspecialidad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombres") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MedicoSearchResponseDto> medicos = searchService.getMedicosByEspecialidadNombre(nombreEspecialidad,
                pageable);

        return ResponseEntity.ok(medicos);
    }

    // Filtrar médicos por rango de precios
    @GetMapping("/medicos/precio")
    public ResponseEntity<Page<MedicoSearchResponseDto>> getMedicosByPrecioRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "precioConsulta") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MedicoSearchResponseDto> medicos = searchService.getMedicosByPrecioRange(min, max, pageable);

        return ResponseEntity.ok(medicos);
    }
}
