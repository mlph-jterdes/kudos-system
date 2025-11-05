package com.kudos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    // Use a fixed secret key (HS256). You can also load this from
    // application.properties
    private static final String SECRET = "MySuperSecretKeyForJwtGenerationWhichShouldBeLongEnough123!";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Token expiration time (e.g., 1 hour)
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    /**
     * Generate a JWT token for the given email
     */
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, email);
    }

    /**
     * Creates a valid jwt
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    /**
     * Extract email (subject) from JWT token
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Validate the token by checking email and expiration
     */
    public boolean validateToken(String token, String email) {
        String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * Get all claims from token
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    }
}
