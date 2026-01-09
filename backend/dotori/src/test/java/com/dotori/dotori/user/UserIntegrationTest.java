package com.dotori.dotori.user;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String getAccessToken() throws Exception {
        // 회원가입
        Map<String, String> signupRequest = Map.of(
                "name", "test_name",
                "email", "test@example.com",
                "password", "1234",
                "nickname", "test_nickname"
        );

        mockMvc.perform(
                post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
        );

        // 로그인
        Map<String, String> loginRequest = Map.of(
                "email", "test@example.com",
                "password", "1234"
        );

        String tokenResponse = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(tokenResponse).get("accessToken").asText();
    }

    @Test
    @DisplayName("사용자 정보 조회 → 200 OK")
    void getMyInfo_success() throws Exception {
        String accessToken = getAccessToken();

        mockMvc.perform(
                        get("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("test_name"))
                .andExpect(jsonPath("$.nickname").value("test_nickname"));
    }

    @Test
    @DisplayName("사용자 정보 수정 → 200 OK")
    void updateMyInfo_success() throws Exception {
        String accessToken = getAccessToken();

        Map<String, String> updateRequest = Map.of(
                "name", "updated_name",
                "nickname", "updated_nickname"
        );

        mockMvc.perform(
                        patch("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("updated_name"))
                .andExpect(jsonPath("$.nickname").value("updated_nickname"));
    }

    @Test
    @DisplayName("JWT 없이 사용자 정보 수정 시도 → 403 Forbidden")
    void updateMyInfo_without_token_fail() throws Exception {
        Map<String, String> updateRequest = Map.of(
                "name", "updated_name",
                "nickname", "updated_nickname"
        );

        mockMvc.perform(
                        patch("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isForbidden());
    }
}

