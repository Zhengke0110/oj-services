-- problem_tag_relation表的模拟插入数据
-- 将problem_mock_data中的题目与problem_tag中的标签关联

-- 插入题目标签关联数据
INSERT INTO problem_tag_relation (problem_id, tag_id) VALUES 

-- ========== 题目1：两数之和 (problem_id = 1) ==========
-- 标签：数组(7)、哈希表(13)、新手友好(17)
(1, 7),   -- 数组
(1, 13),  -- 哈希表
(1, 17),  -- 新手友好

-- ========== 题目2：无重复字符的最长子串 (problem_id = 2) ==========
-- 标签：字符串(14)、哈希表(13)
(2, 14),  -- 字符串
(2, 13),  -- 哈希表

-- ========== 题目3：合并K个升序链表 (problem_id = 3) ==========
-- 标签：链表(8)、分治(6)、排序(1)
(3, 8),   -- 链表
(3, 6),   -- 分治
(3, 1),   -- 排序

-- ========== 题目4：实现一个栈 (problem_id = 4) ==========
-- 标签：栈(9)、新手友好(17)、模拟(16)
(4, 9),   -- 栈
(4, 17),  -- 新手友好
(4, 16),  -- 模拟

-- ========== 题目5：Debug：修复数组越界 (problem_id = 5) ==========
-- 标签：数组(7)、新手友好(17)、模拟(16)
(5, 7),   -- 数组
(5, 17),  -- 新手友好
(5, 16),  -- 模拟

-- ========== 题目6：回文字符串判断 (problem_id = 6) ==========
-- 标签：字符串(14)、新手友好(17)
(6, 14),  -- 字符串
(6, 17),  -- 新手友好

-- ========== 题目7：斐波那契数列 (problem_id = 7) ==========
-- 标签：动态规划(3)、递归(5)、数学(15)、新手友好(17)
(7, 3),   -- 动态规划
(7, 5),   -- 递归
(7, 15),  -- 数学
(7, 17);  -- 新手友好

-- 更新标签使用统计
-- 统计每个标签被使用的次数
UPDATE problem_tag SET usage_count = (
    SELECT COUNT(*) 
    FROM problem_tag_relation 
    WHERE problem_tag_relation.tag_id = problem_tag.id 
    AND problem_tag_relation.is_deleted = FALSE
) WHERE id IN (1, 3, 5, 6, 7, 8, 9, 13, 14, 15, 16, 17);

-- 验证数据的SQL查询（可选执行）
-- 查看每个题目对应的标签
/*
SELECT 
    p.id as problem_id,
    p.title,
    GROUP_CONCAT(pt.tag_name SEPARATOR ', ') as tags
FROM problem p
LEFT JOIN problem_tag_relation ptr ON p.id = ptr.problem_id AND ptr.is_deleted = FALSE
LEFT JOIN problem_tag pt ON ptr.tag_id = pt.id AND pt.is_deleted = FALSE
WHERE p.is_deleted = FALSE
GROUP BY p.id, p.title
ORDER BY p.id;
*/

-- 查看每个标签对应的题目数量
/*
SELECT 
    pt.tag_name,
    pt.category,
    COUNT(ptr.problem_id) as problem_count,
    pt.usage_count
FROM problem_tag pt
LEFT JOIN problem_tag_relation ptr ON pt.id = ptr.tag_id AND ptr.is_deleted = FALSE
WHERE pt.is_deleted = FALSE
GROUP BY pt.id, pt.tag_name, pt.category, pt.usage_count
ORDER BY problem_count DESC, pt.category, pt.tag_name;
*/
