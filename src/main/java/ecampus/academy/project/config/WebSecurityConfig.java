/* === WebSecurityConfig.java === */
package ecampus.academy.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import ecampus.academy.project.repository.UserRepository;
import ecampus.academy.project.security.LoginSuccessHandler;
import ecampus.academy.project.security.SshaPasswordEncoder;

@Configuration
public class WebSecurityConfig {

@Bean
public PasswordEncoder passwordEncoder(){ return new SshaPasswordEncoder(); }

@SuppressWarnings("removal")
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       UserRepository userRepo) throws Exception {

    http.csrf().disable()
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .formLogin(login -> login
                .loginPage("/login")
                .successHandler(new LoginSuccessHandler(userRepo))
                .permitAll())
        .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login"));

    return http.build();
}
}
