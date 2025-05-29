-- 优化后的代码执行记录表
CREATE TABLE code_execution_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    -- 请求标识信息
    request_id VARCHAR(64) COMMENT '请求唯一标识',
    
    -- 基础信息
    language VARCHAR(20) NOT NULL COMMENT '编程语言(JAVA/JAVASCRIPT/PYTHON)',
    code_content TEXT NOT NULL COMMENT '执行的代码内容',
    code_hash VARCHAR(64) COMMENT '代码内容SHA256哈希值',
    
    -- 执行环境
    docker_image VARCHAR(100) NOT NULL COMMENT '使用的Docker镜像',
    container_id VARCHAR(64) COMMENT '使用的容器ID',
    execution_host VARCHAR(100) COMMENT '执行主机',
    
    -- 输入参数信息
    execution_mode VARCHAR(20) NOT NULL COMMENT '执行模式(NO_INPUT/WITH_ARGS)',
    command_args JSON COMMENT '命令行参数(JSON数组格式)',
    execution_count INT NOT NULL DEFAULT 1 COMMENT '请求执行次数',
    actual_execution_count INT NOT NULL DEFAULT 1 COMMENT '实际执行次数',
    
    -- 执行结果
    success BOOLEAN NOT NULL COMMENT '是否成功执行',
    execution_status VARCHAR(30) NOT NULL COMMENT '执行状态(SUCCEED/FAILED/COMPILATION_ERROR/RUNTIME_ERROR/TIMEOUT_ERROR)',
    actual_output JSON COMMENT '实际输出(JSON数组格式，支持多次执行)',
    error_message TEXT COMMENT '错误信息',
    
    -- 性能指标
    execution_time BIGINT NOT NULL COMMENT '执行时间(毫秒)',
    memory_used BIGINT NOT NULL COMMENT '内存使用(字节)',
    max_execution_time BIGINT NOT NULL COMMENT '最大执行时间(毫秒)',
    max_memory_used BIGINT NOT NULL COMMENT '最大内存使用(字节)',
    
    -- 业务关联字段
    account_no BIGINT COMMENT '用户唯一标识',
    problem_id BIGINT COMMENT '问题ID',
    solution_id BIGINT COMMENT '解决方案ID',
    
    -- 判题相关字段(可选，用于判题系统)
    expected_output TEXT COMMENT '预期输出(判题时使用)',
    output_matched BOOLEAN COMMENT '输出是否匹配预期(判题结果)',
    
    -- 客户端信息
    client_ip VARCHAR(45) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    
    -- 时间戳
    submission_time BIGINT COMMENT '提交时间戳(毫秒)',
    execution_start_time BIGINT COMMENT '开始执行时间戳(毫秒)',
    execution_end_time BIGINT COMMENT '结束执行时间戳(毫秒)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 扩展字段
    additional_info JSON COMMENT '附加信息(JSON格式)',
    
    -- 软删除标识
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除(软删除)',
    
    -- 索引优化
    INDEX idx_language (language),
    INDEX idx_account_no (account_no),
    INDEX idx_problem_id (problem_id),
    INDEX idx_execution_status (execution_status),
    INDEX idx_success (success),
    INDEX idx_submission_time (submission_time),
    INDEX idx_code_hash (code_hash),
    INDEX idx_request_id (request_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    
    -- 复合索引
    INDEX idx_account_problem (account_no, problem_id),
    INDEX idx_language_status (language, execution_status),
    INDEX idx_time_range (submission_time, execution_status),
    INDEX idx_status_deleted (execution_status, is_deleted),
    
    -- 外键约束
    CONSTRAINT fk_execution_account FOREIGN KEY (account_no) REFERENCES account (account_no),
    CONSTRAINT fk_execution_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码执行记录表(优化版)';
