package com.dotori.dotori.auth.security;

import com.dotori.dotori.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    // Jwt 생성 및 검증
    private final String SECRET = ${JWT_SECRET_KEY};
    private final long EXPIRATION = ${JWT_EXPIRATION_TIME}; // 24시간

    private byte[] getSigningKey() {
        return SECRET.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(User user) {
        try {
            String token = Jwts.builder()
                    .subject(user.getEmail())
                    .claim("role", user.getRole())
                    .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                    .signWith(Keys.hmacShaKeyFor(getSigningKey()))
                    .compact();
            log.debug("JWT 토큰 생성 완료: email={}", user.getEmail());
            return token;
        } catch (Exception e) {
            log.error("JWT 토큰 생성 실패: email={}, error={}", user.getEmail(), e.getMessage());
            throw e;
        }
    }

    public String validateAndGetEmail(String token) {
        try {
            String email = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(getSigningKey()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            log.debug("JWT 토큰 검증 성공: email={}", email);
            return email;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("잘못된 JWT 토큰: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 예외 발생: {}", e.getMessage());
            throw e;
        }
    }
}
