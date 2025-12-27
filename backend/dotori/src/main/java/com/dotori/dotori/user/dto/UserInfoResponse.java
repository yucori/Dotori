package com.dotori.dotori.user.dto;

import com.dotori.dotori.user.entity.User;
import lombok.Getter;

@Getter
public class UserInfoResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;

    public UserInfoResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.nickname = user.getNickname();
    }
}
