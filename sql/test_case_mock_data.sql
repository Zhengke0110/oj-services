-- test_case表的模拟插入数据
-- 为problem_mock_data中的题目创建对应的测试用例

-- 插入测试用例数据，与problem表中的题目一一对应
INSERT INTO test_case (
    problem_id, case_name, case_type, input_data, expected_output, 
    input_format, is_example, is_public, weight, order_index,
    description, expected_behavior, status
) VALUES 

-- ========== 题目1：两数之和 (problem_id = 1) ==========
-- 示例测试用例1
(1, '示例用例1', 'EXAMPLE', '4 9\n2 7 11 15', '0 1', 'TEXT', TRUE, TRUE, 1, 1,
 '示例测试用例，验证基本功能', '正确找到nums[0] + nums[1] == 9的情况', 1),

-- 示例测试用例2
(1, '示例用例2', 'EXAMPLE', '3 6\n3 2 4', '1 2', 'TEXT', TRUE, TRUE, 1, 2,
 '示例测试用例，验证基本功能', '正确找到nums[1] + nums[2] == 6的情况', 1),

-- 功能测试用例
(1, '基础功能测试', 'FUNCTIONAL', '5 10\n1 2 3 4 5', '3 4', 'TEXT', FALSE, FALSE, 2, 3,
 '测试基本的两数之和功能', '验证算法的正确性', 1),

(1, '负数测试', 'FUNCTIONAL', '4 0\n-3 4 3 90', '0 2', 'TEXT', FALSE, FALSE, 2, 4,
 '包含负数的测试用例', '验证负数情况下的正确性', 1),

-- 边界测试用例
(1, '最小数组', 'BOUNDARY', '2 8\n4 4', '0 1', 'TEXT', FALSE, FALSE, 2, 5,
 '最小数组长度测试', '验证数组长度为2的边界情况', 1),

(1, '大数值测试', 'BOUNDARY', '4 2000000000\n1000000000 1000000000 500000000 1500000000', '0 1', 'TEXT', FALSE, FALSE, 2, 6,
 '大数值边界测试', '验证大整数的处理', 1),

-- ========== 题目2：无重复字符的最长子串 (problem_id = 2) ==========
-- 示例测试用例
(2, '示例用例1', 'EXAMPLE', 'abcabcbb', '3', 'TEXT', TRUE, TRUE, 1, 1,
 '包含重复字符的字符串', '最长无重复子串为abc，长度为3', 1),

(2, '示例用例2', 'EXAMPLE', 'bbbbb', '1', 'TEXT', TRUE, TRUE, 1, 2,
 '全部相同字符的字符串', '最长无重复子串为b，长度为1', 1),

-- 功能测试用例
(2, '无重复字符串', 'FUNCTIONAL', 'abcdef', '6', 'TEXT', FALSE, FALSE, 2, 3,
 '完全无重复的字符串', '整个字符串长度即为答案', 1),

(2, '单字符', 'FUNCTIONAL', 'a', '1', 'TEXT', FALSE, FALSE, 2, 4,
 '单个字符测试', '单字符的最长子串为1', 1),

(2, '空字符串', 'BOUNDARY', '', '0', 'TEXT', FALSE, FALSE, 2, 5,
 '空字符串边界测试', '空字符串的最长子串为0', 1),

(2, '特殊字符测试', 'FUNCTIONAL', 'pwwkew', '3', 'TEXT', FALSE, FALSE, 2, 6,
 '包含特殊字符模式', '最长无重复子串为wke，长度为3', 1),

-- ========== 题目3：合并K个升序链表 (problem_id = 3) ==========
-- 示例测试用例
(3, '示例用例1', 'EXAMPLE', '3\n3 1 4 5\n3 1 3 4\n2 2 6', '1 1 2 3 4 4 5 6', 'TEXT', TRUE, TRUE, 1, 1,
 '三个升序链表合并', '正确合并为一个升序链表', 1),

(3, '空输入示例', 'EXAMPLE', '0', '', 'TEXT', TRUE, TRUE, 1, 2,
 '空链表数组', '输出为空', 1),

