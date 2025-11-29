package com.example.medigo.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private Boolean isRead;
    private ZonedDateTime createdAt;
    private String senderProfilePicture;
    private String receiverProfilePicture;
}
