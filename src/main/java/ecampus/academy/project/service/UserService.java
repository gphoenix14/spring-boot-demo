package ecampus.academy.project.service;

import java.security.KeyPair;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ecampus.academy.project.model.User;
import ecampus.academy.project.repository.UserRepository;
import ecampus.academy.project.util.CryptoUtils;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* ------------------------------------------------------------------ */
    /** Ritorna l’utente, se presente, altrimenti {@code Optional.empty()}. */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    /* ------------------------------------------------------------------ */

    /** Registra un nuovo utente generando la coppia RSA e cifrando la chiave privata. */
    public User save(User user) {

        logger.info("Tentativo di salvare l'utente: {}", user.getUsername());

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            logger.warn("Utente già esistente: {}", user.getUsername());
            throw new RuntimeException(
                "Esiste già un account con quel nome utente: " + user.getUsername());
        }

        String rawPwd = user.getPassword();

        KeyPair kp = CryptoUtils.generateRsaKeyPair();
        user.setPublicKey(CryptoUtils.encodePublicKeySsh(kp.getPublic()));
        user.setPrivateKeyEnc(
            CryptoUtils.encryptPrivateKey(rawPwd.toCharArray(), kp.getPrivate()));
        user.setPassword(passwordEncoder.encode(rawPwd));

        User saved = userRepository.save(user);
        logger.info("Utente salvato con successo: {}", saved.getUsername());
        return saved;
    }
}
