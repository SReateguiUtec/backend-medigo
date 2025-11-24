package com.example.medigo.service;

import com.example.medigo.domain.EstadoCuenta;
import com.example.medigo.domain.Medico;
import com.example.medigo.domain.Paciente;
import com.example.medigo.domain.Rol;
import com.example.medigo.dto.response.MedicoResponseDto;
import com.example.medigo.dto.response.PacienteResponseDto;
import com.example.medigo.dto.response.UpdateEstadoCuentaDto;
import com.example.medigo.dto.response.UpdateMedicoDto;
import com.example.medigo.dto.response.UpdatePacienteDto;
import com.example.medigo.exceptions.UserAlreadyExistsException;
import com.example.medigo.exceptions.UserNotFoundException;
import com.example.medigo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/*
@DisplayName("Tests del Servicio de Perfil")
class ProfileServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProfileService profileService;

    private Paciente testPaciente;
    private Medico testMedico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test paciente
        testPaciente = new Paciente();
        testPaciente.setId(1L);
        testPaciente.setNombres("Juan");
        testPaciente.setApellidos("Perez");
        testPaciente.setEmail("juan@test.com");
        testPaciente.setDni("12345678");
        testPaciente.setTelefono("987654321");
        testPaciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        testPaciente.setRol(Rol.PACIENTE);
        testPaciente.setEstadoCuenta(EstadoCuenta.ACTIVADA);
        
        // Create test medico
        testMedico = new Medico();
        testMedico.setId(2L);
        testMedico.setNombres("Carlos");
        testMedico.setApellidos("Garcia");
        testMedico.setEmail("carlos@test.com");
        testMedico.setDni("87654321");
        testMedico.setTelefono("123456789");
        testMedico.setNumeroColegiado("12345");
        testMedico.setRol(Rol.MEDICO);
        testMedico.setEstadoCuenta(EstadoCuenta.ACTIVADA);
    }

    @Test
    @DisplayName("Should update paciente profile when valid data provided")
    void shouldUpdatePacienteProfileWhenValidDataProvided() {
        // Given
        UpdatePacienteDto updateDto = new UpdatePacienteDto();
        updateDto.setNombres("Juan Carlos");
        updateDto.setEmail("juancarlos@test.com");
        
        Paciente updatedPaciente = new Paciente();
        updatedPaciente.setId(1L);
        updatedPaciente.setNombres("Juan Carlos");
        updatedPaciente.setApellidos("Perez");
        updatedPaciente.setEmail("juancarlos@test.com");
        updatedPaciente.setDni("12345678");
        updatedPaciente.setTelefono("987654321");
        updatedPaciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        updatedPaciente.setRol(Rol.PACIENTE);
        updatedPaciente.setEstadoCuenta(EstadoCuenta.ACTIVADA);
        
        PacienteResponseDto responseDto = PacienteResponseDto.builder()
                .nombres("Juan Carlos")
                .apellidos("Perez")
                .email("juancarlos@test.com")
                .dni("12345678")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .rol(Rol.PACIENTE)
                .build();
        
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testPaciente));
        when(usuarioRepository.existsByEmail("juancarlos@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Paciente.class))).thenReturn(updatedPaciente);
        when(modelMapper.map(any(Paciente.class), eq(PacienteResponseDto.class))).thenReturn(responseDto);

        // When
        Object result = profileService.updateUserProfile("juan@test.com", updateDto);

        // Then
        assertNotNull(result, "Result should not be null");
        assertInstanceOf(PacienteResponseDto.class, result);
        PacienteResponseDto pacienteResponse = (PacienteResponseDto) result;
        assertEquals("Juan Carlos", pacienteResponse.getNombres());
        assertEquals("juancarlos@test.com", pacienteResponse.getEmail());
        
        verify(usuarioRepository, times(1)).findByEmail("juan@test.com");
        verify(usuarioRepository, times(1)).existsByEmail("juancarlos@test.com");
        verify(usuarioRepository, times(1)).save(any(Paciente.class));
        verify(modelMapper, times(1)).map(any(Paciente.class), eq(PacienteResponseDto.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating profile for non-existent user")
    void shouldThrowUserNotFoundExceptionWhenUpdatingProfileForNonExistentUser() {
        // Given
        UpdatePacienteDto updateDto = new UpdatePacienteDto();
        updateDto.setNombres("Juan Carlos");
        
        when(usuarioRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            profileService.updateUserProfile("nonexistent@test.com", updateDto);
        });
        
        verify(usuarioRepository, times(1)).findByEmail("nonexistent@test.com");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when updating email to existing email")
    void shouldThrowUserAlreadyExistsExceptionWhenUpdatingEmailToExistingEmail() {
        // Given
        UpdatePacienteDto updateDto = new UpdatePacienteDto();
        updateDto.setEmail("existing@test.com");
        
        Paciente otherPaciente = new Paciente();
        otherPaciente.setEmail("existing@test.com");
        
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testPaciente));
        when(usuarioRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> {
            profileService.updateUserProfile("juan@test.com", updateDto);
        });
        
        verify(usuarioRepository, times(1)).findByEmail("juan@test.com");
        verify(usuarioRepository, times(1)).existsByEmail("existing@test.com");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get paciente profile when user exists")
    void shouldGetPacienteProfileWhenUserExists() {
        // Given
        PacienteResponseDto responseDto = PacienteResponseDto.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@test.com")
                .dni("12345678")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .rol(Rol.PACIENTE)
                .build();
        
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testPaciente));
        when(modelMapper.map(any(Paciente.class), eq(PacienteResponseDto.class))).thenReturn(responseDto);

        // When
        Object result = profileService.getUserProfile("juan@test.com");

        // Then
        assertNotNull(result);
        assertInstanceOf(PacienteResponseDto.class, result);
        PacienteResponseDto pacienteResponse = (PacienteResponseDto) result;
        assertEquals("Juan", pacienteResponse.getNombres());
        
        verify(usuarioRepository, times(1)).findByEmail("juan@test.com");
        verify(modelMapper, times(1)).map(any(Paciente.class), eq(PacienteResponseDto.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting profile for non-existent user")
    void shouldThrowUserNotFoundExceptionWhenGettingProfileForNonExistentUser() {
        // Given
        when(usuarioRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            profileService.getUserProfile("nonexistent@test.com");
        });
        
        verify(usuarioRepository, times(1)).findByEmail("nonexistent@test.com");
    }

    @Test
    @DisplayName("Should update medico profile when valid data provided")
    void shouldUpdateMedicoProfileWhenValidDataProvided() {
        // Given
        UpdateMedicoDto updateDto = new UpdateMedicoDto();
        updateDto.setNombres("Carlos Alberto");
        updateDto.setEmail("carlosalberto@test.com");
        
        Medico updatedMedico = new Medico();
        updatedMedico.setId(2L);
        updatedMedico.setNombres("Carlos Alberto");
        updatedMedico.setApellidos("Garcia");
        updatedMedico.setEmail("carlosalberto@test.com");
        updatedMedico.setDni("87654321");
        updatedMedico.setTelefono("123456789");
        updatedMedico.setNumeroColegiado("12345");
        updatedMedico.setRol(Rol.MEDICO);
        updatedMedico.setEstadoCuenta(EstadoCuenta.ACTIVADA);
        
        MedicoResponseDto responseDto = MedicoResponseDto.builder()
                .nombres("Carlos Alberto")
                .apellidos("Garcia")
                .email("carlosalberto@test.com")
                .dni("87654321")
                .numeroColegiado("12345")
                .rol(Rol.MEDICO)
                .build();
        
        when(usuarioRepository.findByEmail("carlos@test.com")).thenReturn(Optional.of(testMedico));
        when(usuarioRepository.existsByEmail("carlosalberto@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Medico.class))).thenReturn(updatedMedico);
        when(modelMapper.map(any(Medico.class), eq(MedicoResponseDto.class))).thenReturn(responseDto);

        // When
        Object result = profileService.updateUserProfile("carlos@test.com", updateDto);

        // Then
        assertNotNull(result);
        assertInstanceOf(MedicoResponseDto.class, result);
        MedicoResponseDto medicoResponse = (MedicoResponseDto) result;
        assertEquals("Carlos Alberto", medicoResponse.getNombres());
        assertEquals("carlosalberto@test.com", medicoResponse.getEmail());
        
        verify(usuarioRepository, times(1)).findByEmail("carlos@test.com");
        verify(usuarioRepository, times(1)).existsByEmail("carlosalberto@test.com");
        verify(usuarioRepository, times(1)).save(any(Medico.class));
        verify(modelMapper, times(1)).map(any(Medico.class), eq(MedicoResponseDto.class));
    }

    @Test
    @DisplayName("Should update account status when valid data provided")
    void shouldUpdateAccountStatusWhenValidDataProvided() {
        // Given
        UpdateEstadoCuentaDto statusDto = new UpdateEstadoCuentaDto();
        statusDto.setEstadoCuenta(EstadoCuenta.DESACTIVADA);
        
        Paciente updatedPaciente = new Paciente();
        updatedPaciente.setId(1L);
        updatedPaciente.setNombres("Juan");
        updatedPaciente.setApellidos("Perez");
        updatedPaciente.setEmail("juan@test.com");
        updatedPaciente.setDni("12345678");
        updatedPaciente.setTelefono("987654321");
        updatedPaciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        updatedPaciente.setRol(Rol.PACIENTE);
        updatedPaciente.setEstadoCuenta(EstadoCuenta.DESACTIVADA);
        
        PacienteResponseDto responseDto = PacienteResponseDto.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@test.com")
                .dni("12345678")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .rol(Rol.PACIENTE)
                .build();
        
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testPaciente));
        when(usuarioRepository.save(any(Paciente.class))).thenReturn(updatedPaciente);
        when(modelMapper.map(any(Paciente.class), eq(PacienteResponseDto.class))).thenReturn(responseDto);

        // When
        Object result = profileService.updateAccountStatus("juan@test.com", statusDto);

        // Then
        assertNotNull(result);
        assertInstanceOf(PacienteResponseDto.class, result);
        
        verify(usuarioRepository, times(1)).findByEmail("juan@test.com");
        verify(usuarioRepository, times(1)).save(any(Paciente.class));
        verify(modelMapper, times(1)).map(any(Paciente.class), eq(PacienteResponseDto.class));
    }
}
    */