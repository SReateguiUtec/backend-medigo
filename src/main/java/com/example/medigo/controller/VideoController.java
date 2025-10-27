package com.example.medigo.controller;

import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.response.JoinVideoRoomResponseDto;
import com.example.medigo.dto.response.VideoRoomResponseDto;
import com.example.medigo.service.VideoRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final VideoRoomService videoRoomService;

    @PostMapping("/rooms/cita/{citaId}")
    @PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO')")
    public ResponseEntity<VideoRoomResponseDto> createVideoRoomForCita(
            @PathVariable Long citaId,
            @AuthenticationPrincipal Usuario usuario) {
        videoRoomService.createVideoRoomForCita(citaId);
        VideoRoomResponseDto response = videoRoomService.createVideoRoomResponseDto(citaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/join/cita/{citaId}")
    @PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO')")
    public ResponseEntity<JoinVideoRoomResponseDto> joinVideoRoom(
            @PathVariable Long citaId,
            @AuthenticationPrincipal Usuario usuario) {
        JoinVideoRoomResponseDto response = videoRoomService.createJoinResponseDto(citaId, usuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms/cita/{citaId}")
    @PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO')")
    public ResponseEntity<VideoRoomResponseDto> getVideoRoomDetails(
            @PathVariable Long citaId,
            @AuthenticationPrincipal Usuario usuario) {
        VideoRoomResponseDto response = videoRoomService.createVideoRoomResponseDto(citaId);
        return ResponseEntity.ok(response);
    }
}
