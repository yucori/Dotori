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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .role("MEMBER")
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtTokenProvider.generateToken(user);

        return new LoginResponse(token);
    }
}
