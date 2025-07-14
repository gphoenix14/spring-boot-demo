package ecampus.academy.project.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import ecampus.academy.project.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Recupera l’utente per username. */
    Optional<User> findByUsername(String username);

    /* ---------------------------------------------------------------
       Supporto per lock account / tentativi falliti di login          */
    @Transactional
    @Modifying
    @Query("""
           UPDATE User u
              SET u.failedAttempts = :attempts,
                  u.lockUntil      = :lockUntil
            WHERE u.username       = :username
           """)
    void updateLockInfo(@Param("username")    String        username,
                        @Param("attempts")    int           attempts,
                        @Param("lockUntil")   LocalDateTime lockUntil);
}
