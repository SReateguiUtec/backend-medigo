package com.example.medigo.service;

import com.example.medigo.domain.EstadoCuenta;
import com.example.medigo.domain.Rol;
import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.CreateAdminRequestDto;
import com.example.medigo.dto.response.MedicoResponseDto;
import com.example.medigo.dto.response.PacienteResponseDto;
import com.example.medigo.dto.response.UsuarioResponseDto;
import com.example.medigo.exceptions.UserAlreadyExistsException;
import com.example.medigo.exceptions.UserNotFoundException;
import com.example.medigo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.modelmapper.ModelMapper;
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario updateUserAccountStatus(Long userId, EstadoCuenta newStatus) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        usuario.setEstadoCuenta(newStatus);
        log.info("Admin actualizó el estado de la cuenta del usuario {} a {}", userId, newStatus);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Object getUserDetailsById(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        if (usuario.getRol() == Rol.PACIENTE) {
            return modelMapper.map(usuario, PacienteResponseDto.class);
        } else if (usuario.getRol() == Rol.MEDICO) {
            return modelMapper.map(usuario, MedicoResponseDto.class);
        } else if (usuario.getRol() == Rol.ADMIN) {
            return modelMapper.map(usuario, UsuarioResponseDto.class);
        }
        throw new IllegalStateException("Rol de usuario desconocido: " + usuario.getRol());
    }

    @Transactional
    public UsuarioResponseDto createAdmin(CreateAdminRequestDto request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El email ya está en uso: " + request.getEmail());
        }

        Usuario newAdmin = new Usuario();
        newAdmin.setNombres(request.getNombres());
        newAdmin.setApellidos(request.getApellidos());
        newAdmin.setEmail(request.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        newAdmin.setRol(Rol.ADMIN);
        newAdmin.setEstadoCuenta(EstadoCuenta.ACTIVADA);

        Usuario savedAdmin = usuarioRepository.save(newAdmin);
        log.info("Nuevo administrador creado con email: {}", savedAdmin.getEmail());
        return modelMapper.map(savedAdmin, UsuarioResponseDto.class);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!usuarioRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }
        usuarioRepository.deleteById(userId);
        log.warn("Usuario con ID {} ha sido eliminado por un administrador.", userId);
    }
}