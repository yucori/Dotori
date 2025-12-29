package com.dotori.dotori.user.service;

import com.dotori.dotori.user.dto.UpdateUserRequest;
import com.dotori.dotori.user.dto.UserInfoResponse;
import com.dotori.dotori.user.entity.User;
import com.dotori.dotori.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserInfoResponse getMyInfo(User user) {
        log.debug("사용자 정보 조회: userId={}, email={}", user.getId(), user.getEmail());
        return new UserInfoResponse(user);
    }

    @Transactional
    public UserInfoResponse updateUser(User user, UpdateUserRequest request) {
        log.debug("사용자 정보 수정 시작: userId={}, name={}, nickname={}", 
                user.getId(), request.getName(), request.getNickname());
        
        user = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(request.getName())
                .nickname(request.getNickname())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료: userId={}, nickname={}", savedUser.getId(), savedUser.getNickname());
        
        return new UserInfoResponse(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        log.debug("사용자 조회 (UserDetailsService): email={}", email);
        UserDetails userDetails = userRepository.findByEmail(email).orElse(null);
        if (userDetails == null) {
            log.warn("사용자를 찾을 수 없음 (UserDetailsService): email={}", email);
        }
        return userDetails;
    }
}
