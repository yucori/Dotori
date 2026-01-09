package com.dotori.dotori.task.dto;

import com.dotori.dotori.task.entity.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class TaskResponse {
    private Long id;
    private String title;
    private int priorityType;
    private int durationMinutes;
    @JsonProperty("isFixed")
    private boolean isFixed;
    private int postponeCount;

    // Entity와 동일하게 LocalTime으로 타입을 맞춥니다.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
    private LocalTime startTime;

    // 필요한 경우 endTime도 추가할 수 있습니다.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
    private LocalTime endTime;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.priorityType = task.getPriorityType();
        this.durationMinutes = task.getDurationMinutes();
        this.isFixed = task.isFixed();
        this.postponeCount = task.getPostponeCount();

        // Entity의 타입을 그대로 가져오므로 이제 오류가 발생하지 않습니다.
        this.startTime = task.getStartTime();
        this.endTime = task.getEndTime();
    }
}