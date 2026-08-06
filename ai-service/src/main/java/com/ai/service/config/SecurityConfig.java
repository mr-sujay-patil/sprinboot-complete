package com.ai.service.config;

/*
 * WHY THIS CONFIGURATION EXISTS:
 * - This is a RESOURCE SERVER — it only validates tokens, never issues them
 * - The auth-server (port 9090) handles registration and login
 * - This service trusts tokens by validating the HMAC-SHA256 signature using the shared secret
 * - No UserDetailsService, no PasswordEncoder, no AuthenticationManager needed here
 * - The SecurityFilterChain is simpler than the auth-server's: just stateless + JWT filter
 */

import com.ai.service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // WHY: No PasswordEncoder bean. No AuthenticationManager bean. 
    // Resource server doesn't authenticate credentials, it validates tokens.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // WHY: csrf disabled because this is a stateless Bearer token API, not using cookies/sessions
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/api/ai/health").permitAll()
                // WHY: every AI call costs money, so we gate access behind authentication.
                .requestMatchers("/api/ai/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            // WHY: No headers/frameOptions needed (no H2 console in this service)
            
        return http.build();
    }
}
