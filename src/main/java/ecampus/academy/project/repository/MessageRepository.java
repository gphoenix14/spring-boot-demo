package ecampus.academy.project.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ecampus.academy.project.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
           SELECT m
           FROM   Message m
           WHERE  (m.sender.id = ?1 AND m.receiver.id = ?2)
               OR (m.sender.id = ?2 AND m.receiver.id = ?1)
           ORDER  BY m.timestamp ASC
           """)
    List<Message> findAllMessagesBetweenUsers(Long userId1, Long userId2);

    List<Message> findAllBySenderIdAndReceiverId(Long senderId, Long receiverId);

    /* Messaggi visibili all'utente loggato (broadcast + mittente + destinatario) */
    @Query("""
           SELECT m
           FROM   Message m
           WHERE  m.isBroadcast = true
              OR  m.sender.id   = ?1
              OR  m.receiver.id = ?1
           ORDER  BY m.timestamp DESC
           """)
    Page<Message> findRelevantMessages(Long userId, Pageable pageable);
}
