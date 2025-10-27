package com.example.medigo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoRoomResponseDto {
    private Long id;
    private String roomName;
    private String roomUrl;
    private Long citaId;
    private ZonedDateTime expiresAt;
    private String status;
    private Boolean recordingEnabled;
}