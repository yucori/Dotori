package com.dotori.dotori.task.controller;

import com.dotori.dotori.task.dto.TaskRequest; // DTO 임포트
import com.dotori.dotori.task.dto.TaskResponse;
import com.dotori.dotori.task.entity.Task;
import com.dotori.dotori.task.service.TaskService;
import com.dotori.dotori.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // 1. 계획 조회
    @GetMapping("/auto-plan")
    public ResponseEntity<List<TaskResponse>> getWeeklyPlan(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.generateAutoSchedule(user));
    }

    // 2. 업무 생성 - taskRepository 대신 taskService.saveTask 사용
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@AuthenticationPrincipal User user, @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.saveTask(user, request));
    }

    // 3. 미루기 리스크 조회
    @PostMapping("/{taskId}/postpone-risk")
    public ResponseEntity<?> getPostponeRisk(@PathVariable Long taskId) {
        double risk = taskService.calculatePostponeRisk(taskId);
        return ResponseEntity.ok(Map.of(
                "riskProbability", risk,
                "message", risk > 70 ? "이걸 미루면 도토리가 다 썩을지도 몰라요!" : "아직은 괜찮아요."
        ));
    }
}