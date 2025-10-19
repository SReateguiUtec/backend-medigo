package com.example.medigo.controller;

import com.example.medigo.dto.request.CreateAdminRequestDto;
import com.example.medigo.dto.response.UpdateEstadoCuentaDto;
import com.example.medigo.dto.response.UsuarioResponseDto;
import com.example.medigo.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateEstadoCuentaDto statusDto) {

        adminService.updateUserAccountStatus(userId, statusDto.getEstadoCuenta());
        return ResponseEntity.ok("El estado de la cuenta del usuario " + userId + " ha sido actualizado.");
    }
    @GetMapping("/users/{userId}")
    public ResponseEntity<Object> getUserDetails(@PathVariable Long userId) {
        Object userDetails = adminService.getUserDetailsById(userId);
        return ResponseEntity.ok(userDetails);
    }
    @PostMapping("/admins")
    public ResponseEntity<UsuarioResponseDto> createAdmin(@Valid @RequestBody CreateAdminRequestDto request) {
        UsuarioResponseDto newAdmin = adminService.createAdmin(request);
        return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);
    }
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}