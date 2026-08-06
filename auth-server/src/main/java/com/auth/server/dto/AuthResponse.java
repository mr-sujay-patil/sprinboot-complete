package com.auth.server.dto;

/**
 * DTO for Authentication Responses.
 * 
 * WHY: Keeping the response minimal reduces payload size and avoids leaking sensitive information.
 * Only the necessary data (the JWT and username for UI convenience) is returned.
 */
public record AuthResponse(
    String token,
    String username
) {
}
