package com.example.medigo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinVideoRoomResponseDto {
    private Boolean success;
    private String roomUrl;
    private String token;
    private String roomName;
    private Boolean isDoctor;
    private String message;
}
