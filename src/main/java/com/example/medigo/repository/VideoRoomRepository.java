package com.example.medigo.repository;

import com.example.medigo.domain.VideoRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRoomRepository extends JpaRepository<VideoRoom, Long> {
    Optional<VideoRoom> findByRoomName(String roomName);
    Optional<VideoRoom> findByCitaId(Long citaId);
}