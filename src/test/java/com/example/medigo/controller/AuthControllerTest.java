package com.example.medigo.controller;

import com.example.medigo.auth.AuthController;
import com.example.medigo.auth.AuthService;
import com.example.medigo.dto.request.MedicoRequestDto;
import com.example.medigo.dto.request.PacienteRequestDto;
import com.example.medigo.dto.request.SignInDto;
import com.example.medigo.dto.response.TokenResponse;
import com.example.medigo.exceptions.InvalidCredentialsException;
import com.example.medigo.exceptions.UserAlreadyExistsException;
import com.example.medigo.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests del Controlador de Autenticación")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private PacienteRequestDto pacienteRequestDto;
    private MedicoRequestDto medicoRequestDto;
    private SignInDto signInDto;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        pacienteRequestDto = new PacienteRequestDto();
        pacienteRequestDto.setNombres("Juan");
        pacienteRequestDto.setApellidos("Pérez");
        pacienteRequestDto.setEmail("juan.perez@example.com");
        pacienteRequestDto.setPassword("password123");

        medicoRequestDto = new MedicoRequestDto();
        medicoRequestDto.setNombres("Dr. Carlos");
        medicoRequestDto.setApellidos("García");
        medicoRequestDto.setEmail("carlos.garcia@example.com");
        medicoRequestDto.setPassword("password123");

        signInDto = new SignInDto();
        signInDto.setEmail("juan.perez@example.com");
        signInDto.setPassword("password123");

        tokenResponse = new TokenResponse("jwt-token-12345");
    }

    @Test
    @DisplayName("Should register paciente successfully with valid data")
    void shouldRegisterPacienteSuccessfullyWithValidData() throws Exception {
        // Given
        when(authService.pacienteSignUp(any(PacienteRequestDto.class)))
                .thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/signup/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacienteRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("jwt-token-12345"));
    }

    @Test
    @DisplayName("Should return 409 when paciente email already exists")
    void shouldReturn409WhenPacienteEmailAlreadyExists() throws Exception {
        // Given
        when(authService.pacienteSignUp(any(PacienteRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException("Paciente ya existe con este correo."));

        // When & Then
        mockMvc.perform(post("/api/auth/signup/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacienteRequestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Usuario ya existe"));
    }

    @Test
    @DisplayName("Should return 400 when paciente data is invalid")
    void shouldReturn400WhenPacienteDataIsInvalid() throws Exception {
        // Given
        pacienteRequestDto.setEmail(""); // email vacío
        pacienteRequestDto.setPassword("123"); // password muy corto

        // When & Then
        mockMvc.perform(post("/api/auth/signup/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacienteRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should register medico successfully with valid data")
    void shouldRegisterMedicoSuccessfullyWithValidData() throws Exception {
        // Given
        when(authService.medicoSignUp(any(MedicoRequestDto.class)))
                .thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/signup/medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicoRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("jwt-token-12345"));
    }

    @Test
    @DisplayName("Should return 409 when medico email already exists")
    void shouldReturn409WhenMedicoEmailAlreadyExists() throws Exception {
        // Given
        when(authService.medicoSignUp(any(MedicoRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException("Medico ya existe con este correo."));

        // When & Then
        mockMvc.perform(post("/api/auth/signup/medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicoRequestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Usuario ya existe"));
    }

    @Test
    @DisplayName("Should return 400 when medico data is invalid")
    void shouldReturn400WhenMedicoDataIsInvalid() throws Exception {
        // Given
        medicoRequestDto.setEmail("invalid-email"); // email inválido
        medicoRequestDto.setPassword(""); // password vacío

        // When & Then
        mockMvc.perform(post("/api/auth/signup/medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicoRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should sign in successfully with valid credentials")
    void shouldSignInSuccessfullyWithValidCredentials() throws Exception {
        // Given
        when(authService.signin(any(SignInDto.class))).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("jwt-token-12345"));
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        // Given
        when(authService.signin(any(SignInDto.class)))
                .thenThrow(new InvalidCredentialsException("Credenciales inválidas"));

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("Should return 400 when signin data is invalid")
    void shouldReturn400WhenSigninDataIsInvalid() throws Exception {
        // Given
        signInDto.setEmail(""); // email vacío
        signInDto.setPassword(""); // password vacío

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when request body is missing")
    void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when JSON is malformed")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}
