-- 题目标签关联表
CREATE TABLE problem_tag_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    
    -- 关联信息
    problem_id BIGINT NOT NULL COMMENT '题目ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    
    -- 软删除标识
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除(软删除)',
    
    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引
    INDEX idx_problem_id (problem_id),
    INDEX idx_tag_id (tag_id),
    INDEX idx_is_deleted (is_deleted),
    UNIQUE KEY uk_problem_tag_active (problem_id, tag_id, is_deleted),
    
    -- 外键约束
    CONSTRAINT fk_problem_tag_problem FOREIGN KEY (problem_id) REFERENCES problem (id) ON DELETE CASCADE,
    CONSTRAINT fk_problem_tag_tag FOREIGN KEY (tag_id) REFERENCES problem_tag (id) ON DELETE CASCADE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签关联表';
