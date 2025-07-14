/* === LoginSuccessHandler.java === */
package ecampus.academy.project.security;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import ecampus.academy.project.model.User;
import ecampus.academy.project.repository.UserRepository;
import ecampus.academy.project.util.CryptoUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *  Dopo un login riuscito:
 *  1.  Prende la password in chiaro dal form (request parameter).
 *  2.  Decritta la chiave RSA privata salvata in DB.
 *  3.  Salva la chiave (codificata Base64‑PKCS#8) in un cookie HttpOnly.
 *  4.  Reindirizza alla dashboard (/messages) o alla SavedRequest.
 */
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

private final UserRepository userRepo;

@Autowired
public LoginSuccessHandler(UserRepository repo){
    this.userRepo = repo;
    setDefaultTargetUrl("/messages");        // destinazione di fallback
    setAlwaysUseDefaultTargetUrl(true);      // forziamo sempre /messages
}

@Override
public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                    Authentication auth) throws ServletException, IOException {

    String rawPwd = req.getParameter("password");   // password dal form
    if (rawPwd != null && !rawPwd.isBlank()) {
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ServletException("Utente non trovato"));
        try {
            String pem = Base64.getEncoder().encodeToString(
                    CryptoUtils.decryptPrivateKey(rawPwd.toCharArray(), user.getPrivateKeyEnc()).getEncoded());

            Cookie c = new Cookie("rsaPriv", pem);
            c.setHttpOnly(true);
            c.setSecure(req.isSecure());
            c.setPath("/");
            c.setMaxAge(3600);             // 1 ora
            res.addCookie(c);
        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/login?error");
            return;
        }
    }
    super.onAuthenticationSuccess(req, res, auth);
}
}
