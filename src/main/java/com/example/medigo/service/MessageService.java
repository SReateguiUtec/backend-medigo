package com.example.medigo.service;

import com.example.medigo.domain.Message;
import com.example.medigo.domain.Usuario;
import com.example.medigo.dto.request.SendMessageRequest;
import com.example.medigo.dto.response.ConversationResponse;
import com.example.medigo.dto.response.MessageResponse;
import com.example.medigo.repository.MessageRepository;
import com.example.medigo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        Usuario sender = usuarioRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Usuario receiver = usuarioRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        return mapToResponse(message);
    }

    public List<MessageResponse> getConversation(Long userId, Long otherUserId) {
        List<Message> messages = messageRepository.findConversation(userId, otherUserId);
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ConversationResponse> getConversations(Long userId) {
        List<Usuario> partners = messageRepository.findConversationPartners(userId);
        return partners.stream()
                .map(partner -> buildConversationResponse(userId, partner))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getReceiver().getId().equals(userId)) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    @Transactional
    public void deleteConversation(Long userId, Long otherUserId) {
        messageRepository.deleteConversation(userId, otherUserId);
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getNombres() + " " + message.getSender().getApellidos())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getNombres() + " " + message.getReceiver().getApellidos())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private ConversationResponse buildConversationResponse(Long userId, Usuario partner) {
        List<Message> conversation = messageRepository.findConversation(userId, partner.getId());
        Message lastMessage = conversation.isEmpty() ? null : conversation.get(conversation.size() - 1);

        long unreadCount = conversation.stream()
                .filter(m -> m.getReceiver().getId().equals(userId) && !m.getIsRead())
                .count();

        return ConversationResponse.builder()
                .userId(partner.getId())
                .userName(partner.getNombres() + " " + partner.getApellidos())
                .userRole(partner.getRol().name())
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .unreadCount(unreadCount)
                .build();
    }
}
