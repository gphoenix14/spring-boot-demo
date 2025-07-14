package ecampus.academy.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ecampus.academy.project.service.UserService;
import ecampus.academy.project.model.User;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register"; // Restituisce la vista di registrazione in caso di errori di binding
        }
        try {
            userService.save(user);
        } catch (Exception e) {
            model.addAttribute("registrationError", e.getMessage());
            return "register"; // Restituisce la vista di registrazione con messaggio di errore
        }
        return "redirect:/login"; // Reindirizzamento alla pagina di login in caso di successo
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
