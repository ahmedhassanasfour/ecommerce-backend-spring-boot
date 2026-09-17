package com.ahmed.ecommerce.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String ISSUER = "ecommerce-api";
    private static final String TOKEN_TYPE = "token_type";

    private final SecretKey key;

    public JwtService(
            @Value("${JWT_SECRET}") String secretKey
    ) {
        this.key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(String email) {

        long expiration = 1000L * 60 * 15;

        return Jwts.builder()
                .subject(email)
                .issuer(ISSUER)
                .claim(TOKEN_TYPE, "ACCESS")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + expiration)
                )
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String email) {

        long expiration = 1000L * 60 * 60 * 24 * 7;

        return Jwts.builder()
                .subject(email)
                .issuer(ISSUER)
                .claim(TOKEN_TYPE, "REFRESH")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + expiration)
                )
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {

        return getClaims(token)
                .getSubject();
    }

    public boolean isRefreshToken(String token) {

        return "REFRESH".equals(
                getClaims(token).get(TOKEN_TYPE, String.class)
        );
    }

    private Claims getClaims(String token) {

        return Jwts.parser()
                .requireIssuer(ISSUER)
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}