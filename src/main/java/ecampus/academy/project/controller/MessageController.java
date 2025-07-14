package ecampus.academy.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter; // Importa la classe Sort corretta

import ecampus.academy.project.model.Message;
import ecampus.academy.project.repository.UserRepository;
import ecampus.academy.project.service.MessageService;


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

        String currentUsername =
            SecurityContextHolder.getContext().getAuthentication().getName();

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Message> messagePage = messageService.findAllRelevantMessages(currentUsername, pageable);

        model.addAttribute("messagePage", messagePage);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentUser", currentUsername);          // ⬅️ serve nel template
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

    @PostMapping("/messages/delete")
    public String deleteMessage(@RequestParam("id") Long id) {
        String currentUsername =
            SecurityContextHolder.getContext().getAuthentication().getName();

        messageService.delete(id, currentUsername);
        return "redirect:/messages";
    }
}
