package com.auth.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for Authentication Requests (Login/Register).
 * 
 * WHY: Using a Java record (introduced in Java 14) creates an immutable data carrier.
 * It automatically provides constructors, getters (e.g., username()), equals, hashCode, and toString.
 */
public record AuthRequest(
    // WHY: Validation is critical for auth endpoints to prevent empty credentials or overly short passwords.
    // @NotBlank ensures the string is not null and has at least one non-whitespace character.
    @NotBlank(message = "Username cannot be blank") 
    String username,
    
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password
) {
}
