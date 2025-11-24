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
import com.example.medigo.exceptions.UserAlreadyExistsException;
import com.example.medigo.exceptions.UserNotFoundException;
import com.example.medigo.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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
    public Object updateUserProfile(String email, Map<String, Object> updates) {
        Usuario usuario = obtenerUsuarioPorEmail(email);

        if (usuario.getRol() == Rol.PACIENTE) {
            return updatePacienteProfile((Paciente) usuario, updates);
        } else if (usuario.getRol() == Rol.MEDICO) {
            return updateMedicoProfile((Medico) usuario, updates);
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

    private Object updatePacienteProfile(Paciente paciente, Map<String, Object> updates) {

        // Campos de Usuario (clase padre): nombres, apellidos, email, telefono, edad,
        // rutaFoto
        if (updates.containsKey("nombres")) {
            paciente.setNombres((String) updates.get("nombres"));
        }
        if (updates.containsKey("apellidos")) {
            paciente.setApellidos((String) updates.get("apellidos"));
        }
        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (newEmail != null && !newEmail.isEmpty() && 
                !newEmail.equals(paciente.getEmail() != null ? paciente.getEmail() : "") &&
                usuarioRepository.existsByEmail(newEmail)) {
                throw new UserAlreadyExistsException("Email ya está en uso.");
            }
            paciente.setEmail(newEmail);
        }
        if (updates.containsKey("telefono")) {
            String newTelefono = (String) updates.get("telefono");
            if (newTelefono != null && !newTelefono.isEmpty() && 
                !newTelefono.equals(paciente.getTelefono() != null ? paciente.getTelefono() : "") &&
                usuarioRepository.findByTelefono(newTelefono).isPresent()) {
                throw new UserAlreadyExistsException("Teléfono ya está en uso.");
            }
            paciente.setTelefono(newTelefono);
        }
        if (updates.containsKey("edad")) {
            Object edadObj = updates.get("edad");
            if (edadObj instanceof Integer) {
                paciente.setEdad((Integer) edadObj);
            } else if (edadObj instanceof String) {
                try {
                    paciente.setEdad(Integer.parseInt((String) edadObj));
                } catch (NumberFormatException e) {
                }
            }
        }

        // Campos específicos de Paciente: dni, fechaNacimiento
        if (updates.containsKey("dni")) {
            paciente.setDni((String) updates.get("dni"));
        }
        if (updates.containsKey("fechaNacimiento")) {
            String fechaStr = (String) updates.get("fechaNacimiento");
            if (fechaStr != null && !fechaStr.isEmpty()) {
                paciente.setFechaNacimiento(LocalDate.parse(fechaStr));
            }
        }

        usuarioRepository.save(paciente);
        return modelMapper.map(paciente, PacienteResponseDto.class);
    }

    private Object updateMedicoProfile(Medico medico, Map<String, Object> updates) {

        // Campos de Usuario (clase padre): nombres, apellidos, email, telefono, edad,
        // rutaFoto
        if (updates.containsKey("nombres")) {
            medico.setNombres((String) updates.get("nombres"));
        }
        if (updates.containsKey("apellidos")) {
            medico.setApellidos((String) updates.get("apellidos"));
        }
        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (newEmail != null && !newEmail.isEmpty() && 
                !newEmail.equals(medico.getEmail() != null ? medico.getEmail() : "") &&
                usuarioRepository.existsByEmail(newEmail)) {
                throw new UserAlreadyExistsException("Email ya está en uso.");
            }
            medico.setEmail(newEmail);
        }
        if (updates.containsKey("telefono")) {
            String newTelefono = (String) updates.get("telefono");
            if (newTelefono != null && !newTelefono.isEmpty() && 
                !newTelefono.equals(medico.getTelefono() != null ? medico.getTelefono() : "") &&
                usuarioRepository.findByTelefono(newTelefono).isPresent()) {
                throw new UserAlreadyExistsException("Teléfono ya está en uso.");
            }
            medico.setTelefono(newTelefono);
        }
        if (updates.containsKey("edad")) {
            Object edadObj = updates.get("edad");
            if (edadObj instanceof Integer) {
                medico.setEdad((Integer) edadObj);
            } else if (edadObj instanceof String) {
                try {
                    medico.setEdad(Integer.parseInt((String) edadObj));
                } catch (NumberFormatException e) {
                }
            }
        }

        // Campos específicos de Medico: dni, numeroColegiado, bio, precioConsulta
        if (updates.containsKey("dni")) {
            medico.setDni((String) updates.get("dni"));
        }
        if (updates.containsKey("numeroColegiado")) {
            medico.setNumeroColegiado((String) updates.get("numeroColegiado"));
        }
        if (updates.containsKey("bio")) {
            medico.setBio((String) updates.get("bio"));
        }
        if (updates.containsKey("precioConsulta")) {
            Object precioObj = updates.get("precioConsulta");
            BigDecimal precio = null;
            try {
                if (precioObj instanceof Double) {
                    precio = BigDecimal.valueOf((Double) precioObj);
                } else if (precioObj instanceof String) {
                    precio = new BigDecimal((String) precioObj);
                } else if (precioObj instanceof Integer) {
                    precio = BigDecimal.valueOf(((Integer) precioObj).doubleValue());
                } else if (precioObj instanceof BigDecimal) {
                    precio = (BigDecimal) precioObj;
                } else if (precioObj != null) {
                    // Try to parse as string representation
                    precio = new BigDecimal(precioObj.toString());
                }
            } catch (NumberFormatException e) {
                // If we can't parse the price, we'll leave it as null and not update
                // This prevents errors when invalid data is sent
            }
            if (precio != null) {
                medico.setPrecioConsulta(precio);
            }
        }

        usuarioRepository.save(medico);
        return modelMapper.map(medico, MedicoResponseDto.class);
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
