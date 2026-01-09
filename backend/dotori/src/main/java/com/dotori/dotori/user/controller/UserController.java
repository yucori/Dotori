package com.dotori.dotori.user.controller;

import com.dotori.dotori.user.dto.UpdateUserRequest;
import com.dotori.dotori.user.dto.UserInfoResponse;
import com.dotori.dotori.user.entity.User;
import com.dotori.dotori.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal User user) {
        log.info("사용자 정보 조회 요청: userId={}, email={}", user.getId(), user.getEmail());
        try {
            UserInfoResponse response = userService.getMyInfo(user);
            log.debug("사용자 정보 조회 성공: userId={}", user.getId());
            return response;
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: userId={}, error={}", user.getId(), e.getMessage());
            throw e;
        }
    }

    @PatchMapping("/me")
    public UserInfoResponse updateMyInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserRequest request
    ) {
        log.info("사용자 정보 수정 요청: userId={}, email={}, name={}, nickname={}", 
                user.getId(), user.getEmail(), request.getName(), request.getNickname());
        try {
            UserInfoResponse response = userService.updateUser(user, request);
            log.info("사용자 정보 수정 성공: userId={}, nickname={}", user.getId(), response.getNickname());
            return response;
        } catch (Exception e) {
            log.error("사용자 정보 수정 실패: userId={}, error={}", user.getId(), e.getMessage());
            throw e;
        }
    }
}
