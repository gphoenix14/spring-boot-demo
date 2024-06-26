package criff.academy.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import criff.academy.project.service.MessageService;
import criff.academy.project.model.Message;
import criff.academy.project.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort; // Importa la classe Sort corretta


@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/messages")
    public String viewMessages(Model model, 
                               @RequestParam(defaultValue = "0") int page, 
                               @RequestParam(defaultValue = "10") int size) {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // Crea l'oggetto Pageable con ordinamento discendente basato sul timestamp
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Message> messagePage = messageService.findAllRelevantMessages(currentUsername, pageable);
        
        model.addAttribute("messagePage", messagePage);
        model.addAttribute("users", userRepository.findAll());
        return "messages";
    }

    @GetMapping("/messages/stream")
    public SseEmitter streamMessages() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        messageService.addEmitter(emitter);

        emitter.onCompletion(() -> messageService.removeEmitter(emitter));
        emitter.onTimeout(() -> messageService.removeEmitter(emitter));

        return emitter;
    }

    @PostMapping("/messages/send")
    public String sendMessage(@ModelAttribute("message") Message message, 
                              @RequestParam(required = false) String receiverUsername, 
                              @RequestParam(defaultValue = "false") boolean sendPrivate) {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isBroadcast = !sendPrivate;
        
        messageService.save(message, username, isBroadcast, receiverUsername);
        return "redirect:/messages";
    }
}
