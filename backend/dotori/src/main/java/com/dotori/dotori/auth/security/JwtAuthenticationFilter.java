package com.dotori.dotori.auth.security;

import com.dotori.dotori.user.entity.User;
import com.dotori.dotori.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Authorization 헤더 검사 → JWT 인증 등록
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String requestPath = request.getRequestURI();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("JWT 토큰 없음: path={}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtTokenProvider.validateAndGetEmail(token);
            log.debug("JWT 토큰 검증 성공: email={}, path={}", email, requestPath);

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, null, null
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 컨텍스트 설정 완료: userId={}, email={}, path={}", 
                        user.getId(), user.getEmail(), requestPath);
            } else {
                log.warn("토큰은 유효하지만 사용자를 찾을 수 없음: email={}, path={}", email, requestPath);
            }
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 실패: path={}, error={}", requestPath, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
