package com.auth.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // WHY: HMAC-SHA256 (symmetric) is simpler and faster because it uses a single shared secret key 
    // for both signing and verification. This is ideal for microservices where the auth server 
    // and resource server share the same environment/configuration.
    public String generateToken(String username, String roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRoles(String token) {
        return extractAllClaims(token).get("roles", String.class);
    }

    // WHY: Token validation must catch and explicitly handle various JWT exceptions.
    // This covers: 
    // 1. Signature validity (tampering detection)
    // 2. Expiration (token has lived past its allowed time)
    // 3. Malformation (token structure is broken or invalid)
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // WHY: We use Jwts.parser() configured with our specific signing key to verify the signature.
    // If the token was altered by a client, the signature won't match and parseSignedClaims will throw an exception.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // WHY: Raw string passwords shouldn't be used directly as keys. The JWT spec requires 
    // keys of specific bit lengths based on the algorithm (e.g., 256 bits for HS256). 
    // Decoding a base64 string ensures the key bytes map exactly to the cryptographic requirements.
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
