package ecampus.academy.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ecampus.academy.project.model.User;
import ecampus.academy.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

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
    public String registerUser(@ModelAttribute("user") User user,
                               BindingResult result, Model model) {
        if (result.hasErrors()) return "register";
        try {
            userService.save(user);
        } catch (Exception e) {
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }
        return "redirect:/login";
    }

@GetMapping("/login")
public String showLoginForm(@RequestParam(value="error",  required=false) String err,
                            @RequestParam(value="locked", required=false) String lock,
                            HttpServletRequest request,
                            Model model) {

    if (err != null)  model.addAttribute("loginError", true);

    if (lock != null) {
        String lastUser = (String) request.getSession()
                             .getAttribute("SPRING_SECURITY_LAST_USERNAME");

        userService.findByUsername(lastUser).ifPresent(u -> {
            model.addAttribute("accountLocked", true);
            model.addAttribute("lockUntil", u.getLockUntil());   // <‑‑  **lockUntil**
        });
    }
    return "login";
}
}
