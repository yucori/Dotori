package com.dotori.dotori.user.controller;

import com.dotori.dotori.user.dto.UpdateUserRequest;
import com.dotori.dotori.user.dto.UserInfoResponse;
import com.dotori.dotori.user.entity.User;
import com.dotori.dotori.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal User user) {
        return userService.getMyInfo(user);
    }

    @PatchMapping("/me")
    public UserInfoResponse updateMyInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(user, request);
    }
}
