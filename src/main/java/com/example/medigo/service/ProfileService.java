package com.example.medigo.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medigo.domain.Medico;
import com.example.medigo.domain.Paciente;
import com.example.medigo.domain.Rol;
import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.response.MedicoResponseDto;
import com.example.medigo.dto.response.PacienteResponseDto;
import com.example.medigo.dto.response.UpdateEstadoCuentaDto;
import com.example.medigo.dto.response.UpdateMedicoDto;
import com.example.medigo.dto.response.UpdatePacienteDto;
import com.example.medigo.exceptions.UserAlreadyExistsException;
import com.example.medigo.exceptions.UserNotFoundException;
import com.example.medigo.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    private Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));
    }

    @Transactional
    public Object updateUserProfile(String email, Object updates) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        
        if (usuario.getRol() == Rol.PACIENTE) {
            return updatePacienteProfile((Paciente) usuario, updates); // Se deriva al metodo donde se setea al paciente
        } else if (usuario.getRol() == Rol.MEDICO) {
            return updateMedicoProfile((Medico) usuario, updates); // Se deriva al metodo donde se setea al medico
        }
        throw new IllegalStateException("Rol de usuario no válido.");
    }

    @Transactional(readOnly = true)
    public Object getUserProfile(String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);

        if (usuario.getRol() == Rol.PACIENTE) {
            Paciente paciente = (Paciente) usuario;
            return modelMapper.map(paciente, PacienteResponseDto.class);
        } else if (usuario.getRol() == Rol.MEDICO) {
            Medico medico = (Medico) usuario;
            return modelMapper.map(medico, MedicoResponseDto.class);
        }
        throw new IllegalStateException("Rol de usuario no válido.");
    }

    private Object updatePacienteProfile(Paciente paciente, Object updates) {

        if (updates instanceof UpdatePacienteDto) {
            UpdatePacienteDto pacienteUpdates = (UpdatePacienteDto) updates;
            
            if (pacienteUpdates.getNombres() != null) {
                paciente.setNombres(pacienteUpdates.getNombres());
            }
            if (pacienteUpdates.getApellidos() != null) {
                paciente.setApellidos(pacienteUpdates.getApellidos());
            }
            if (pacienteUpdates.getEmail() != null) {
                // Validar que no sea el mismmo email
                if (!paciente.getEmail().equals(pacienteUpdates.getEmail()) && 
                    usuarioRepository.existsByEmail(pacienteUpdates.getEmail())) {
                    throw new UserAlreadyExistsException("Email ya está en uso.");
                }
                paciente.setEmail(pacienteUpdates.getEmail());
            }
            if (pacienteUpdates.getFechaNacimiento() != null) {
                paciente.setFechaNacimiento(pacienteUpdates.getFechaNacimiento());
            }
            if (pacienteUpdates.getDni() != null) {
                paciente.setDni(pacienteUpdates.getDni());
            }
            if (pacienteUpdates.getTelefono() != null) {
                // Validar que no sea un telefono de otro usuario
                if (!paciente.getTelefono().equals(pacienteUpdates.getTelefono()) && 
                    usuarioRepository.findByTelefono(pacienteUpdates.getTelefono()).isPresent()) {
                    throw new UserAlreadyExistsException("Teléfono ya está en uso.");
                }
                paciente.setTelefono(pacienteUpdates.getTelefono());
            }
            
            usuarioRepository.save(paciente);
            return modelMapper.map(paciente, PacienteResponseDto.class);
        }
        throw new IllegalArgumentException("Tipo de actualización no válido para paciente");
    }

    private Object updateMedicoProfile(Medico medico, Object updates) {
        if (updates instanceof UpdateMedicoDto) {
            UpdateMedicoDto medicoUpdates = (UpdateMedicoDto) updates;
            
            if (medicoUpdates.getNombres() != null) {
                medico.setNombres(medicoUpdates.getNombres());
            }
            if (medicoUpdates.getApellidos() != null) {
                medico.setApellidos(medicoUpdates.getApellidos());
            }
            if (medicoUpdates.getEmail() != null) {
                // Validar que no sea el mismmo email
                if (!medico.getEmail().equals(medicoUpdates.getEmail()) && 
                    usuarioRepository.existsByEmail(medicoUpdates.getEmail())) {
                    throw new UserAlreadyExistsException("Email ya está en uso.");
                }
                medico.setEmail(medicoUpdates.getEmail());
            }
            if (medicoUpdates.getDni() != null) {
                medico.setDni(medicoUpdates.getDni());
            }
            if (medicoUpdates.getNumeroColegiado() != null) {
                medico.setNumeroColegiado(medicoUpdates.getNumeroColegiado());
            }
            if (medicoUpdates.getBio() != null) {
                medico.setBio(medicoUpdates.getBio());
            }
            if (medicoUpdates.getTelefono() != null) {
                // Validar que no sea un telefono de otro usuario
                if (!medico.getTelefono().equals(medicoUpdates.getTelefono()) && 
                    usuarioRepository.findByTelefono(medicoUpdates.getTelefono()).isPresent()) {
                    throw new UserAlreadyExistsException("Teléfono ya está en uso.");
                }
                medico.setTelefono(medicoUpdates.getTelefono());
            }
            
            usuarioRepository.save(medico);
            return modelMapper.map(medico, MedicoResponseDto.class);
        }
        throw new IllegalArgumentException("Tipo de actualización no válido para médico");
    }

    @Transactional
    public Object updateAccountStatus(String email, UpdateEstadoCuentaDto statusDto) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        usuario.setEstadoCuenta(statusDto.getEstadoCuenta());
        usuarioRepository.save(usuario);
        
        if (usuario.getRol() == Rol.PACIENTE) {
            return modelMapper.map(usuario, PacienteResponseDto.class);
        } else if (usuario.getRol() == Rol.MEDICO) {
            return modelMapper.map(usuario, MedicoResponseDto.class);
        }
        throw new IllegalStateException("Rol de usuario no válido.");
    }
}