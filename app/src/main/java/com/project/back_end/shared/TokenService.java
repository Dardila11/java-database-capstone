package com.project.back_end.shared;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final Map<String, RoleValidator> validators;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(List<RoleValidator> validators) {
        this.validators = validators.stream()
                .collect(Collectors.toMap(RoleValidator::role, v -> v));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token, String role) {
        try {
            String subject = extractEmail(token);
            RoleValidator validator = validators.get(role);
            return validator != null && validator.isValidSubject(subject);
        } catch (JwtException e) {
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during token validation for role '{}': {}", role, e.getMessage(), e);
            return false;
        }
    }
}
