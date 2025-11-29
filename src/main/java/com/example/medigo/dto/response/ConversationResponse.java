package com.example.medigo.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
@Builder
public class ConversationResponse {
    private Long userId;
    private String userName;
    private String userRole;
    private String lastMessage;
    private ZonedDateTime lastMessageTime;
    private Long unreadCount;
    private String profilePicture;
}
