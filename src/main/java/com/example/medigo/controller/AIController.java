package com.example.medigo.controller;

import com.example.medigo.domain.HistorialMedico;
import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.AIConsultaRequest;
import com.example.medigo.dto.AIConsultaResponse;
import com.example.medigo.service.HistorialMedicoService;
import com.example.medigo.service.MedicalAIService;
import com.example.medigo.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final MedicalAIService medicalAIService;
    private final HistorialMedicoService historialMedicoService;
    private final RateLimitService rateLimitService;

    private static final String DISCLAIMER = 
            "⚠️ IMPORTANTE: Esta información es solo educativa y se basa en tu historial médico existente. " +
            "NO reemplaza la consulta con un profesional de la salud. Para cualquier duda o decisión médica, " +
            "consulta siempre con tu médico.";

    @PostMapping("/consultar-historial")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<?> consultarHistorial(
            @RequestBody AIConsultaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        
        try {
            // Validar que la pregunta no esté vacía
            if (request.getPregunta() == null || request.getPregunta().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La pregunta no puede estar vacía"));
            }

            // Verificar rate limit
            if (!rateLimitService.isAllowed(usuario.getId())) {
                int remaining = rateLimitService.getRemainingRequests(usuario.getId());
                long timeUntilReset = rateLimitService.getTimeUntilReset(usuario.getId());
                
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(AIConsultaResponse.builder()
                                .respuesta("Has alcanzado el límite de consultas por hora.")
                                .disclaimer(DISCLAIMER)
                                .consultasRestantes(remaining)
                                .tiempoHastaReset(timeUntilReset)
                                .build());
            }

            // Obtener historial médico del paciente
            List<HistorialMedico> historial = historialMedicoService.getByPacienteId(usuario.getId());
            
            if (historial.isEmpty()) {
                return ResponseEntity.ok(AIConsultaResponse.builder()
                        .respuesta("Aún no tienes historial médico en la plataforma. " +
                                "El historial se crea después de tus consultas con los médicos.")
                        .disclaimer(DISCLAIMER)
                        .consultasRestantes(rateLimitService.getRemainingRequests(usuario.getId()))
                        .tiempoHastaReset(rateLimitService.getTimeUntilReset(usuario.getId()))
                        .build());
            }

            // Consultar a la IA
            String respuesta = medicalAIService.consultarHistorial(historial, request.getPregunta());
            
            // Log de auditoría
            log.info("Consulta AI - Usuario: {}, Pregunta: {}", 
                    usuario.getId(), 
                    request.getPregunta().substring(0, Math.min(50, request.getPregunta().length())));

            return ResponseEntity.ok(AIConsultaResponse.builder()
                    .respuesta(respuesta)
                    .disclaimer(DISCLAIMER)
                    .consultasRestantes(rateLimitService.getRemainingRequests(usuario.getId()))
                    .tiempoHastaReset(rateLimitService.getTimeUntilReset(usuario.getId()))
                    .build());

        } catch (Exception e) {
            log.error("Error en consulta AI", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar la consulta. Por favor, intenta nuevamente."));
        }
    }

    @GetMapping("/consultas-restantes")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Map<String, Object>> getConsultasRestantes(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(Map.of(
                "consultasRestantes", rateLimitService.getRemainingRequests(usuario.getId()),
                "tiempoHastaReset", rateLimitService.getTimeUntilReset(usuario.getId())
        ));
    }
}
