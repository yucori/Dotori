package com.dotori.dotori.task;

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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskIntegrationTest {

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
    @DisplayName("자동 계획 조회 → 200 OK (빈 리스트)")
    void getWeeklyPlan_empty_success() throws Exception {
        String accessToken = getAccessToken();

        mockMvc.perform(
                        get("/api/tasks/auto-plan")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("작업 생성 → 200 OK")
    void createTask_success() throws Exception {
        String accessToken = getAccessToken();

        Map<String, Object> taskRequest = Map.of(
                "title", "테스트 작업",
                "priorityType", 1,
                "durationMinutes", 60,
                "isFixed", false
        );

        mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(taskRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("테스트 작업"))
                .andExpect(jsonPath("$.priorityType").value(1))
                .andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.isFixed").value(false))
                .andExpect(jsonPath("$.postponeCount").value(0));
    }

    @Test
    @DisplayName("작업 생성 후 자동 계획 조회 → 생성된 작업 포함")
    void createTask_and_getWeeklyPlan_success() throws Exception {
        String accessToken = getAccessToken();

        // 작업 생성
        Map<String, Object> taskRequest = Map.of(
                "title", "우선순위 높은 작업",
                "priorityType", 1,
                "durationMinutes", 90,
                "isFixed", true
        );

        String createResponse = mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(taskRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // 자동 계획 조회
        mockMvc.perform(
                        get("/api/tasks/auto-plan")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].title").value("우선순위 높은 작업"))
                .andExpect(jsonPath("$[0].priorityType").value(1));
    }

    @Test
    @DisplayName("여러 작업 생성 후 우선순위 정렬 확인")
    void createMultipleTasks_and_checkPriorityOrder() throws Exception {
        String accessToken = getAccessToken();

        // 우선순위가 다른 여러 작업 생성
        Map<String, Object> task1 = Map.of(
                "title", "우선순위 3 작업",
                "priorityType", 3,
                "durationMinutes", 60,
                "isFixed", false
        );

        Map<String, Object> task2 = Map.of(
                "title", "우선순위 1 작업",
                "priorityType", 1,
                "durationMinutes", 60,
                "isFixed", false
        );

        Map<String, Object> task3 = Map.of(
                "title", "우선순위 2 작업",
                "priorityType", 2,
                "durationMinutes", 60,
                "isFixed", false
        );

        mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task1))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task2))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task3))
                )
                .andExpect(status().isOk());

        // 자동 계획 조회 시 우선순위 순서대로 정렬되어야 함
        mockMvc.perform(
                        get("/api/tasks/auto-plan")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].priorityType").value(1))
                .andExpect(jsonPath("$[1].priorityType").value(2))
                .andExpect(jsonPath("$[2].priorityType").value(3));
    }

    @Test
    @DisplayName("미루기 리스크 조회 → 200 OK")
    void getPostponeRisk_success() throws Exception {
        String accessToken = getAccessToken();

        // 작업 생성
        Map<String, Object> taskRequest = Map.of(
                "title", "테스트 작업",
                "priorityType", 1,
                "durationMinutes", 60,
                "isFixed", false
        );

        String createResponse = mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(taskRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // 미루기 리스크 조회
        mockMvc.perform(
                        post("/api/tasks/" + taskId + "/postpone-risk")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskProbability", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("존재하지 않는 작업의 미루기 리스크 조회 → 500 Internal Server Error")
    void getPostponeRisk_taskNotFound_fail() throws Exception {
        String accessToken = getAccessToken();

        mockMvc.perform(
                        post("/api/tasks/99999/postpone-risk")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("JWT 없이 작업 생성 시도 → 403 Forbidden")
    void createTask_without_token_fail() throws Exception {
        Map<String, Object> taskRequest = Map.of(
                "title", "테스트 작업",
                "priorityType", 1,
                "durationMinutes", 60,
                "isFixed", false
        );

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(taskRequest))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("JWT 없이 자동 계획 조회 시도 → 403 Forbidden")
    void getWeeklyPlan_without_token_fail() throws Exception {
        mockMvc.perform(
                        get("/api/tasks/auto-plan")
                )
                .andExpect(status().isForbidden());
    }
}

