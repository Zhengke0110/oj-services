CREATE TABLE code_execution_record
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    -- 基础信息
    language           VARCHAR(20)  NOT NULL COMMENT '编程语言(java/python/javascript)',
    code_content       TEXT         NOT NULL COMMENT '执行的代码内容',
    expected_output    TEXT COMMENT '预期输出',
    submission_time    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',

    -- 执行环境
    docker_image       VARCHAR(100) NOT NULL COMMENT '使用的Docker镜像',
    execution_count    INT          NOT NULL DEFAULT 1 COMMENT '执行次数',
    execution_mode     VARCHAR(20)  NOT NULL COMMENT '执行模式(normal/args/testfile)',

    -- 参数或测试文件信息（可选）
    command_args       TEXT COMMENT '命令行参数(JSON格式)',
    test_case_content  TEXT COMMENT '测试用例内容',

    -- 执行结果摘要
    success            TINYINT(1) NOT NULL COMMENT '是否成功执行(0失败/1成功)',
    output_matched     TINYINT(1) NOT NULL COMMENT '输出是否匹配预期(0不匹配/1匹配)',
    execution_status   VARCHAR(30)  NOT NULL COMMENT '执行状态(COMPLETED/COMPILATION_ERROR/RUNTIME_ERROR等)',
    actual_output      TEXT COMMENT '实际输出',
    error_message      TEXT COMMENT '错误信息',

    -- 性能指标
    avg_execution_time BIGINT       NOT NULL COMMENT '平均执行时间(毫秒)',
    avg_memory_used    BIGINT       NOT NULL COMMENT '平均内存使用(字节)',
    max_execution_time BIGINT       NOT NULL COMMENT '最大执行时间(毫秒)',
    max_memory_used    BIGINT       NOT NULL COMMENT '最大内存使用(字节)',

    -- 扩展字段
    account_no         BIGINT COMMENT '用户唯一标识',
    problem_id         BIGINT COMMENT '问题ID',
    solution_id        BIGINT COMMENT '解决方案ID',
    additional_info    JSON COMMENT '附加信息(JSON格式)',

    -- 时间戳
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX              idx_language (language),
    INDEX              idx_account_no (account_no),
    INDEX              idx_problem_id (problem_id),
    INDEX              idx_submission_time (submission_time),
    INDEX              idx_status (execution_status),

    -- 外键约束
    CONSTRAINT fk_execution_account FOREIGN KEY (account_no) REFERENCES account (account_no),
    CONSTRAINT fk_execution_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码执行记录表';