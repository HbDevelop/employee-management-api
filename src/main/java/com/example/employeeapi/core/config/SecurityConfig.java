package com.example.employeeapi.core.config;

import com.example.employeeapi.authentication.JwtAuthFilter;
import com.example.employeeapi.authentication.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Règles de sécurité : quelles routes sont protégées, et comment. */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(AppProperties properties) {
        return new BCryptPasswordEncoder(properties.bcryptStrength());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService, ObjectMapper objectMapper)
            throws Exception {
        http
                // Pas de session ni de formulaire de connexion : chaque requête porte son jeton
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Seules les routes des employés demandent d'être connecté
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/employees/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new JwtAuthFilter(jwtService, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
