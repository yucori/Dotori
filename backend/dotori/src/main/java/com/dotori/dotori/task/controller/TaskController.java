package com.dotori.dotori.task.controller;

import com.dotori.dotori.task.dto.TaskRequest; // DTO 임포트
import com.dotori.dotori.task.dto.TaskResponse;
import com.dotori.dotori.task.entity.Task;
import com.dotori.dotori.task.service.TaskService;
import com.dotori.dotori.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // 1. 계획 조회
    @GetMapping("/auto-plan")
    public ResponseEntity<List<TaskResponse>> getWeeklyPlan(@AuthenticationPrincipal User user) {
        log.info("자동 계획 조회 요청: userId={}, email={}", user.getId(), user.getEmail());
        try {
            List<TaskResponse> tasks = taskService.generateAutoSchedule(user);
            log.info("자동 계획 조회 성공: userId={}, taskCount={}", user.getId(), tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("자동 계획 조회 실패: userId={}, error={}", user.getId(), e.getMessage());
            throw e;
        }
    }

    // 2. 업무 생성 - taskRepository 대신 taskService.saveTask 사용
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@AuthenticationPrincipal User user, @RequestBody TaskRequest request) {
        log.info("작업 생성 요청: userId={}, email={}, title={}, priorityType={}", 
                user.getId(), user.getEmail(), request.getTitle(), request.getPriorityType());
        try {
            TaskResponse response = taskService.saveTask(user, request);
            log.info("작업 생성 성공: userId={}, taskId={}, title={}", 
                    user.getId(), response.getId(), response.getTitle());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("작업 생성 실패: userId={}, title={}, error={}", 
                    user.getId(), request.getTitle(), e.getMessage());
            throw e;
        }
    }

    // 3. 미루기 리스크 조회
    @PostMapping("/{taskId}/postpone-risk")
    public ResponseEntity<?> getPostponeRisk(@PathVariable Long taskId) {
        log.info("미루기 리스크 조회 요청: taskId={}", taskId);
        try {
            double risk = taskService.calculatePostponeRisk(taskId);
            log.info("미루기 리스크 계산 완료: taskId={}, risk={}%", taskId, risk);
            return ResponseEntity.ok(Map.of(
                    "riskProbability", risk,
                    "message", risk > 70 ? "이걸 미루면 도토리가 다 썩을지도 몰라요!" : "아직은 괜찮아요."
            ));
        } catch (Exception e) {
            log.error("미루기 리스크 조회 실패: taskId={}, error={}", taskId, e.getMessage());
            throw e;
        }
    }
}