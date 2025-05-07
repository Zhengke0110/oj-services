CREATE TABLE test_case
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '测试用例ID',
    problem_id      BIGINT   NOT NULL COMMENT '关联的题目ID',
    input           TEXT COMMENT '输入数据',
    expected_output TEXT     NOT NULL COMMENT '期望输出结果',
    is_example      TINYINT  NOT NULL DEFAULT 0 COMMENT '是否为示例测试用例：0-否，1-是',
    weight          INT      NOT NULL DEFAULT 1 COMMENT '测试用例权重',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX           idx_problem_id (problem_id),
    CONSTRAINT fk_test_case_problem_id FOREIGN KEY (problem_id) REFERENCES problem (id) ON DELETE CASCADE
) COMMENT='题目测试用例表';