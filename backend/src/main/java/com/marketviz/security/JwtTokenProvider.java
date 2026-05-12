package com.marketviz.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Generates and validates HS256-signed JWT access tokens.
 *
 * <p>The signing key is derived from the {@code app.jwt.secret} configuration
 * property. In production this value must be supplied via the {@code JWT_SECRET}
 * environment variable and must be at least 32 characters to satisfy HS256
 * key-length requirements.
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds a signed JWT for the authenticated principal.
     *
     * @param authentication a successfully authenticated Spring Security context
     * @return compact, URL-safe JWT string
     */
    public String generateToken(Authentication authentication) {
        var principal = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey())
                .compact();
    }

    /**
     * Extracts the username (subject claim) from a validated token.
     *
     * @param token compact JWT string
     * @return the {@code sub} claim value
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Returns {@code true} iff the token signature is valid and the token is not expired.
     *
     * @param token compact JWT string
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
