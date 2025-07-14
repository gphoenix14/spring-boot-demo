package ecampus.academy.project.security;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import ecampus.academy.project.model.User;
import ecampus.academy.project.repository.UserRepository;
import ecampus.academy.project.util.CryptoUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepo;

    @Autowired
    public LoginSuccessHandler(UserRepository repo) {
        this.userRepo = repo;
        setDefaultTargetUrl("/messages");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication auth)
                                        throws ServletException, IOException {

        String rawPwd = req.getParameter("password");

        User user = userRepo.findByUsername(auth.getName())
                            .orElseThrow(() -> new ServletException("Utente non trovato"));

        /* reset contatore/lock */
        user.setFailedAttempts(0);
        user.setLockUntil(null);
        userRepo.save(user);

        /* salva rsaPriv nel cookie */
        if (rawPwd!=null && !rawPwd.isBlank()) {
            try {
                String pkcs8 = Base64.getEncoder().encodeToString(
                        CryptoUtils.decryptPrivateKey(rawPwd.toCharArray(),
                                                      user.getPrivateKeyEnc()).getEncoded());

                Cookie c = new Cookie("rsaPriv", pkcs8);
                c.setHttpOnly(false);
                c.setSecure(req.isSecure());
                c.setPath("/");
                c.setMaxAge(3600);
                res.addCookie(c);

            } catch (Exception e) {
                res.sendRedirect("/login?error");
                return;
            }
        }
        super.onAuthenticationSuccess(req, res, auth);
    }
}
