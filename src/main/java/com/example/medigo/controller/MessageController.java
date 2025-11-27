package com.example.medigo.controller;

import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.SendMessageRequest;
import com.example.medigo.dto.response.ConversationResponse;
import com.example.medigo.dto.response.MessageResponse;
import com.example.medigo.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    // REST endpoint para enviar mensaje
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {

        Usuario sender = (Usuario) authentication.getPrincipal();
        MessageResponse response = messageService.sendMessage(request, sender.getId());

        // Enviar mensaje en tiempo real vía WebSocket a AMBOS usuarios
        // Al receptor
        messagingTemplate.convertAndSend(
                "/topic/chat." + request.getReceiverId(),
                response);

        // Al remitente (para sincronizar múltiples ventanas/dispositivos)
        messagingTemplate.convertAndSend(
                "/topic/chat." + sender.getId(),
                response);

        return ResponseEntity.ok(response);
    }

    // WebSocket endpoint para mensajes en tiempo real
    @MessageMapping("/chat.send")
    public void sendMessageViaWebSocket(@Payload SendMessageRequest request, Authentication authentication) {
        Usuario sender = (Usuario) authentication.getPrincipal();
        MessageResponse response = messageService.sendMessage(request, sender.getId());

        // Enviar mensaje en tiempo real vía WebSocket a AMBOS usuarios
        // Al receptor
        messagingTemplate.convertAndSend(
                "/topic/chat." + request.getReceiverId(),
                response);

        // Al remitente (para sincronizar múltiples ventanas/dispositivos)
        messagingTemplate.convertAndSend(
                "/topic/chat." + sender.getId(),
                response);
    }

    // Obtener conversación con un usuario
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {

        Usuario currentUser = (Usuario) authentication.getPrincipal();
        List<MessageResponse> messages = messageService.getConversation(currentUser.getId(), userId);
        return ResponseEntity.ok(messages);
    }

    // Obtener lista de conversaciones
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(Authentication authentication) {
        Usuario currentUser = (Usuario) authentication.getPrincipal();
        List<ConversationResponse> conversations = messageService.getConversations(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }

    // Marcar mensaje como leído
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long messageId,
            Authentication authentication) {

        Usuario currentUser = (Usuario) authentication.getPrincipal();
        messageService.markAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    // Eliminar conversación con un usuario
    @DeleteMapping("/conversation/{userId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long userId,
            Authentication authentication) {

        Usuario currentUser = (Usuario) authentication.getPrincipal();
        messageService.deleteConversation(currentUser.getId(), userId);
        return ResponseEntity.ok().build();
    }
}
