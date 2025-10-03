package br.com.applogin.backend_applogin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.applogin.backend_applogin.auth.JwtAuthFilter;
import br.com.applogin.backend_applogin.auth.JwtUtil;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration-minutes}")
    private long expMinutes;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secret, expMinutes * 60);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/h2/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .headers(h -> h.frameOptions(f -> f.disable())) // H2 console
                .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
