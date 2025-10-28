package com.example.medigo.service;

import com.example.medigo.domain.EstadoCuenta;
import com.example.medigo.domain.Rol;
import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.CreateAdminRequestDto;
import com.example.medigo.dto.response.UsuarioResponseDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests del Servicio de Admin")
class AdminServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Usuario testUsuario;
    private UsuarioResponseDto usuarioResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUsuario = new Usuario();
        testUsuario.setId(1L);
        testUsuario.setNombres("Admin");
        testUsuario.setApellidos("Test");
        testUsuario.setEmail("admin@test.com");
        testUsuario.setPassword("encodedPassword");
        testUsuario.setRol(Rol.ADMIN);
        testUsuario.setEstadoCuenta(EstadoCuenta.ACTIVADA);

        usuarioResponseDto = new UsuarioResponseDto();
        usuarioResponseDto.setId(1L);
        usuarioResponseDto.setNombres("Admin");
        usuarioResponseDto.setApellidos("Test");
        usuarioResponseDto.setEmail("admin@test.com");
        usuarioResponseDto.setRol(Rol.ADMIN);
        usuarioResponseDto.setEstadoCuenta(EstadoCuenta.ACTIVADA);
    }

    @Test
    @DisplayName("Should update user account status when user exists")
    void shouldUpdateUserAccountStatusWhenUserExists() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(testUsuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(testUsuario);

        // When
        Usuario result = adminService.updateUserAccountStatus(1L, EstadoCuenta.BLOQUEADA);

        // Then
        assertNotNull(result);
        assertEquals(EstadoCuenta.BLOQUEADA, result.getEstadoCuenta());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(testUsuario);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating status for non-existent user")
    void shouldThrowUserNotFoundExceptionWhenUpdatingStatusForNonExistentUser() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            adminService.updateUserAccountStatus(1L, EstadoCuenta.BLOQUEADA);
        });
        
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Should get user details by ID when user exists")
    void shouldGetUserDetailsByIdWhenUserExists() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(testUsuario));
        when(modelMapper.map(testUsuario, UsuarioResponseDto.class)).thenReturn(usuarioResponseDto);

        // When
        Object result = adminService.getUserDetailsById(1L);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof UsuarioResponseDto);
        assertEquals(usuarioResponseDto, result);
        verify(usuarioRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(testUsuario, UsuarioResponseDto.class);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting details for non-existent user")
    void shouldThrowUserNotFoundExceptionWhenGettingDetailsForNonExistentUser() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            adminService.getUserDetailsById(1L);
        });
        
        verify(usuarioRepository, times(1)).findById(1L);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("Should create admin when email is not already in use")
    void shouldCreateAdminWhenEmailIsNotAlreadyInUse() {
        // Given
        CreateAdminRequestDto request = new CreateAdminRequestDto();
        request.setNombres("New");
        request.setApellidos("Admin");
        request.setEmail("newadmin@test.com");
        request.setPassword("password123");

        Usuario newAdmin = new Usuario();
        newAdmin.setId(2L);
        newAdmin.setNombres("New");
        newAdmin.setApellidos("Admin");
        newAdmin.setEmail("newadmin@test.com");
        newAdmin.setPassword("encodedPassword123");
        newAdmin.setRol(Rol.ADMIN);
        newAdmin.setEstadoCuenta(EstadoCuenta.ACTIVADA);

        UsuarioResponseDto responseDto = new UsuarioResponseDto();
        responseDto.setId(2L);
        responseDto.setNombres("New");
        responseDto.setApellidos("Admin");
        responseDto.setEmail("newadmin@test.com");
        responseDto.setRol(Rol.ADMIN);
        responseDto.setEstadoCuenta(EstadoCuenta.ACTIVADA);

        when(usuarioRepository.existsByEmail("newadmin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(newAdmin);
        when(modelMapper.map(newAdmin, UsuarioResponseDto.class)).thenReturn(responseDto);

        // When
        UsuarioResponseDto result = adminService.createAdmin(request);

        // Then
        assertNotNull(result);
        assertEquals("newadmin@test.com", result.getEmail());
        verify(usuarioRepository, times(1)).existsByEmail("newadmin@test.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(modelMapper, times(1)).map(newAdmin, UsuarioResponseDto.class);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when creating admin with existing email")
    void shouldThrowUserAlreadyExistsExceptionWhenCreatingAdminWithExistingEmail() {
        // Given
        CreateAdminRequestDto request = new CreateAdminRequestDto();
        request.setNombres("New");
        request.setApellidos("Admin");
        request.setEmail("admin@test.com");
        request.setPassword("password123");

        when(usuarioRepository.existsByEmail("admin@test.com")).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> {
            adminService.createAdmin(request);
        });
        
        verify(usuarioRepository, times(1)).existsByEmail("admin@test.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("Should delete user when user exists")
    void shouldDeleteUserWhenUserExists() {
        // Given
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // When
        adminService.deleteUser(1L);

        // Then
        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void shouldThrowUserNotFoundExceptionWhenDeletingNonExistentUser() {
        // Given
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            adminService.deleteUser(1L);
        });
        
        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}