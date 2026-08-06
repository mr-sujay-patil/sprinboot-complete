package com.auth.server.security;

import com.auth.server.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        // WHY: The 'Bearer' scheme is the standard HTTP authentication scheme for OAuth 2.0 and JWTs.
        // It cleanly separates the token type from the token value itself in the Authorization header.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // WHY: We ALWAYS call doFilter to let the request proceed. 
            // We let the Spring Security filter chain decide if this unauthenticated request 
            // should be blocked or allowed (e.g., public endpoints).
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtUtil.extractUsername(jwt);

        // WHY: We check if SecurityContextHolder already has an authentication to avoid 
        // redundantly re-authenticating on every filter pass if it's already set somehow.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            if (jwtUtil.validateToken(jwt)) {
                // WHY: We reload the UserDetails from the database on every request to ensure 
                // the user still exists, hasn't had their roles changed, and hasn't been disabled/deleted.
                // Alternatively, for pure statelessness, you could build UserDetails directly from JWT claims.
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // WHY: Placing the AuthToken into the SecurityContextHolder is how Spring Security 
                // knows the request is authenticated for all downstream filters and controllers.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // ALWAYS pass the request forward in the chain
        filterChain.doFilter(request, response);
    }
}
