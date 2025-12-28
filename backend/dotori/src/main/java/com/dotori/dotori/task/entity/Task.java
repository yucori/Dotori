package com.dotori.dotori.task.entity;

import com.dotori.dotori.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 생성 방지
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "priority_type")
    private int priorityType; // 1~4순위 유형

    @Column(name = "manual_priority")
    private int manualPriority;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "is_fixed")
    private boolean isFixed;

    @Column(name = "postpone_count")
    private int postponeCount;

    @Column(name = "is_completed")
    private boolean isCompleted;

    @Column(name = "start_time")
    private java.time.LocalTime startTime;

    @Column(name = "end_time")
    private java.time.LocalTime endTime;

    @Column(name = "days_of_week")
    private String daysOfWeek;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 로직에 따른 상태 변경 메서드 (Setter 대신 사용)
    public void completeTask() {
        this.isCompleted = true;
    }

    public void incrementPostponeCount() {
        this.postponeCount++;
    }
}