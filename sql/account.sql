-- 优化后的 account 表
-- 修复与 code_execution_record_optimized 表的兼容性问题

CREATE TABLE `account` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `account_no`   BIGINT NOT NULL COMMENT '用户唯一标识(业务主键)',
    
    -- 基础信息
    `nickname`     VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '昵称',
    `head_img`     VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '头像URL',
    
    -- 联系方式
    `phone`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '手机号',
    
    -- 安全信息
    `pwd`          VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '密码(加密后)',
    
    -- 权限和状态
    `auth`   VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'DEFAULT' COMMENT '认证级别：DEFAULT，REALNAME，ENTERPRISE',
    `status`       TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态：0-禁用，1-启用，2-锁定',
    
    -- 统计信息(用于沙箱访问控制)
    `total_execution_count` BIGINT NOT NULL DEFAULT 0 COMMENT '总代码执行次数',
    `last_execution_time`   BIGINT DEFAULT NULL COMMENT '最后执行时间戳(毫秒)',
    
    -- 配额限制(基于认证级别)
    `execution_timeout`     INT NOT NULL DEFAULT 5000 COMMENT '单次执行超时时间(毫秒)',
    `memory_limit`          BIGINT NOT NULL DEFAULT 268435456 COMMENT '内存限制(字节,默认256MB)',

    -- 软删除标识
    `is_deleted`    BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除(软删除)',
    
    -- 时间戳(统一命名风格)
    `created_at`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_at` TIMESTAMP DEFAULT NULL COMMENT '最后登录时间',
    
    -- 主键
    PRIMARY KEY (`id`),
    
    -- 唯一键约束(支持软删除)
    UNIQUE KEY `uk_account_no` (`account_no`) USING BTREE,
    UNIQUE KEY `uk_phone_active` (`phone`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_auth` (`auth`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_last_execution_time` (`last_execution_time`),
    
    -- 复合索引(用于沙箱访问控制查询)
    INDEX `idx_account_status_auth` (`account_no`, `status`, `auth`),
    INDEX `idx_status_deleted` (`status`, `is_deleted`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账户表(优化版，兼容沙箱执行记录)';

-- 创建认证级别检查约束
ALTER TABLE `account` ADD CONSTRAINT `chk_account_auth` 
CHECK (`auth` IN ('DEFAULT', 'REALNAME', 'ENTERPRISE'));

-- 创建状态检查约束  
ALTER TABLE `account` ADD CONSTRAINT `chk_account_status` 
CHECK (`status` IN (0, 1, 2));

-- 创建执行限制检查约束
ALTER TABLE `account` ADD CONSTRAINT `chk_account_execution_limits` 
CHECK (`execution_timeout` > 0 AND `memory_limit` > 0);
