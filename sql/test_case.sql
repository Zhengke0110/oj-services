-- 优化后的测试用例表
-- 支持无输入题目和复杂测试场景

CREATE TABLE test_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '测试用例ID',
    
    -- 关联信息
    problem_id BIGINT NOT NULL COMMENT '关联的题目ID',
    
    -- 测试用例基础信息
    case_name VARCHAR(100) DEFAULT NULL COMMENT '测试用例名称',
    case_type VARCHAR(20) NOT NULL DEFAULT 'FUNCTIONAL' COMMENT '测试用例类型：FUNCTIONAL(功能)，BOUNDARY(边界)，PERFORMANCE(性能)，EXAMPLE(示例)',
    
    -- 输入输出数据
    input_data TEXT DEFAULT NULL COMMENT '输入数据(支持多行，JSON等格式)',
    expected_output TEXT NOT NULL COMMENT '期望输出结果',
    
    -- 输入数据格式化
    input_format VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '输入格式：TEXT(文本)，JSON(JSON)，ARGS(命令行参数)，NONE(无输入)',
    input_args JSON DEFAULT NULL COMMENT '当input_format为ARGS时，存储参数数组',
    
    -- 测试用例属性
    is_example BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为示例测试用例',
    is_public BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为公开测试用例(用户可见)',
    weight INT NOT NULL DEFAULT 1 COMMENT '测试用例权重',
    order_index INT NOT NULL DEFAULT 0 COMMENT '执行顺序',
    
    -- 执行配置(可覆盖题目默认配置)
    time_limit_override INT DEFAULT NULL COMMENT '时间限制覆盖(ms)，NULL表示使用题目默认值',
    memory_limit_override BIGINT DEFAULT NULL COMMENT '内存限制覆盖(字节)，NULL表示使用题目默认值',
    
    -- 状态和统计
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    execution_count BIGINT NOT NULL DEFAULT 0 COMMENT '执行次数统计',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '成功次数统计',
    
    -- 附加信息
    description TEXT DEFAULT NULL COMMENT '测试用例说明',
    expected_behavior TEXT DEFAULT NULL COMMENT '预期行为描述',
    notes TEXT DEFAULT NULL COMMENT '备注信息',
    
    -- 软删除标识
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除(软删除)',
    
    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_problem_id (problem_id),
    INDEX idx_case_type (case_type),
    INDEX idx_input_format (input_format),
    INDEX idx_is_example (is_example),
    INDEX idx_is_public (is_public),
    INDEX idx_status (status),
    INDEX idx_order_index (order_index),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    
    -- 复合索引
    INDEX idx_problem_status (problem_id, status),
    INDEX idx_problem_example (problem_id, is_example),
    INDEX idx_problem_order (problem_id, order_index),
    INDEX idx_type_status (case_type, status),
    INDEX idx_status_deleted (status, is_deleted),
    
    -- 外键约束
    CONSTRAINT fk_test_case_problem FOREIGN KEY (problem_id) REFERENCES problem (id) ON DELETE CASCADE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表(优化版)';

-- 添加约束检查
ALTER TABLE test_case 
  ADD CONSTRAINT chk_testcase_case_type CHECK (case_type IN ('FUNCTIONAL', 'BOUNDARY', 'PERFORMANCE', 'EXAMPLE')),
  ADD CONSTRAINT chk_testcase_input_format CHECK (input_format IN ('TEXT', 'JSON', 'ARGS', 'NONE')),
  ADD CONSTRAINT chk_testcase_status CHECK (status IN (0, 1)),
  ADD CONSTRAINT chk_testcase_weight CHECK (weight > 0),
  ADD CONSTRAINT chk_testcase_order_index CHECK (order_index >= 0),
  ADD CONSTRAINT chk_testcase_execution_stats CHECK (execution_count >= 0 AND success_count >= 0 AND success_count <= execution_count),
  ADD CONSTRAINT chk_testcase_time_limit_override CHECK (time_limit_override IS NULL OR time_limit_override > 0),
  ADD CONSTRAINT chk_testcase_memory_limit_override CHECK (memory_limit_override IS NULL OR memory_limit_override > 0);

-- 创建唯一约束，确保同一问题下的测试用例顺序唯一
ALTER TABLE test_case 
  ADD CONSTRAINT uk_testcase_problem_order UNIQUE (problem_id, order_index);
