package com.dotori.dotori.auth.security;

import com.dotori.dotori.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Jwt 생성 및 검증
    private final String SECRET = "veryverysecretkeyveryverysecretkey"; // 32바이트 이상
    private final long EXPIRATION = 1000L * 60 * 60 * 24; // 24시간

    private byte[] getSigningKey() {
        return SECRET.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(getSigningKey()))
                .compact();
    }

    public String validateAndGetEmail(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(getSigningKey()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
