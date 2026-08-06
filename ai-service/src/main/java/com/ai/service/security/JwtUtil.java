package com.ai.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

/*
 * WHY THIS EXISTS:
 * This service NEVER issues tokens. It only validates tokens issued by the auth-server.
 * Both services must share the same secret (HMAC-SHA256) for this to work.
 * In production, use asymmetric keys (RS256) where only the auth server has the private key.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // WHY: decode base64 secret into SecretKey using Keys.hmacShaKeyFor().
    // This key MUST match the auth-server's signing key exactly. If they differ, all tokens will be rejected.
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRoles(String token) {
        return extractAllClaims(token).get("roles", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // WHY: parse with Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // WHY: validation covers signature verification (was this token signed by our auth server?),
    // expiration check, and malformation detection.
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
