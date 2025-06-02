-- 优化后的问题表
-- 兼容沙箱服务和支持多种题目类型

CREATE TABLE problem (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    
    -- 基础信息
    title VARCHAR(200) NOT NULL COMMENT '题目标题',
    title_en VARCHAR(200) DEFAULT NULL COMMENT '英文标题',
    description TEXT NOT NULL COMMENT '题目描述',
    description_en TEXT DEFAULT NULL COMMENT '英文描述',
    
    -- 题目分类
    problem_type VARCHAR(20) NOT NULL DEFAULT 'ALGORITHM' COMMENT '题目类型：ALGORITHM(算法)，PRACTICE(编程练习)，DEBUG(代码调试)',
    difficulty TINYINT NOT NULL DEFAULT 0 COMMENT '难度级别：0-简单，1-中等，2-困难',
    
    -- 执行限制
    time_limit INT NOT NULL DEFAULT 1000 COMMENT '默认时间限制(ms)',
    memory_limit BIGINT NOT NULL DEFAULT 268435456 COMMENT '默认内存限制(字节，默认256MB)',
    
    -- 支持的编程语言
    supported_languages JSON NOT NULL COMMENT '支持的编程语言列表',
    
    -- 代码模板(支持多语言)
    solution_templates JSON DEFAULT NULL COMMENT '解题代码模板(多语言)，格式：{"JAVA":"...", "PYTHON":"..."}',
    
    -- 输入输出说明
    input_description TEXT DEFAULT NULL COMMENT '输入格式说明',
    output_description TEXT DEFAULT NULL COMMENT '输出格式说明',
    has_input BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否需要输入数据',
    input_format VARCHAR(50) DEFAULT 'TEXT' COMMENT '输入格式：TEXT(文本)，JSON(JSON格式)，ARGS(命令行参数)',
    
    -- 示例数据
    examples JSON DEFAULT NULL COMMENT '示例输入输出，格式：[{"input":"...", "output":"...", "explanation":"..."}]',
    
    -- 题目状态和权限
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用，2-草稿',
    visibility TINYINT NOT NULL DEFAULT 1 COMMENT '可见性：0-私有，1-公开，2-下线',
    
    -- 统计信息
    submission_count BIGINT NOT NULL DEFAULT 0 COMMENT '提交次数',
    accepted_count BIGINT NOT NULL DEFAULT 0 COMMENT '通过次数',
    
    -- 创建者和权限
    creator_id BIGINT DEFAULT NULL COMMENT '创建者账户号(对应account.account_no)',
    
    -- 扩展信息
    hints JSON DEFAULT NULL COMMENT '提示信息(分步提示)',
    constraints TEXT DEFAULT NULL COMMENT '约束条件说明',
    notes TEXT DEFAULT NULL COMMENT '备注信息',
    metadata JSON DEFAULT NULL COMMENT '元数据(JSON格式)',
    
    -- 软删除标识
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除(软删除)',
    
    -- 时间戳(统一命名风格)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_problem_type (problem_type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status),
    INDEX idx_visibility (visibility),
    INDEX idx_creator_id (creator_id),
    INDEX idx_created_at (created_at),
    INDEX idx_has_input (has_input),
    INDEX idx_is_deleted (is_deleted),
    
    -- 复合索引
    INDEX idx_status_visibility (status, visibility),
    INDEX idx_type_difficulty (problem_type, difficulty),
    INDEX idx_submission_stats (submission_count, accepted_count),
    INDEX idx_status_deleted (status, visibility, is_deleted)
    
    -- 注意：外键约束暂时注释，建议在应用层维护数据一致性
    -- 如果需要启用外键约束，请确保 account 表已创建且使用正确的字段
    -- CONSTRAINT fk_problem_creator FOREIGN KEY (creator_id) REFERENCES account (account_no)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题信息表(优化版)';

-- 添加约束检查
ALTER TABLE problem 
  ADD CONSTRAINT chk_problem_type CHECK (problem_type IN ('ALGORITHM', 'PRACTICE', 'DEBUG')),
  ADD CONSTRAINT chk_problem_difficulty CHECK (difficulty IN (0, 1, 2)),
  ADD CONSTRAINT chk_problem_status CHECK (status IN (0, 1, 2)),
  ADD CONSTRAINT chk_problem_visibility CHECK (visibility IN (0, 1, 2)),
  ADD CONSTRAINT chk_problem_input_format CHECK (input_format IN ('TEXT', 'JSON', 'ARGS')),
  ADD CONSTRAINT chk_problem_limits CHECK (time_limit > 0 AND memory_limit > 0),
  ADD CONSTRAINT chk_problem_stats CHECK (submission_count >= 0 AND accepted_count >= 0 AND accepted_count <= submission_count);