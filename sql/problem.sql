CREATE TABLE problem
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    title             VARCHAR(128) NOT NULL COMMENT '题目标题',
    description       TEXT         NOT NULL COMMENT '题目描述',
    difficulty        TINYINT      NOT NULL DEFAULT 0 COMMENT '难度级别：0-简单，1-中等，2-困难',
    time_limit        INT          NOT NULL DEFAULT 1000 COMMENT '时间限制(ms)',
    memory_limit      INT          NOT NULL DEFAULT 256000 COMMENT '内存限制(KB)',
    tags              VARCHAR(255) COMMENT '标签，多个标签用逗号分隔',
    solution_template TEXT COMMENT '解题代码模板',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用'
) COMMENT='题目信息表';