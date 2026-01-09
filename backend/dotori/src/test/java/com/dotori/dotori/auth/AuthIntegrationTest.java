package com.dotori.dotori.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 🔥 매 테스트마다 DB 초기화
class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 → 201 Created")
    void signup_success() throws Exception {
        Map<String, String> request = Map.of(
                "name", "test_name",
                "email", "test@example.com",
                "password", "1234",
                "nickname", "test_nickname"
        );

        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("signup success"));
    }

    @Test
    @DisplayName("로그인 → JWT 토큰 발급 성공")
    void login_success() throws Exception {
        signup_success();

        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "password", "1234"
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    @DisplayName("JWT 인증 필요한 API 호출 성공")
    void me_with_token_success() throws Exception {
        signup_success();

        Map<String, String> loginReq = Map.of(
                "email", "test@example.com",
                "password", "1234"
        );

        String token = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(token).get("accessToken").asText();

        mockMvc.perform(
                        get("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("JWT 없이 /users/me → 403 Forbidden (스프링 기본 동작)")
    void me_without_token_fail() throws Exception {
        mockMvc.perform(
                        get("/users/me")
                )
                .andExpect(status().isForbidden()); // 🔥 403으로 변경
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시도 → 400 Bad Request")
    void signup_duplicate_email_fail() throws Exception {
        // 첫 번째 회원가입
        signup_success();

        // 동일한 이메일로 다시 회원가입 시도
        Map<String, String> request = Map.of(
                "name", "test_name2",
                "email", "test@example.com",
                "password", "1234",
                "nickname", "test_nickname2"
        );

        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Email already exists"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 → 404 Not Found")
    void login_user_not_found_fail() throws Exception {
        Map<String, String> request = Map.of(
                "email", "notfound@example.com",
                "password", "1234"
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 → 400 Bad Request")
    void login_invalid_password_fail() throws Exception {
        signup_success();

        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "password", "wrong_password"
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Password does not match"));
    }
}
