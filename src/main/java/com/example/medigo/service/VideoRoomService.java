package com.example.medigo.service;

import com.example.medigo.config.DailyConfig;
import com.example.medigo.domain.*;
import com.example.medigo.dto.response.JoinVideoRoomResponseDto;
import com.example.medigo.dto.response.VideoRoomResponseDto;
import com.example.medigo.exceptions.InvalidCredentialsException;
import com.example.medigo.exceptions.ResourceNotFoundException;
import com.example.medigo.repository.VideoRoomRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoRoomService {

    private final VideoRoomRepository videoRoomRepository;
    private final CitaService citaService;
    private final DailyConfig dailyConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public VideoRoom createVideoRoomForCita(Long citaId) {

        // Verificar si ya existe una sala para esta cita
        Optional<VideoRoom> existingRoom = videoRoomRepository.findByCitaId(citaId);
        if (existingRoom.isPresent()) {
            VideoRoom room = existingRoom.get();
            // Verificar si la sala ha expirado
            if (ZonedDateTime.now().isAfter(room.getExpiresAt())) {
                room.setStatus("EXPIRED");
                videoRoomRepository.save(room);
                throw new IllegalStateException("La sala de video ha expirado");
            }
            log.info("Sala de video ya existe para cita ID: {}", citaId);
            return room;
        }

        // Obtener la cita, permitir crear sala para citas pendientes o confirmadas
        Cita cita = citaService.findCitaById(citaId);
        if (cita.getEstado() != EstadoCita.CONFIRMADA && cita.getEstado() != EstadoCita.PENDIENTE) { 
            throw new IllegalStateException("La cita no está en un estado válido para crear una sala de video");
        }
        
        // Verificar que esté dentro del tiempo permitido para crear la sala (solo a la hora de inicio o después)
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime appointmentTime = cita.getFechaHora();
        ZonedDateTime timeWindowEnd = appointmentTime.plusHours(1); // 1 hora después (duración máxima de la videollamada)
        
        if (now.isBefore(appointmentTime)) {
            throw new IllegalStateException("La sala de video solo puede crearse a partir de la hora programada de inicio de la cita: " + appointmentTime.toString());
        }
        
        if (now.isAfter(timeWindowEnd)) {
            throw new IllegalStateException("La sala de video ya no está disponible. La cita programada ha expirado.");
        }
        
        // Generar nombre único para la sala
        String roomName = "medigo-cita-" + citaId + "-" + System.currentTimeMillis();
        // Calcular tiempo de expiración (1 hora después de la cita)
        ZonedDateTime expirationTime = cita.getFechaHora().plusHours(1);
        long expirationEpoch = expirationTime.toInstant().getEpochSecond();

        // Preparar request para Daily API
        Map<String, Object> roomRequest = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put("exp", expirationEpoch);
        properties.put("enable_recording", false);
        properties.put("start_audio_off", true);
        properties.put("start_video_off", true);
        roomRequest.put("name", roomName);
        roomRequest.put("properties", properties);

        // Llamar a Daily API para crear la sala
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(roomRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar datos JSON: " + e.getMessage(), e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + dailyConfig.getApiKey());
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    dailyConfig.getApiUrl() + "/rooms",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode roomData;
                try {
                    roomData = objectMapper.readTree(response.getBody());
                } catch (Exception e) {
                    throw new RuntimeException("Error al procesar respuesta de Daily API: " + e.getMessage(), e);
                }
                String roomUrl = roomData.get("url").asText();
                String dailyRoomId = roomData.get("id").asText();

                // Generar tokens para paciente y médico
                String patientToken = generateMeetingToken(
                        roomName,
                        cita.getPaciente().getId().toString(),
                        cita.getPaciente().getEmail(),
                        false,
                        expirationEpoch
                );
                String doctorToken = generateMeetingToken(
                        roomName,
                        cita.getMedico().getId().toString(),
                        cita.getMedico().getEmail(),
                        true,
                        expirationEpoch
                );

                // Guardar información de la sala en la base de datos
                VideoRoom videoRoom = VideoRoom.builder()
                        .roomName(roomName)
                        .roomUrl(roomUrl)
                        .cita(cita)
                        .expiresAt(expirationTime)
                        .status("ACTIVE")
                        .dailyRoomId(dailyRoomId)
                        .patientToken(patientToken)
                        .doctorToken(doctorToken)
                        .recordingEnabled(false)
                        .build();

                VideoRoom savedRoom = videoRoomRepository.save(videoRoom);
                return savedRoom;
            } else {
                throw new RuntimeException("Error al crear sala en Daily API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al crear sala de video: " + e.getMessage(), e);
        }
    }

     // Generar un token de reunión seguro para acceder a la sala de video,
     // los tokens son auto-firmados usando la API key de Daily como secreto
    private String generateMeetingToken(String roomName, String userId, String userName,
                                        boolean isOwner, long expirationTime) {
        long nowInSeconds = System.currentTimeMillis() / 1000;

        // Crear claims del JWT para el token de Daily
        Map<String, Object> claims = new HashMap<>();
        claims.put("r", roomName);  // room name
        claims.put("d", dailyConfig.getDailyDomain());  // daily domain
        claims.put("user_id", userId);
        claims.put("user_name", userName);

        if (isOwner) {
            claims.put("is_owner", true);
            claims.put("enable_recording", false);
        }

        // Crear y firmar el token JWT
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(nowInSeconds * 1000))
                .setExpiration(new Date(expirationTime * 1000))
                .signWith(SignatureAlgorithm.HS256, dailyConfig.getApiKey())
                .compact();
        return token;
    }

     // Obtener sala de video por ID de cita
    @Transactional(readOnly = true)
    public VideoRoom getVideoRoomByCitaId(Long citaId) {
        VideoRoom room = videoRoomRepository.findByCitaId(citaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró sala de video para la cita ID: " + citaId
                ));
        if (ZonedDateTime.now().isAfter(room.getExpiresAt())) {
            room.setStatus("EXPIRED");
            videoRoomRepository.save(room);
            throw new IllegalStateException("La sala de video ha expirado");
        }
        return room;
    }


    // Obtener el token de acceso apropiado basado en el rol del usuario
    @Transactional(readOnly = true)
    public String getAccessToken(Long citaId, Usuario usuario) {
        VideoRoom room = getVideoRoomByCitaId(citaId);
        Cita cita = room.getCita();

        // Verificar autorizacion del usuario
        boolean isPaciente = usuario.getRol() == Rol.PACIENTE &&
                cita.getPaciente().getId().equals(usuario.getId());
        boolean isMedico = usuario.getRol() == Rol.MEDICO &&
                cita.getMedico().getId().equals(usuario.getId());

        if (!isPaciente && !isMedico) {
            throw new InvalidCredentialsException("No tiene permiso para acceder a esta sala de video");
        }
        return isMedico ? room.getDoctorToken() : room.getPatientToken();
    }

    @Transactional
    public VideoRoomResponseDto createVideoRoomResponseDto(Long citaId) {
        VideoRoom room = getVideoRoomByCitaId(citaId);
        return VideoRoomResponseDto.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .roomUrl(room.getRoomUrl())
                .citaId(room.getCita().getId())
                .expiresAt(room.getExpiresAt())
                .status(room.getStatus())
                .recordingEnabled(room.getRecordingEnabled())
                .build();
    }
    @Transactional
    public JoinVideoRoomResponseDto createJoinResponseDto(Long citaId, Usuario usuario) {
        // First try to get the existing room
        VideoRoom room;
        try {
            room = getVideoRoomByCitaId(citaId);
        } catch (ResourceNotFoundException e) {
            // If room doesn't exist, try to create it
            try {
                room = createVideoRoomForCita(citaId);
            } catch (Exception creationException) {
                log.error("Failed to create video room for cita ID: {}", citaId, creationException);
                return createErrorJoinResponseDto("No se pudo crear la sala de video para la cita ID: " + citaId);
            }
        }
        
        // Verificar que esté dentro del tiempo permitido para unirse a la sala
        Cita cita = room.getCita();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime appointmentTime = cita.getFechaHora();
        ZonedDateTime timeWindowEnd = appointmentTime.plusHours(1); // 1 hora después (duración máxima de la videollamada)
        
        if (now.isBefore(appointmentTime)) {
            return createErrorJoinResponseDto("El acceso a la videollamada solo está permitido a partir de la hora programada de inicio de la cita: " + appointmentTime.toString());
        }
        
        if (now.isAfter(timeWindowEnd)) {
            return createErrorJoinResponseDto("El acceso a la videollamada ya no está disponible. La cita programada ha expirado.");
        }
        
        String accessToken = getAccessToken(citaId, usuario);
        boolean isDoctor = usuario.getRol() == Rol.MEDICO;
        
        return JoinVideoRoomResponseDto.builder()
                .success(true)
                .roomUrl(room.getRoomUrl())
                .token(accessToken)
                .roomName(room.getRoomName())
                .isDoctor(isDoctor)
                .message("Acceso a sala de video concedido")
                .build();
    }
    @Transactional(readOnly = true)
    public JoinVideoRoomResponseDto createErrorJoinResponseDto(String message) {
        return JoinVideoRoomResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
}