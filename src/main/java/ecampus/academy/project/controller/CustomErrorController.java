package ecampus.academy.project.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(Model model) {
        // Personalizza il messaggio di errore da visualizzare sulla pagina di errore
        model.addAttribute("errorMessage", "Si è verificato un errore. Si prega di riprovare più tardi.");
        return "error"; // Assicurati che ci sia un template "error.html" nella cartella "templates"
    }

    public String getErrorPath() {
        return "/error";
    }
}
