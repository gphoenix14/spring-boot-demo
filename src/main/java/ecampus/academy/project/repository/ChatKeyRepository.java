package ecampus.academy.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ecampus.academy.project.model.ChatKey;

public interface ChatKeyRepository extends JpaRepository<ChatKey, Long> {

    /** restituisce la riga indipendentemente dall’ordine A–B / B–A */
    @Query("""
           SELECT ck FROM ChatKey ck
           WHERE (ck.userA.id = :u1 AND ck.userB.id = :u2)
              OR (ck.userA.id = :u2 AND ck.userB.id = :u1)
           """)
    Optional<ChatKey> findByUsers(Long u1, Long u2);
}
