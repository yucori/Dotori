package com.dotori.dotori.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskRequest {
    private String title;
    private int priorityType;
    private int durationMinutes;
    @JsonProperty("isFixed")
    private boolean isFixed;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm")
    private java.time.LocalTime startTime;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm")
    private java.time.LocalTime endTime;

    private List<String> daysOfWeek;
}