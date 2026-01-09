package com.dotori.dotori.auth.controller;

import com.dotori.dotori.auth.dto.LoginRequest;
import com.dotori.dotori.auth.dto.LoginResponse;
import com.dotori.dotori.auth.dto.SignupRequest;
import com.dotori.dotori.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        log.info("회원가입 요청: email={}, name={}, nickname={}", 
                request.getEmail(), request.getName(), request.getNickname());
        try {
            authService.signup(request);
            log.info("회원가입 성공: email={}", request.getEmail());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("message", "signup success"));
        } catch (Exception e) {
            log.error("회원가입 실패: email={}, error={}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        log.info("로그인 시도: email={}", request.getEmail());
        try {
            LoginResponse response = authService.login(request);
            log.info("로그인 성공: email={}", request.getEmail());
            return response;
        } catch (Exception e) {
            log.warn("로그인 실패: email={}, error={}", request.getEmail(), e.getMessage());
            throw e;
        }
    }
}
