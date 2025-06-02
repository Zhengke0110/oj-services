-- code_execution_record表的模拟插入数据
-- 为不同用户、题目、语言创建代码执行记录

-- 插入代码执行记录数据
INSERT INTO code_execution_record (
    request_id, language, code_content, code_hash, docker_image, container_id, execution_host,
    execution_mode, command_args, execution_count, actual_execution_count, success, execution_status,
    actual_output, error_message, execution_time, memory_used, max_execution_time, max_memory_used,
    account_no, problem_id, solution_id, expected_output, output_matched, client_ip, user_agent,
    submission_time, execution_start_time, execution_end_time, additional_info
) VALUES 

-- ========== 题目1：两数之和 - 成功提交 ==========
-- 用户1001 Java成功提交
('req_001_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < nums.length; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                return new int[] { map.get(complement), i };\n            }\n            map.put(nums[i], i);\n        }\n        return new int[0];\n    }\n}',
'a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456',
'oj-java:openjdk-11', 'container_java_001', 'oj-host-01',
'WITH_ARGS', JSON_ARRAY('4', '9', '2', '7', '11', '15'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('0 1'), NULL, 245, 15728640, 245, 15728640,
1001, 1, 1001, '0 1', TRUE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
1717200000000, 1717200000100, 1717200000345, 
JSON_OBJECT('compiler_version', 'openjdk-11.0.15', 'test_case_id', 1)
),

-- 用户1002 Python成功提交
('req_002_' || UNIX_TIMESTAMP() * 1000, 'PYTHON', 
'def two_sum(nums, target):\n    num_map = {}\n    for i, num in enumerate(nums):\n        complement = target - num\n        if complement in num_map:\n            return [num_map[complement], i]\n        num_map[num] = i\n    return []',
'b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456a1',
'oj-python:3.9', 'container_python_001', 'oj-host-02',
'WITH_ARGS', JSON_ARRAY('4', '9', '2', '7', '11', '15'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('0 1'), NULL, 156, 8388608, 156, 8388608,
1002, 1, 1002, '0 1', TRUE, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
1717200300000, 1717200300050, 1717200300206, 
JSON_OBJECT('python_version', '3.9.16', 'test_case_id', 1)
),

-- ========== 题目1：两数之和 - 错误提交 ==========
-- 用户1003 Java编译错误
('req_003_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // 缺少返回语句的错误代码\n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < nums.length; i++) {\n            // 忘记返回结果\n        }\n    }\n}',
'c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456a1b2',
'oj-java:openjdk-11', 'container_java_002', 'oj-host-01',
'WITH_ARGS', JSON_ARRAY('4', '9', '2', '7', '11', '15'), 1, 0, FALSE, 'COMPILATION_ERROR',
NULL, 'Solution.java:8: error: missing return statement\n    }\n    ^\n1 error', 0, 0, 1000, 268435456,
1003, 1, NULL, '0 1', FALSE, '192.168.1.102', 'Mozilla/5.0 (Linux; X11)',
1717200600000, 1717200600050, 1717200600050, 
JSON_OBJECT('compiler_error', 'missing return statement')
),

-- ========== 题目2：无重复字符的最长子串 ==========
-- 用户1001 Python成功提交
('req_004_' || UNIX_TIMESTAMP() * 1000, 'PYTHON', 
'def length_of_longest_substring(s):\n    char_map = {}\n    left = 0\n    max_length = 0\n    \n    for right in range(len(s)):\n        if s[right] in char_map:\n            left = max(left, char_map[s[right]] + 1)\n        char_map[s[right]] = right\n        max_length = max(max_length, right - left + 1)\n    \n    return max_length',
'd4e5f6789012345678901234567890abcdef1234567890abcdef123456a1b2c3',
'oj-python:3.9', 'container_python_002', 'oj-host-02',
'WITH_ARGS', JSON_ARRAY('abcabcbb'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('3'), NULL, 189, 9437184, 189, 9437184,
1001, 2, 1003, '3', TRUE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
1717201200000, 1717201200080, 1717201200269, 
JSON_OBJECT('algorithm', 'sliding_window', 'test_case_id', 8)
),

-- ========== 题目3：合并K个升序链表 - 超时错误 ==========
-- 用户1002 Java超时
('req_005_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'class Solution {\n    public ListNode mergeKLists(ListNode[] lists) {\n        // 低效的逐一合并方法\n        ListNode result = null;\n        for (ListNode list : lists) {\n            result = mergeTwoLists(result, list);\n        }\n        return result;\n    }\n    \n    private ListNode mergeTwoLists(ListNode l1, ListNode l2) {\n        // 递归合并两个链表\n        if (l1 == null) return l2;\n        if (l2 == null) return l1;\n        if (l1.val < l2.val) {\n            l1.next = mergeTwoLists(l1.next, l2);\n            return l1;\n        } else {\n            l2.next = mergeTwoLists(l1, l2.next);\n            return l2;\n        }\n    }\n}',
'e5f6789012345678901234567890abcdef1234567890abcdef123456a1b2c3d4',
'oj-java:openjdk-11', 'container_java_003', 'oj-host-01',
'WITH_ARGS', JSON_ARRAY('3', '3', '1', '4', '5', '3', '1', '3', '4', '2', '2', '6'), 1, 1, FALSE, 'TIMEOUT_ERROR',
NULL, 'Time Limit Exceeded: execution time > 3000ms', 3000, 67108864, 3000, 67108864,
1002, 3, NULL, '1 1 2 3 4 4 5 6', FALSE, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
1717201800000, 1717201800100, 1717201803100, 
JSON_OBJECT('timeout_reason', 'inefficient_algorithm', 'test_case_id', 13)
),

-- ========== 题目4：实现一个栈 ==========
-- 用户1003 Java成功提交
('req_006_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'class MyStack {\n    private List<Integer> stack;\n    \n    public MyStack() {\n        stack = new ArrayList<>();\n    }\n    \n    public void push(int x) {\n        stack.add(x);\n    }\n    \n    public int pop() {\n        return stack.remove(stack.size() - 1);\n    }\n    \n    public int top() {\n        return stack.get(stack.size() - 1);\n    }\n    \n    public boolean empty() {\n        return stack.isEmpty();\n    }\n}',
'f6789012345678901234567890abcdef1234567890abcdef123456a1b2c3d4e5',
'oj-java:openjdk-11', 'container_java_004', 'oj-host-02',
'WITH_ARGS', JSON_ARRAY('6', 'push 1', 'push 2', 'top', 'pop', 'empty', 'pop'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('2\n2\nfalse\n1'), NULL, 167, 12582912, 167, 12582912,
1003, 4, 1004, '2\n2\nfalse\n1', TRUE, '192.168.1.102', 'Mozilla/5.0 (Linux; X11)',
1717202400000, 1717202400060, 1717202400227, 
JSON_OBJECT('data_structure', 'stack', 'test_case_id', 17)
),

-- ========== 题目5：Debug数组越界 ==========
-- 用户1001 Java修复后成功
('req_007_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'public class Solution {\n    public int findMax(int[] nums) {\n        int max = nums[0];\n        for (int i = 1; i < nums.length; i++) {  // 修复: i <= 改为 i <\n            if (nums[i] > max) {\n                max = nums[i];\n            }\n        }\n        return max;\n    }\n}',
'g789012345678901234567890abcdef1234567890abcdef123456a1b2c3d4e5f6',
'oj-java:openjdk-11', 'container_java_005', 'oj-host-01',
'WITH_ARGS', JSON_ARRAY('5', '3', '7', '2', '9', '1'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('9'), NULL, 134, 10485760, 134, 10485760,
1001, 5, 1005, '9', TRUE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
1717203000000, 1717203000070, 1717203000204, 
JSON_OBJECT('bug_fixed', 'array_index_bounds', 'test_case_id', 22)
),

-- ========== 题目6：回文字符串判断 ==========
-- 用户1002 Python成功提交
('req_008_' || UNIX_TIMESTAMP() * 1000, 'PYTHON', 
'def is_palindrome(s):\n    # 转换为小写并过滤非字母数字字符\n    filtered = \"\".join(c.lower() for c in s if c.isalnum())\n    # 双指针判断回文\n    left, right = 0, len(filtered) - 1\n    while left < right:\n        if filtered[left] != filtered[right]:\n            return False\n        left += 1\n        right -= 1\n    return True',
'h89012345678901234567890abcdef1234567890abcdef123456a1b2c3d4e5f6g7',
'oj-python:3.9', 'container_python_003', 'oj-host-02',
'WITH_ARGS', JSON_ARRAY('A man, a plan, a canal: Panama'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('true'), NULL, 198, 11534336, 198, 11534336,
1002, 6, 1006, 'true', TRUE, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
1717203600000, 1717203600090, 1717203600288, 
JSON_OBJECT('algorithm', 'two_pointers', 'test_case_id', 28)
),

-- ========== 题目7：斐波那契数列 ==========
-- 用户1003 Python递归版本（较慢但正确）
('req_009_' || UNIX_TIMESTAMP() * 1000, 'PYTHON', 
'def fib(n):\n    if n <= 1:\n        return n\n    return fib(n-1) + fib(n-2)',
'i9012345678901234567890abcdef1234567890abcdef123456a1b2c3d4e5f6g7h8',
'oj-python:3.9', 'container_python_004', 'oj-host-01',
'WITH_ARGS', JSON_ARRAY('10'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('55'), NULL, 1245, 8912896, 1245, 8912896,
1003, 7, 1007, '55', TRUE, '192.168.1.102', 'Mozilla/5.0 (Linux; X11)',
1717204200000, 1717204200100, 1717204201345, 
JSON_OBJECT('algorithm', 'recursion', 'optimization', 'could_use_dp', 'test_case_id', 35)
),

-- 用户1001 Java动态规划版本（高效）
('req_010_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'class Solution {\n    public int fib(int n) {\n        if (n <= 1) return n;\n        int a = 0, b = 1;\n        for (int i = 2; i <= n; i++) {\n            int temp = a + b;\n            a = b;\n            b = temp;\n        }\n        return b;\n    }\n}',
'j012345678901234567890abcdef1234567890abcdef123456a1b2c3d4e5f6g7h8i9',
'oj-java:openjdk-11', 'container_java_006', 'oj-host-02',
'WITH_ARGS', JSON_ARRAY('20'), 1, 1, TRUE, 'SUCCEED',
JSON_ARRAY('6765'), NULL, 89, 9961472, 89, 9961472,
1001, 7, 1008, '6765', TRUE, '192.168.1.100', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
1717204800000, 1717204800050, 1717204800139, 
JSON_OBJECT('algorithm', 'dynamic_programming', 'optimization', 'iterative', 'test_case_id', 36)
),

-- ========== 运行时错误示例 ==========
-- 用户1002 Java运行时错误（空指针异常）
('req_011_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        Map<Integer, Integer> map = null;  // 故意的空指针\n        for (int i = 0; i < nums.length; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                return new int[] { map.get(complement), i };\n            }\n            map.put(nums[i], i);\n        }\n        return new int[0];\n    }\n}',
'k12345678901234567890abcdef1234567890abcdef123456a1b2c3d4e5f6g7h8i9j0',
'oj-java:openjdk-11', 'container_java_007', 'oj-host-01',
'WITH_ARGS', JSON_ARRAY('4', '9', '2', '7', '11', '15'), 1, 1, FALSE, 'RUNTIME_ERROR',
NULL, 'Exception in thread "main" java.lang.NullPointerException\n\tat Solution.twoSum(Solution.java:5)', 78, 14680064, 78, 14680064,
1002, 1, NULL, '0 1', FALSE, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
1717205400000, 1717205400060, 1717205400138, 
JSON_OBJECT('error_type', 'NullPointerException', 'test_case_id', 2)
),

-- ========== 内存超限示例 ==========
-- 用户1003创建大数组导致内存超限
('req_012_' || UNIX_TIMESTAMP() * 1000, 'JAVA', 
'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // 故意创建大数组\n        int[] bigArray = new int[Integer.MAX_VALUE / 100];\n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < nums.length; i++) {\n            map.put(nums[i], i);\n        }\n        return new int[0];\n    }\n}',
'l23456789012345678901234567890abcdef1234567890abcdef123456a1b2c3d4e5f6g7h8i9j0k1',
'oj-java:openjdk-11', 'container_java_008', 'oj-host-02',
'WITH_ARGS', JSON_ARRAY('4', '9', '2', '7', '11', '15'), 1, 1, FALSE, 'RUNTIME_ERROR',
NULL, 'java.lang.OutOfMemoryError: Java heap space', 234, 268435456, 234, 268435456,
1003, 1, NULL, '0 1', FALSE, '192.168.1.102', 'Mozilla/5.0 (Linux; X11)',
1717206000000, 1717206000080, 1717206000314, 
JSON_OBJECT('error_type', 'OutOfMemoryError', 'memory_exceeded', true)
);

-- 更新用户的执行统计信息
UPDATE account SET 
    total_execution_count = (
        SELECT COUNT(*) 
        FROM code_execution_record 
        WHERE account_no = account.account_no AND is_deleted = FALSE
    ),
    last_execution_time = (
        SELECT MAX(submission_time)
        FROM code_execution_record 
        WHERE account_no = account.account_no AND is_deleted = FALSE
    )
WHERE account_no IN (1001, 1002, 1003);

-- 更新题目的提交统计信息
UPDATE problem SET 
    submission_count = (
        SELECT COUNT(*) 
        FROM code_execution_record 
        WHERE problem_id = problem.id AND is_deleted = FALSE
    ),
    accepted_count = (
        SELECT COUNT(*) 
        FROM code_execution_record 
        WHERE problem_id = problem.id AND success = TRUE AND is_deleted = FALSE
    )
WHERE id IN (1, 2, 3, 4, 5, 6, 7);
