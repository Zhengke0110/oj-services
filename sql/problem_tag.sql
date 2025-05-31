-- 题目标签表
-- 替代problem表中的tags字段，支持更好的标签管理

CREATE TABLE problem_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    
    -- 标签信息
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_name_en VARCHAR(50) DEFAULT NULL COMMENT '英文标签名称',
    tag_color VARCHAR(7) DEFAULT '#007bff' COMMENT '标签颜色(十六进制)',
    
    -- 分类信息
    category VARCHAR(20) NOT NULL DEFAULT 'ALGORITHM' COMMENT '标签分类：ALGORITHM(算法)，DATA_STRUCTURE(数据结构)，TOPIC(主题)，DIFFICULTY(难度)',
    
    -- 统计信息
    usage_count BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数',
    
    -- 状态
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    
    -- 描述
    description TEXT DEFAULT NULL COMMENT '标签描述',
    
    -- 软删除标识
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除(软删除)',
    
    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    UNIQUE KEY uk_tag_name_active (tag_name, is_deleted),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_usage_count (usage_count),
    INDEX idx_is_deleted (is_deleted),
    
    -- 约束
    CONSTRAINT chk_problemtag_category CHECK (category IN ('ALGORITHM', 'DATA_STRUCTURE', 'TOPIC', 'DIFFICULTY')),
    CONSTRAINT chk_problemtag_status CHECK (status IN (0, 1))
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签表';


-- 预插入一些常用标签
INSERT INTO problem_tag (tag_name, tag_name_en, category, tag_color, description) VALUES
-- 算法类标签
('排序', 'Sorting', 'ALGORITHM', '#28a745', '排序算法相关题目'),
('搜索', 'Search', 'ALGORITHM', '#17a2b8', '搜索算法相关题目'),
('动态规划', 'Dynamic Programming', 'ALGORITHM', '#ffc107', '动态规划算法题目'),
('贪心算法', 'Greedy', 'ALGORITHM', '#fd7e14', '贪心算法题目'),
('递归', 'Recursion', 'ALGORITHM', '#6f42c1', '递归算法题目'),
('分治', 'Divide and Conquer', 'ALGORITHM', '#e83e8c', '分治算法题目'),

-- 数据结构类标签
('数组', 'Array', 'DATA_STRUCTURE', '#007bff', '数组相关题目'),
('链表', 'Linked List', 'DATA_STRUCTURE', '#6c757d', '链表相关题目'),
('栈', 'Stack', 'DATA_STRUCTURE', '#28a745', '栈相关题目'),
('队列', 'Queue', 'DATA_STRUCTURE', '#17a2b8', '队列相关题目'),
('树', 'Tree', 'DATA_STRUCTURE', '#ffc107', '树结构相关题目'),
('图', 'Graph', 'DATA_STRUCTURE', '#fd7e14', '图结构相关题目'),
('哈希表', 'Hash Table', 'DATA_STRUCTURE', '#6f42c1', '哈希表相关题目'),

-- 主题类标签
('字符串', 'String', 'TOPIC', '#20c997', '字符串处理题目'),
('数学', 'Math', 'TOPIC', '#fd7e14', '数学计算题目'),
('模拟', 'Simulation', 'TOPIC', '#6c757d', '模拟类题目'),
('新手友好', 'Beginner Friendly', 'TOPIC', '#28a745', '适合新手的题目');
