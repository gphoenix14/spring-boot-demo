package ecampus.academy.project.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import ecampus.academy.project.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepo;
    private final int  maxAttempts;
    private final int  lockMinutes;

    public LoginFailureHandler(UserRepository repo,
                               @Value("${security.login.max-attempts}") int max,
                               @Value("${security.login.lock-minutes}") int lock) {
        this.userRepo    = repo;
        this.maxAttempts = max;
        this.lockMinutes = lock;
        setDefaultFailureUrl("/login?error");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse res,
                                        AuthenticationException ex)
                                        throws IOException, ServletException {

        String username = req.getParameter("username");

        /* -------------------- update tentativi / lock -------------------- */
        userRepo.findByUsername(username).ifPresent(u -> {
            if (u.getLockUntil()!=null && u.getLockUntil().isAfter(LocalDateTime.now()))
                return;                                    // già bloccato

            u.setFailedAttempts(u.getFailedAttempts() + 1);

            if (u.getFailedAttempts() >= maxAttempts) {
                u.setLockUntil(LocalDateTime.now().plusMinutes(lockMinutes));
                u.setFailedAttempts(0);
            }
            userRepo.save(u);
        });

        boolean lockedNow = userRepo.findByUsername(username)
                            .map(u -> u.getLockUntil()!=null &&
                                      u.getLockUntil().isAfter(LocalDateTime.now()))
                            .orElse(false);

        /* ----------------------------------------------------------------- */
        /*     ►►  qui salviamo NOI il nome utente in sessione  ◄◄           */
        /* ----------------------------------------------------------------- */
        if (lockedNow) {
            req.getSession(true)
               .setAttribute("SPRING_SECURITY_LAST_USERNAME", username);

            getRedirectStrategy().sendRedirect(req, res, "/login?locked");
        } else {
            super.onAuthenticationFailure(req, res, ex);
        }
    }
}
