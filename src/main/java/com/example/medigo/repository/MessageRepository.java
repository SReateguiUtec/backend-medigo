package com.example.medigo.repository;

import com.example.medigo.domain.Message;
import com.example.medigo.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Obtener conversación entre dos usuarios
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversation(Long userId1, Long userId2);

    // Obtener lista de conversaciones únicas para un usuario
    @Query("SELECT DISTINCT u FROM Usuario u " +
            "INNER JOIN Message m ON (u.id = m.sender.id OR u.id = m.receiver.id) " +
            "WHERE (m.sender.id = :userId OR m.receiver.id = :userId) " +
            "AND u.id != :userId")
    List<Usuario> findConversationPartners(Long userId);

    // Contar mensajes no leídos
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessages(Long userId);

    // Eliminar conversación entre dos usuarios
    @Modifying
    @Query("DELETE FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.receiver.id = :userId1)")
    void deleteConversation(Long userId1, Long userId2);
}
