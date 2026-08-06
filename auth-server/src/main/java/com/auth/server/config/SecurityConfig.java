/* 
 * This project uses a custom JWT approach for learning purposes.
 * 
 * Trade-offs vs OAuth2/OIDC with Keycloak/Auth0:
 * - Custom JWT: Full control, simpler to understand, but you own all security logic 
 *   (token revocation, refresh tokens, key rotation) — easy to get wrong.
 * - OAuth2/Keycloak: Battle-tested, handles refresh/revocation/SSO/MFA out of the box, 
 *   but more complex setup, external dependency.
 * - For production multi-service architectures, OAuth2 with an identity provider is strongly recommended.
 * - This custom approach is fine for learning the fundamentals of how JWT auth works under the hood.
 */
package com.auth.server.config;

import com.auth.server.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // WHY: Stateless APIs don't use cookies/sessions, so CSRF is irrelevant. 
        // Tokens in the Authorization header aren't auto-attached by browsers for cross-site requests.
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // WHY: Spring Boot routes internal errors to /error. 
                // If this is blocked, clients receive 401s instead of actual error messages.
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            // WHY: Core principle of stateless architectures — the server stores NO session state. 
            // Each request must carry its own JWT token. This makes scaling microservices easy.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // WHY: Our custom JWT filter must run BEFORE the standard UsernamePasswordAuthenticationFilter 
            // so we can set the authentication in the SecurityContext before Spring attempts to check it.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // WHY: H2 console runs inside a frame. Spring Security blocks iframes by default (X-Frame-Options) 
            // to prevent clickjacking. We set it to sameOrigin for dev purposes only. Remove in production.
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    /*
     * BCrypt is a one-way adaptive hashing function. 
     * 'Adaptive' means the work factor can be increased as hardware improves. 
     * Plaintext passwords are unsafe because: 
     * (1) database breaches expose all credentials, 
     * (2) users reuse passwords across services, 
     * (3) even 'encrypted' passwords are reversible — hashing is not. 
     * BCrypt adds a unique salt per password, defeating rainbow table attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // WHY: Spring uses the AuthenticationManager to orchestrate the authentication process.
    // It delegates to our CustomUserDetailsService and PasswordEncoder to verify credentials.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