-- 功能测试用例
(3, '单个链表', 'FUNCTIONAL', '1\n5 1 2 3 4 5', '1 2 3 4 5', 'TEXT', FALSE, FALSE, 2, 3,
 '只有一个链表的情况', '输出该链表本身', 1),

(3, '包含空链表', 'FUNCTIONAL', '3\n0\n2 1 3\n1 2', '1 2 3', 'TEXT', FALSE, FALSE, 2, 4,
 '包含空链表的测试', '忽略空链表，正确合并非空链表', 1),

-- 边界测试用例
(3, '大量链表', 'BOUNDARY', '5\n2 1 4\n2 1 3\n2 1 2\n2 2 6\n1 5', '1 1 1 2 2 3 4 5 6', 'TEXT', FALSE, FALSE, 3, 5,
 '多个链表合并测试', '验证大量链表的合并效率', 1),

-- ========== 题目4：实现一个栈 (problem_id = 4) ==========
-- 示例测试用例
(4, '示例用例', 'EXAMPLE', '6\npush 1\npush 2\ntop\npop\nempty\npop', '2\n2\nfalse\n1', 'TEXT', TRUE, TRUE, 1, 1,
 '栈的基本操作测试', '验证push、pop、top、empty的正确性', 1),

-- 功能测试用例
(4, '空栈测试', 'FUNCTIONAL', '1\nempty', 'true', 'TEXT', FALSE, FALSE, 2, 2,
 '测试空栈的empty操作', '空栈应该返回true', 1),

(4, '单元素测试', 'FUNCTIONAL', '4\npush 42\ntop\npop\nempty', '42\n42\ntrue', 'TEXT', FALSE, FALSE, 2, 3,
 '单元素栈操作', '验证单元素的完整操作流程', 1),

(4, '多元素测试', 'FUNCTIONAL', '8\npush 1\npush 2\npush 3\ntop\npop\ntop\npop\ntop', '3\n3\n2\n2\n1', 'TEXT', FALSE, FALSE, 2, 4,
 '多元素栈操作', '验证LIFO特性', 1),

-- 边界测试用例
(4, '大量操作', 'BOUNDARY', '7\npush 100\npush 200\npush 300\npop\npop\npop\nempty', '300\n200\n100\ntrue', 'TEXT', FALSE, FALSE, 3, 5,
 '大量操作测试', '验证栈在大量操作下的正确性', 1),

-- ========== 题目5：Debug数组越界 (problem_id = 5) ==========
-- 示例测试用例
(5, '示例用例1', 'EXAMPLE', '5\n3 7 2 9 1', '9', 'TEXT', TRUE, TRUE, 1, 1,
 '正常数组最大值查找', '正确找到最大值9', 1),

(5, '示例用例2', 'EXAMPLE', '3\n-1 -5 -2', '-1', 'TEXT', TRUE, TRUE, 1, 2,
 '负数数组最大值查找', '正确找到最大值-1', 1),

-- 功能测试用例
(5, '单元素数组', 'FUNCTIONAL', '1\n42', '42', 'TEXT', FALSE, FALSE, 2, 3,
 '单元素数组测试', '单元素即为最大值', 1),

(5, '相同元素数组', 'FUNCTIONAL', '4\n5 5 5 5', '5', 'TEXT', FALSE, FALSE, 2, 4,
 '相同元素数组', '所有元素相同时的最大值', 1),

-- 边界测试用例
(5, '最大边界测试', 'BOUNDARY', '3\n-1000 1000 -500', '1000', 'TEXT', FALSE, FALSE, 2, 5,
 '边界值测试', '验证最大最小边界值', 1),

(5, '递增序列', 'FUNCTIONAL', '5\n1 2 3 4 5', '5', 'TEXT', FALSE, FALSE, 2, 6,
 '递增序列测试', '最后一个元素为最大值', 1),

