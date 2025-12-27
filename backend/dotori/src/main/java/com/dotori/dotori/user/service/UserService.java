package com.dotori.dotori.user.service;

import com.dotori.dotori.user.dto.UpdateUserRequest;
import com.dotori.dotori.user.dto.UserInfoResponse;
import com.dotori.dotori.user.entity.User;
import com.dotori.dotori.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserInfoResponse getMyInfo(User user) {
        return new UserInfoResponse(user);
    }

    public UserInfoResponse updateUser(User user, UpdateUserRequest request) {
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

        return new UserInfoResponse(userRepository.save(user));
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
