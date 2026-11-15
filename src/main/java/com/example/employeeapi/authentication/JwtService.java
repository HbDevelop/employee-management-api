package com.example.employeeapi.authentication;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.example.employeeapi.core.config.AppProperties;
import com.example.employeeapi.users.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

/** Génération et vérification des JWT (HS256), même contenu que la version Express. */
@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final Duration ttl;

    public JwtService(AppProperties properties) {
        byte[] secret = properties.jwtSecret() == null
                ? new byte[0]
                : properties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET is missing or too short: HS256 requires at least " + MIN_SECRET_BYTES + " characters");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.ttl = properties.jwtTtl();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("_id", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * @throws io.jsonwebtoken.ExpiredJwtException si le token a expiré
     * @throws io.jsonwebtoken.JwtException        si le token est invalide (signature, format...)
     */
    public AuthUser verifyToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new AuthUser(
                claims.get("_id", String.class),
                claims.get("email", String.class),
                claims.get("firstName", String.class),
                claims.get("lastName", String.class));
    }
}
