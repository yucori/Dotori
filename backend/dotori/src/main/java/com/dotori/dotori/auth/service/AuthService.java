package com.dotori.dotori.auth.service;

import com.dotori.dotori.auth.dto.LoginRequest;
import com.dotori.dotori.auth.dto.LoginResponse;
import com.dotori.dotori.auth.dto.SignupRequest;
import com.dotori.dotori.auth.security.JwtTokenProvider;
import com.dotori.dotori.global.exception.CustomException;
import com.dotori.dotori.global.exception.ErrorCode;
import com.dotori.dotori.user.entity.User;
import com.dotori.dotori.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(SignupRequest request) {
        log.debug("회원가입 처리 시작: email={}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("이미 존재하는 이메일로 회원가입 시도: email={}", request.getEmail());
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .role("MEMBER")
                .build();

        User savedUser = userRepository.save(user);
        log.info("새 사용자 생성 완료: userId={}, email={}, nickname={}", 
                savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());
    }

    public LoginResponse login(LoginRequest request) {
        log.debug("로그인 처리 시작: email={}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 사용자 로그인 시도: email={}", request.getEmail());
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("비밀번호 불일치: email={}", request.getEmail());
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("로그인 성공 및 토큰 생성: userId={}, email={}", user.getId(), user.getEmail());

        return new LoginResponse(token);
    }
}
