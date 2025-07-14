/* === PasswordCapturingAuthenticationFilter.java === */
package ecampus.academy.project.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class PasswordCapturingAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

public static final String RAW_PASS_ATTR="RAW_PASSWORD";

@Override
public org.springframework.security.core.Authentication attemptAuthentication(HttpServletRequest req,HttpServletResponse res){
String pwd=obtainPassword(req);
req.setAttribute(RAW_PASS_ATTR,pwd);
return super.attemptAuthentication(req,res);
}
}
