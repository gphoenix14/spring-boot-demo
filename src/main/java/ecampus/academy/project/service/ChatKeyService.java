package ecampus.academy.project.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecampus.academy.project.model.ChatKey;
import ecampus.academy.project.model.User;
import ecampus.academy.project.repository.ChatKeyRepository;
import ecampus.academy.project.repository.UserRepository;
import ecampus.academy.project.util.CryptoUtils;

@Service
public class ChatKeyService {

    private final ChatKeyRepository repo;
    private final UserRepository userRepo;
    private final SecureRandom rnd = new SecureRandom();

    public ChatKeyService(ChatKeyRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    /**
     * Recupera (o crea) la chat‑key tra due utenti (usernames).
     * L'ordine viene normalizzato per mantenere la unique constraint (user_a_id,user_b_id).
     */
    @Transactional
    public ChatKey getOrCreate(String usernameA, String usernameB) {

        User ua = userRepo.findByUsername(usernameA)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + usernameA));
        User ub = userRepo.findByUsername(usernameB)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + usernameB));

        // normalizza
        User first  = ua.getId() < ub.getId() ? ua : ub;
        User second = ua.getId() < ub.getId() ? ub : ua;

        // prova esistente
        ChatKey existing = repo.findByUsers(first.getId(), second.getId()).orElse(null);
        if (existing != null) {
            return existing;
        }

        // genera AES 256
        byte[] aes = new byte[32];
        rnd.nextBytes(aes);

        // cifra per entrambi (RSA OAEP SHA‑256)
        String encForFirst  = CryptoUtils.rsaEncryptBytes(aes, first.getPublicKey());
        String encForSecond = CryptoUtils.rsaEncryptBytes(aes, second.getPublicKey());

        ChatKey ck = new ChatKey();
        ck.setUserA(first);
        ck.setUserB(second);
        ck.setKeyEncForA(encForFirst);
        ck.setKeyEncForB(encForSecond);
        ck.setCreatedAt(LocalDateTime.now());
        // ck.setKeyPlain(aes);  // solo runtime; @Transient nell'entità (opzionale)

        return repo.save(ck);
    }

    /**
     * Restituisce la chiave AES cifrata (RSA) per l'utente che la richiede.
     * Viene usata dal client per decriptare con la propria RSA privata (cookie).
     */
    @Transactional(readOnly = true)
    public String getEncryptedKeyFor(String requester, String partner) {

        User req = userRepo.findByUsername(requester)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + requester));
        User pt  = userRepo.findByUsername(partner)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + partner));

        User first  = req.getId() < pt.getId() ? req : pt;
        User second = req.getId() < pt.getId() ? pt  : req;

        ChatKey ck = repo.findByUsers(first.getId(), second.getId())
                .orElseThrow(() -> new IllegalStateException("Chat‑key assente (creata al primo invio privato)."));

        return req.getId().equals(first.getId()) ? ck.getKeyEncForA() : ck.getKeyEncForB();
    }
}
