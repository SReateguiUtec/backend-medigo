package com.example.medigo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String roomUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", unique = true, nullable = false)
    private Cita cita;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String dailyRoomId;

    @Column(columnDefinition = "TEXT")
    private String patientToken;

    @Column(columnDefinition = "TEXT")
    private String doctorToken;

    @Column(nullable = false)
    private Boolean recordingEnabled = true;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
        if (recordingEnabled == null) {
            recordingEnabled = true;
        }
    }
}
