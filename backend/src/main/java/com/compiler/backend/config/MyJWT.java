package com.compiler.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class MyJWT {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME =
            24 * 60 * 60 * 1000L;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(Integer userId, String email) {

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + EXPIRATION_TIME
                ))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims verifyToken(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer getUserId(String token) {

        Claims claims = verifyToken(token);

        return Integer.parseInt(claims.getSubject());
    }

    public String getEmail(String token) {

        return verifyToken(token).get("email", String.class);
    }
}
