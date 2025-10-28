package com.example.medigo.service;

import com.example.medigo.domain.Medico;
import com.example.medigo.dto.response.MedicoSearchResponseDto;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests del Servicio de Búsqueda")
class SearchServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SearchService searchService;

    private Medico testMedico;
    private MedicoSearchResponseDto medicoSearchResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testMedico = new Medico();
        testMedico.setId(1L);
        testMedico.setNombres("Dr. Carlos");
        testMedico.setApellidos("García López");
        testMedico.setEmail("carlos@test.com");
        testMedico.setDni("87654321");
        testMedico.setNumeroColegiado("CO12345");
        testMedico.setBio("Médico especialista");
        testMedico.setPrecioConsulta(new BigDecimal("100.00"));

        medicoSearchResponseDto = new MedicoSearchResponseDto();
        medicoSearchResponseDto.setId(1L);
        medicoSearchResponseDto.setNombres("Dr. Carlos");
        medicoSearchResponseDto.setApellidos("García López");
        medicoSearchResponseDto.setEmail("carlos@test.com");
        medicoSearchResponseDto.setNumeroColegiado("CO12345");
        medicoSearchResponseDto.setBio("Médico especialista");
        medicoSearchResponseDto.setPrecioConsulta(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should get all medicos with pagination")
    void shouldGetAllMedicosWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> medicoPage = new PageImpl<>(Collections.singletonList(testMedico));
        
        when(medicoRepository.findAll(pageable)).thenReturn(medicoPage);
        when(modelMapper.map(testMedico, MedicoSearchResponseDto.class)).thenReturn(medicoSearchResponseDto);

        // When
        Page<MedicoSearchResponseDto> result = searchService.getAllMedicos(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(medicoSearchResponseDto, result.getContent().get(0));
        verify(medicoRepository, times(1)).findAll(pageable);
        verify(modelMapper, times(1)).map(testMedico, MedicoSearchResponseDto.class);
    }

    @Test
    @DisplayName("Should search medicos by name with pagination")
    void shouldSearchMedicosByNameWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> medicoPage = new PageImpl<>(Collections.singletonList(testMedico));
        
        when(medicoRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                "carlos", "carlos", pageable)).thenReturn(medicoPage);
        when(modelMapper.map(testMedico, MedicoSearchResponseDto.class)).thenReturn(medicoSearchResponseDto);

        // When
        Page<MedicoSearchResponseDto> result = searchService.searchMedicosByNombre("carlos", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(medicoSearchResponseDto, result.getContent().get(0));
        verify(medicoRepository, times(1))
                .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase("carlos", "carlos", pageable);
        verify(modelMapper, times(1)).map(testMedico, MedicoSearchResponseDto.class);
    }

    @Test
    @DisplayName("Should get medico by email when medico exists")
    void shouldGetMedicoByEmailWhenMedicoExists() {
        // Given
        when(medicoRepository.findByEmail("carlos@test.com")).thenReturn(Optional.of(testMedico));
        when(modelMapper.map(testMedico, MedicoSearchResponseDto.class)).thenReturn(medicoSearchResponseDto);

        // When
        MedicoSearchResponseDto result = searchService.getMedicoByEmail("carlos@test.com");

        // Then
        assertNotNull(result);
        assertEquals(medicoSearchResponseDto, result);
        verify(medicoRepository, times(1)).findByEmail("carlos@test.com");
        verify(modelMapper, times(1)).map(testMedico, MedicoSearchResponseDto.class);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when medico does not exist by email")
    void shouldThrowResourceNotFoundExceptionWhenMedicoDoesNotExistByEmail() {
        // Given
        when(medicoRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            searchService.getMedicoByEmail("nonexistent@test.com");
        });
        
        verify(medicoRepository, times(1)).findByEmail("nonexistent@test.com");
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("Should get medico by ID when medico exists")
    void shouldGetMedicoByIdWhenMedicoExists() {
        // Given
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(testMedico));
        when(modelMapper.map(testMedico, MedicoSearchResponseDto.class)).thenReturn(medicoSearchResponseDto);

        // When
        MedicoSearchResponseDto result = searchService.getMedicoById(1L);

        // Then
        assertNotNull(result);
        assertEquals(medicoSearchResponseDto, result);
        verify(medicoRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(testMedico, MedicoSearchResponseDto.class);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when medico does not exist by ID")
    void shouldThrowResourceNotFoundExceptionWhenMedicoDoesNotExistById() {
        // Given
        when(medicoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            searchService.getMedicoById(999L);
        });
        
        verify(medicoRepository, times(1)).findById(999L);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("Should get medicos by especialidad with pagination")
    void shouldGetMedicosByEspecialidadWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> medicoPage = new PageImpl<>(Collections.singletonList(testMedico));
        
        when(medicoRepository.findByEspecialidadesId(1L, pageable)).thenReturn(medicoPage);
        when(modelMapper.map(testMedico, MedicoSearchResponseDto.class)).thenReturn(medicoSearchResponseDto);

        // When
        Page<MedicoSearchResponseDto> result = searchService.getMedicosByEspecialidad(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(medicoSearchResponseDto, result.getContent().get(0));
        verify(medicoRepository, times(1)).findByEspecialidadesId(1L, pageable);
        verify(modelMapper, times(1)).map(testMedico, MedicoSearchResponseDto.class);
    }

    @Test
    @DisplayName("Should get medicos by price range with pagination")
    void shouldGetMedicosByPriceRangeWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> medicoPage = new PageImpl<>(Collections.singletonList(testMedico));
        BigDecimal minPrecio = new BigDecimal("50.00");
        BigDecimal maxPrecio = new BigDecimal("150.00");
        
        when(medicoRepository.findByPrecioConsultaBetween(minPrecio, maxPrecio, pageable)).thenReturn(medicoPage);
        when(modelMapper.map(testMedico, MedicoSearchResponseDto.class)).thenReturn(medicoSearchResponseDto);

        // When
        Page<MedicoSearchResponseDto> result = searchService.getMedicosByPrecioRange(minPrecio, maxPrecio, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(medicoSearchResponseDto, result.getContent().get(0));
        verify(medicoRepository, times(1)).findByPrecioConsultaBetween(minPrecio, maxPrecio, pageable);
        verify(modelMapper, times(1)).map(testMedico, MedicoSearchResponseDto.class);
    }
}