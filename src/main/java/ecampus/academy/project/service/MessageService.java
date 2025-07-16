package ecampus.academy.project.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ecampus.academy.project.model.Message;
import ecampus.academy.project.model.User;
import ecampus.academy.project.repository.MessageRepository;
import ecampus.academy.project.repository.UserRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatKeyService chatKeyService;

    private final Set<SseEmitter> emitters =
            Collections.synchronizedSet(new HashSet<>());

    @Autowired
    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          ChatKeyService chatKeyService) {
        this.messageRepository = messageRepository;
        this.userRepository    = userRepository;
        this.chatKeyService    = chatKeyService;
    }

    /**
     * Salva un messaggio. Per i broadcast il contenuto resta in chiaro.
     * Per i privati il contenuto è atteso GIÀ cifrato lato client (AES‑GCM)
     * e viene salvato così com'è. Qui ci limitiamo a garantire che la chat‑key
     * esista (creandola se necessario).
     */
    public Message save(Message message, String senderUsername,
                        boolean isBroadcast, String receiverUsername) {

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("Mittente non trovato"));
        message.setSender(sender);

        if (!isBroadcast) {
            if (receiverUsername == null || receiverUsername.isBlank()) {
                throw new IllegalArgumentException("Destinatario mancante");
            }

            User receiver = userRepository.findByUsername(receiverUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Destinatario non trovato"));
            message.setReceiver(receiver);
            message.setBroadcast(false);

            // assicura chat‑key (creata se prima volta)
            chatKeyService.getOrCreate(senderUsername, receiverUsername);

            // NESSUNA cifratura server: content già cifrato lato client.
            // (Se vuoi fallback server-side, avvisami e lo aggiungiamo.)
        } else {
            message.setReceiver(null);
            message.setBroadcast(true);
        }

        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        return messageRepository.save(message);
    }

    public Page<Message> findAllRelevantMessages(String currentUsername, Pageable pageable) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        return messageRepository.findRelevantMessages(current.getId(), pageable);
    }

    public void delete(Long messageId, String currentUsername) {
        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Messaggio non trovato"));
        if (!m.getSender().getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Non puoi cancellare messaggi altrui!");
        }
        messageRepository.delete(m);
    }

    public void addEmitter(SseEmitter e)   { emitters.add(e); }
    public void removeEmitter(SseEmitter e){ emitters.remove(e); }
}