-- ========== 题目6：回文字符串判断 (problem_id = 6) ==========
-- 示例测试用例
(6, '示例用例1', 'EXAMPLE', 'A man, a plan, a canal: Panama', 'true', 'TEXT', TRUE, TRUE, 1, 1,
 '复杂回文字符串', '忽略标点和大小写的回文判断', 1),

(6, '示例用例2', 'EXAMPLE', 'race a car', 'false', 'TEXT', TRUE, TRUE, 1, 2,
 '非回文字符串', '验证非回文的正确判断', 1),

-- 功能测试用例
(6, '简单回文', 'FUNCTIONAL', 'aba', 'true', 'TEXT', FALSE, FALSE, 2, 3,
 '简单回文字符串', '基本回文模式', 1),

(6, '空字符串', 'BOUNDARY', '', 'true', 'TEXT', FALSE, FALSE, 2, 4,
 '空字符串测试', '空字符串被认为是回文', 1),

(6, '单字符', 'BOUNDARY', 'a', 'true', 'TEXT', FALSE, FALSE, 2, 5,
 '单字符测试', '单字符是回文', 1),

(6, '只有标点', 'FUNCTIONAL', '.,', 'true', 'TEXT', FALSE, FALSE, 2, 6,
 '只有标点符号', '只有标点时应该是回文', 1),

(6, '数字回文', 'FUNCTIONAL', '12321', 'true', 'TEXT', FALSE, FALSE, 2, 7,
 '数字回文测试', '纯数字的回文判断', 1),

-- ========== 题目7：斐波那契数列 (problem_id = 7) ==========
-- 示例测试用例
(7, '示例用例1', 'EXAMPLE', '2', '1', 'TEXT', TRUE, TRUE, 1, 1,
 'F(2)的计算', 'F(2) = F(1) + F(0) = 1 + 0 = 1', 1),

(7, '示例用例2', 'EXAMPLE', '3', '2', 'TEXT', TRUE, TRUE, 1, 2,
 'F(3)的计算', 'F(3) = F(2) + F(1) = 1 + 1 = 2', 1),

(7, '示例用例3', 'EXAMPLE', '4', '3', 'TEXT', TRUE, TRUE, 1, 3,
 'F(4)的计算', 'F(4) = F(3) + F(2) = 2 + 1 = 3', 1),

-- 功能测试用例
(7, '基础用例F(0)', 'FUNCTIONAL', '0', '0', 'TEXT', FALSE, FALSE, 2, 4,
 'F(0)边界测试', '斐波那契数列的起始值', 1),

(7, '基础用例F(1)', 'FUNCTIONAL', '1', '1', 'TEXT', FALSE, FALSE, 2, 5,
 'F(1)边界测试', '斐波那契数列的第二个值', 1),

(7, '中等规模F(10)', 'FUNCTIONAL', '10', '55', 'TEXT', FALSE, FALSE, 2, 6,
 'F(10)功能测试', '验证中等规模的计算正确性', 1),

-- 边界测试用例
(7, '较大规模F(20)', 'BOUNDARY', '20', '6765', 'TEXT', FALSE, FALSE, 3, 7,
 'F(20)性能测试', '验证较大规模的计算效率', 1),

(7, '最大边界F(30)', 'BOUNDARY', '30', '832040', 'TEXT', FALSE, FALSE, 3, 8,
 'F(30)边界测试', '验证题目约束范围内的最大值', 1);

-- 更新测试用例的执行统计数据（模拟一些执行记录）
UPDATE test_case SET execution_count = 45, success_count = 43 WHERE problem_id = 1;
UPDATE test_case SET execution_count = 32, success_count = 28 WHERE problem_id = 2;
UPDATE test_case SET execution_count = 18, success_count = 14 WHERE problem_id = 3;
UPDATE test_case SET execution_count = 56, success_count = 52 WHERE problem_id = 4;
UPDATE test_case SET execution_count = 78, success_count = 74 WHERE problem_id = 5;
UPDATE test_case SET execution_count = 41, success_count = 39 WHERE problem_id = 6;
UPDATE test_case SET execution_count = 67, success_count = 63 WHERE problem_id = 7;
