package com.example.medigo.auth;

import com.example.medigo.domain.EstadoCuenta;
import com.example.medigo.domain.Medico;
import com.example.medigo.domain.Paciente;
import com.example.medigo.domain.Rol;
import com.example.medigo.dto.request.MedicoRequestDto;
import com.example.medigo.dto.request.PacienteRequestDto;
import com.example.medigo.dto.request.SignInDto;
import com.example.medigo.dto.response.TokenResponse;
import com.example.medigo.events.SignUpEvent;
import com.example.medigo.exceptions.UserAlreadyExistsException;
import com.example.medigo.repository.MedicoRepository;
import com.example.medigo.repository.PacienteRepository;
import com.example.medigo.repository.UsuarioRepository;
import com.example.medigo.security.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TokenResponse pacienteSignUp(PacienteRequestDto request) {

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Paciente ya existe con este correo.");
        }
    
        Paciente paciente = Paciente.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.PACIENTE)
                .estadoCuenta(EstadoCuenta.ACTIVADA)
                .build();
        pacienteRepository.save(paciente);
        eventPublisher.publishEvent(new SignUpEvent(this, paciente));

        var token = jwtService.generateToken(paciente);
        return new TokenResponse(token);
    }

    @Transactional
    public TokenResponse medicoSignUp(MedicoRequestDto request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Medico ya existe con este correo.");
        }

        Medico medico = Medico.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dni("")
                .rol(Rol.MEDICO)
                .estadoCuenta(EstadoCuenta.ACTIVADA)
                .build();
        medicoRepository.save(medico);
        eventPublisher.publishEvent(new SignUpEvent(this, medico));

        var token = jwtService.generateToken(medico);
        return new TokenResponse(token);
    }

    @Transactional(readOnly = true)
    public TokenResponse signin(SignInDto request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        var token = jwtService.generateToken(userDetails);
        return new TokenResponse(token);
    }
}