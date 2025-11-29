package com.example.medigo.service;

import com.example.medigo.domain.HistorialMedico;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    private static final String SYSTEM_PROMPT = """
            Eres un asistente médico educativo para la plataforma MediGO. Tu función es ÚNICAMENTE explicar y aclarar 
            información que YA existe en el historial médico del paciente.
            
            RESTRICCIONES ESTRICTAS:
            - NO puedes dar diagnósticos nuevos
            - NO puedes recetar medicamentos
            - NO puedes dar consejos médicos específicos
            - SOLO puedes explicar términos médicos y aclarar información existente en el historial
            - SIEMPRE debes recomendar consultar con un médico para dudas específicas
            - Responde en español de manera clara, empática y educativa
            - Si te piden algo fuera de tu alcance, explica amablemente que solo puedes explicar información existente
            
            Formato de respuesta:
            - Sé conciso pero completo
            - Usa lenguaje simple y comprensible
            - Si hay términos médicos, explícalos
            - Termina recordando que esta es información general y que deben consultar a su médico
            """;

    private final RestTemplate restTemplate = new RestTemplate();

    public String consultarHistorial(List<HistorialMedico> historial, String pregunta) {
        try {
            // Construir contexto del historial
            String contexto = construirContexto(historial);
            
            // Preparar el prompt completo
            String promptCompleto = SYSTEM_PROMPT + "\n\n" +
                    "HISTORIAL MÉDICO DEL PACIENTE:\n" + contexto + "\n\n" +
                    "PREGUNTA DEL PACIENTE: " + pregunta + "\n\n" +
                    "RESPUESTA:";

            // Llamar a Gemini API
            String respuesta = llamarGeminiAPI(promptCompleto);
            
            log.info("Consulta AI exitosa para pregunta: {}", pregunta.substring(0, Math.min(50, pregunta.length())));
            
            return respuesta;
            
        } catch (Exception e) {
            log.error("Error al consultar Gemini AI", e);
            return "Lo siento, hubo un error al procesar tu consulta. Por favor, intenta nuevamente más tarde.";
        }
    }

    private String construirContexto(List<HistorialMedico> historial) {
        if (historial == null || historial.isEmpty()) {
            return "No hay historial médico disponible.";
        }

        StringBuilder contexto = new StringBuilder();
        int count = 1;
        
        for (HistorialMedico h : historial) {
            contexto.append(String.format("Registro %d (Fecha: %s):\n", count++, h.getCreatedAt().toLocalDate()));
            contexto.append("- Diagnóstico: ").append(h.getDiagnostico()).append("\n");
            
            if (h.getReceta() != null && !h.getReceta().isEmpty()) {
                contexto.append("- Receta: ").append(h.getReceta()).append("\n");
            }
            
            if (h.getNotas() != null && !h.getNotas().isEmpty()) {
                contexto.append("- Notas: ").append(h.getNotas()).append("\n");
            }
            
            contexto.append("\n");
        }
        
        return contexto.toString();
    }

    @SuppressWarnings("unchecked")
    private String llamarGeminiAPI(String prompt) {
        try {
            log.info("Llamando a Gemini API con key: {}...", apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) : "NULL");
            
            String url = GEMINI_API_URL + "?key=" + apiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 1000
                )
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            log.info("Respuesta de Gemini API: Status {}", response.getStatusCode());
            
            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            
            log.error("No se encontró respuesta en el body de Gemini: {}", response.getBody());
            return "No se pudo obtener una respuesta de la IA.";
            
        } catch (Exception e) {
            log.error("Error llamando a Gemini API: {}", e.getMessage());
            log.error("Detalle completo del error:", e);
            throw new RuntimeException("Error al comunicarse con el servicio de IA: " + e.getMessage(), e);
        }
    }
}
