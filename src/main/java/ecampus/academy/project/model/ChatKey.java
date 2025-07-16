package ecampus.academy.project.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "chat_keys",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_a_id","user_b_id"})
)
public class ChatKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* L’ordine A/B è solo normalizzazione (minId<maxId in service) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id")
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id")
    private User userB;

    @Column(name = "key_enc_fora", nullable = false, columnDefinition = "TEXT")
    private String keyEncForA;   // AES cifrata con RSA di userA

    @Column(name = "key_enc_forb", nullable = false, columnDefinition = "TEXT")
    private String keyEncForB;   // AES cifrata con RSA di userB

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /* NON persistiamo la chiave in chiaro! */
    @Transient
    private byte[] keyPlain;     // opzionale runtime

    /* ===== getters/setters ===== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUserA() { return userA; }
    public void setUserA(User userA) { this.userA = userA; }

    public User getUserB() { return userB; }
    public void setUserB(User userB) { this.userB = userB; }

    public String getKeyEncForA() { return keyEncForA; }
    public void setKeyEncForA(String keyEncForA) { this.keyEncForA = keyEncForA; }

    public String getKeyEncForB() { return keyEncForB; }
    public void setKeyEncForB(String keyEncForB) { this.keyEncForB = keyEncForB; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public byte[] getKeyPlain() { return keyPlain; }
    public void setKeyPlain(byte[] keyPlain) { this.keyPlain = keyPlain; }
}
