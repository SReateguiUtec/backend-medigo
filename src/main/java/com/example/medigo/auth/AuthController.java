package com.example.medigo.auth;

import com.example.medigo.dto.request.MedicoRequestDto;
import com.example.medigo.dto.request.PacienteRequestDto;
import com.example.medigo.dto.request.SignInDto;
import com.example.medigo.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/paciente")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody
                                                    PacienteRequestDto request) {
        TokenResponse response = authService.pacienteSignUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/signup/medico")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody
                                                MedicoRequestDto request) {
        TokenResponse response = authService.medicoSignUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/signin")
    public ResponseEntity<TokenResponse> signin(@Valid @RequestBody SignInDto request) {
        TokenResponse response = authService.signin(request);
        return ResponseEntity.ok().body(response);
    }
}
