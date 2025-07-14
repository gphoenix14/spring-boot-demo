package ecampus.academy.project.config;

import ecampus.academy.project.security.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SshaPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            LoginSuccessHandler successHandler,
            LoginFailureHandler failureHandler) throws Exception {

        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login","/register","/css/**","/js/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(fl -> fl
                .loginPage("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler))
            .logout(lo -> lo.logoutUrl("/logout")
                            .logoutSuccessUrl("/login"));

        return http.build();
    }
}
