package com.dotori.dotori.task.repository;

import com.dotori.dotori.task.entity.Task;
import com.dotori.dotori.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // 특정 유저의 완료되지 않은 일정만 가져오기
    List<Task> findByUserAndIsCompletedFalse(User user);
}