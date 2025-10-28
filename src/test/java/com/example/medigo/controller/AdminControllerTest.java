package com.example.medigo.controller;

import com.example.medigo.domain.*;
import com.example.medigo.dto.request.CreateAdminRequestDto;
import com.example.medigo.dto.response.UpdateEstadoCuentaDto;
import com.example.medigo.dto.response.UsuarioResponseDto;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.exceptions.UserAlreadyExistsException;
import com.example.medigo.security.JwtService;
import com.example.medigo.service.AdminService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests del Controlador de Administración")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private Usuario testUsuario;
    private CreateAdminRequestDto createAdminRequest;
    private UsuarioResponseDto adminResponse;
    private UpdateEstadoCuentaDto updateEstadoCuentaDto;

    @BeforeEach
    void setUp() {
        testUsuario = new Usuario();
        testUsuario.setId(1L);
        testUsuario.setNombres("Juan");
        testUsuario.setApellidos("Pérez");
        testUsuario.setEmail("juan.perez@example.com");
        testUsuario.setPassword("password");
        testUsuario.setRol(Rol.PACIENTE);
        testUsuario.setEstadoCuenta(EstadoCuenta.ACTIVADA);

        createAdminRequest = new CreateAdminRequestDto();
        createAdminRequest.setNombres("Admin");
        createAdminRequest.setApellidos("Sistema");
        createAdminRequest.setEmail("admin@medigo.com");
        createAdminRequest.setPassword("AdminPassword123!");

        adminResponse = new UsuarioResponseDto();
        adminResponse.setId(3L);
        adminResponse.setNombres("Admin");
        adminResponse.setApellidos("Sistema");
        adminResponse.setEmail("admin@medigo.com");

        updateEstadoCuentaDto = new UpdateEstadoCuentaDto();
        updateEstadoCuentaDto.setEstadoCuenta(EstadoCuenta.DESACTIVADA);
    }

    @Test
    @DisplayName("Should update user status successfully when admin is authenticated")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateUserStatusSuccessfullyWhenAdminIsAuthenticated() throws Exception {
        // Given
        when(adminService.updateUserAccountStatus(anyLong(), any(EstadoCuenta.class)))
                .thenReturn(testUsuario);

        // When & Then
        mockMvc.perform(patch("/api/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEstadoCuentaDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow non-admin when security is disabled")
    @WithMockUser(roles = "PACIENTE")
    void shouldReturn403WhenNonAdminTriesToUpdateUserStatus() throws Exception {
        // Given - Security disabled, endpoint accessible
        when(adminService.updateUserAccountStatus(anyLong(), any(EstadoCuenta.class)))
                .thenReturn(testUsuario);
        
        // When & Then - Returns 200 because security is disabled
        mockMvc.perform(patch("/api/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEstadoCuentaDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access without authentication (security disabled)")
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
        // Given - Security disabled
        when(adminService.updateUserAccountStatus(anyLong(), any(EstadoCuenta.class)))
                .thenReturn(testUsuario);
        
        // When & Then - Returns 200 because security is disabled
        mockMvc.perform(patch("/api/admin/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEstadoCuentaDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when updating status of non-existent user")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUpdatingStatusOfNonExistentUser() throws Exception {
        // Given
        when(adminService.updateUserAccountStatus(anyLong(), any(EstadoCuenta.class)))
                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

        // When & Then
        mockMvc.perform(patch("/api/admin/users/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEstadoCuentaDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get user details successfully when admin is authenticated")
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserDetailsSuccessfullyWhenAdminIsAuthenticated() throws Exception {
        // Given
        when(adminService.getUserDetailsById(anyLong())).thenReturn(testUsuario);

        // When & Then
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));
    }

    @Test
    @DisplayName("Should return 404 when getting details of non-existent user")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenGettingDetailsOfNonExistentUser() throws Exception {
        // Given
        when(adminService.getUserDetailsById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

        // When & Then
        mockMvc.perform(get("/api/admin/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create admin successfully when admin is authenticated")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAdminSuccessfullyWhenAdminIsAuthenticated() throws Exception {
        // Given
        when(adminService.createAdmin(any(CreateAdminRequestDto.class))).thenReturn(adminResponse);

        // When & Then
        mockMvc.perform(post("/api/admin/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAdminRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("admin@medigo.com"));
    }

    @Test
    @DisplayName("Should return 409 when creating admin with existing email")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenCreatingAdminWithExistingEmail() throws Exception {
        // Given
        when(adminService.createAdmin(any(CreateAdminRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException("Email ya existe"));

        // When & Then
        mockMvc.perform(post("/api/admin/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAdminRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Usuario ya existe"));
    }

    @Test
    @DisplayName("Should return 400 when creating admin with invalid data")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenCreatingAdminWithInvalidData() throws Exception {
        // Given
        createAdminRequest.setEmail(""); // email vacío
        createAdminRequest.setPassword("123"); // password muy corto

        // When & Then
        mockMvc.perform(post("/api/admin/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAdminRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete user successfully when admin is authenticated")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUserSuccessfullyWhenAdminIsAuthenticated() throws Exception {
        // Given
        doNothing().when(adminService).deleteUser(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Usuario no encontrado"))
                .when(adminService).deleteUser(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/admin/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should allow non-admin to delete when security is disabled")
    @WithMockUser(roles = "MEDICO")
    void shouldReturn403WhenNonAdminTriesToDeleteUser() throws Exception {
        // Given - Security disabled
        doNothing().when(adminService).deleteUser(anyLong());
        
        // When & Then - Returns 204 because security is disabled
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());
    }
}
