CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,

    -- 관계 및 필수 정보
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    priority_type INTEGER NOT NULL CHECK (priority_type BETWEEN 1 AND 4), -- 1~4 범위 제한
    manual_priority INTEGER DEFAULT 0,
    duration_minutes INTEGER DEFAULT 60,
    is_fixed BOOLEAN DEFAULT FALSE,
    postpone_count INTEGER DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,

    -- 시간 및 반복 정보 (고정 일정용)
    start_time TIME,
    end_time TIME,
    days_of_week VARCHAR(50), -- 예: "MON,WED,FRI" 또는 "1,3,5"
    recurrence_rule VARCHAR(100),

    -- 기록 정보
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 외래키 제약 조건
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 인덱스 추가 (조회 성능 최적화)
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_is_completed ON tasks(is_completed);

-- 컬럼 코멘트
COMMENT ON COLUMN tasks.priority_type IS '1:중요/높은집중, 2:중요/낮은집중, 3:낮음/높은집중, 4:낮음/낮은집중';
