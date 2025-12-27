package com.dotori.dotori.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND(404, "User not found"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),
    INVALID_PASSWORD(400, "Password does not match");

    private final int status;
    private final String message;
}
