package com.auth.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint, no authentication needed.";
    }

    @GetMapping("/secured")
    public String securedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // WHY: The Principal gets populated by our JwtAuthenticationFilter when it parses 
        // the incoming JWT, validates it, and places the UsernamePasswordAuthenticationToken 
        // into the SecurityContextHolder. Because of this, our controllers can securely 
        // access the authenticated user's details without manually parsing the token again.
        String currentPrincipalName = authentication.getName();
        
        log.info("Accessed secured endpoint by user: {}", currentPrincipalName);
        return "This is a secured endpoint. Authenticated user: " + currentPrincipalName;
    }
}
