package com.dotori.dotori.task.service;

import com.dotori.dotori.task.dto.TaskRequest;
import com.dotori.dotori.task.dto.TaskResponse;
import com.dotori.dotori.task.entity.Task;
import com.dotori.dotori.task.repository.TaskRepository;
import com.dotori.dotori.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    // 1. 자동 계획 생성 (기존)
    public List<TaskResponse> generateAutoSchedule(User user) {
        log.debug("자동 계획 생성 시작: userId={}", user.getId());
        List<Task> tasks = taskRepository.findByUserAndIsCompletedFalse(user);
        log.debug("미완료 작업 조회 완료: userId={}, taskCount={}", user.getId(), tasks.size());
        
        List<TaskResponse> result = tasks.stream()
                .sorted(Comparator
                    // 1순위 기준: 우선순위 유형 (1 -> 2 -> 3 -> 4)
                    .comparing(Task::getPriorityType)
                    // 2순위 기준: 같은 우선순위라면 고정 스케줄을 먼저 표시 (선택 사항)
                    .thenComparing(Task::isFixed, Comparator.reverseOrder())
                    // 3순위 기준: 시작 시간이 빠른 순서대로 (시간이 없는 경우 뒤로 보냄)
                    .thenComparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .map(TaskResponse::new) // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
        
        log.debug("자동 계획 생성 완료: userId={}, resultCount={}", user.getId(), result.size());
        return result;
    }

    // 2. 업무 저장 (추가) - 쓰기 작업이므로 @Transactional (readOnly=false)
    @Transactional
    public TaskResponse saveTask(User user, TaskRequest request) {
        log.debug("작업 저장 시작: userId={}, title={}", user.getId(), request.getTitle());
        
        // List<String>을 "월,화" 형태의 문자열로 변환
        String daysOfWeekStr = (request.getDaysOfWeek() != null)
                ? String.join(",", request.getDaysOfWeek())
                : null;

        Task task = Task.builder()
                .user(user)
                .title(request.getTitle())
                .priorityType(request.getPriorityType())
                .durationMinutes(request.getDurationMinutes())
                .isFixed(request.isFixed())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .daysOfWeek(daysOfWeekStr) // 변환된 문자열 저장
                .postponeCount(0)
                .isCompleted(false)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("작업 저장 완료: taskId={}, userId={}, title={}, priorityType={}, isFixed={}", 
                savedTask.getId(), user.getId(), savedTask.getTitle(), 
                savedTask.getPriorityType(), savedTask.isFixed());
        
        return new TaskResponse(savedTask);
    }

    // 3. 미루기 리스크 계산 (추가)
    public double calculatePostponeRisk(Long taskId) {
        log.debug("미루기 리스크 계산 시작: taskId={}", taskId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("작업을 찾을 수 없음: taskId={}", taskId);
                    return new IllegalArgumentException("업무를 찾을 수 없습니다.");
                });

        // 간단한 업보 계산 알고리즘: 미룬 횟수당 15%씩 상승
        double risk = task.getPostponeCount() * 15.0;
        if (task.isFixed()) risk += 20.0; // 고정 업무는 미루면 위험도 가중

        double finalRisk = Math.min(99.0, risk);
        log.debug("미루기 리스크 계산 완료: taskId={}, postponeCount={}, isFixed={}, risk={}%", 
                taskId, task.getPostponeCount(), task.isFixed(), finalRisk);
        
        return finalRisk;
    }
}