package ecampus.academy.project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ecampus.academy.project.model.ChatKey;
import ecampus.academy.project.service.ChatKeyService;

@RestController
public class ChatKeyController {

    private final ChatKeyService chatKeyService;

    public ChatKeyController(ChatKeyService chatKeyService) {
        this.chatKeyService = chatKeyService;
    }

    /**
     * Ritorna la chiave AES cifrata (per il richiedente) **creandola se non esiste**.
     * Il client la userà per cifrare il primo messaggio privato.
     */
    @GetMapping("/chat-key")
    public String getKey(@RequestParam("partner") String partner, Authentication auth) {
        String me = auth.getName();
        ChatKey ck = chatKeyService.getOrCreate(me, partner); // garantisce esistenza
        // restituisci la versione cifrata per "me"
        return ck.getUserA().getUsername().equals(me) ? ck.getKeyEncForA()
                                                      : ck.getKeyEncForB();
    }
}
