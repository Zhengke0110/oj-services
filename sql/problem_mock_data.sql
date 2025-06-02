-- problem表的模拟插入数据

-- 插入算法题目数据
INSERT INTO problem (
    title, title_en, description, description_en, problem_type, difficulty, 
    time_limit, memory_limit, supported_languages, solution_templates,
    input_description, output_description, has_input, input_format,
    examples, status, visibility, hints, constraints, notes, metadata, creator_id
) VALUES 

-- 1. 简单算法题：两数之和
(
    '两数之和', 
    'Two Sum',
    '给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值 target 的那两个整数，并返回它们的数组下标。',
    'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.',
    'ALGORITHM',
    0, -- 简单
    1000, -- 1秒
    268435456, -- 256MB
    JSON_ARRAY('JAVA', 'PYTHON', 'CPP', 'JAVASCRIPT'),
    JSON_OBJECT(
        'JAVA', 'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // 请在此处实现你的代码\n        return new int[0];\n    }\n}',
        'PYTHON', 'def two_sum(nums, target):\n    # 请在此处实现你的代码\n    pass',
        'CPP', '#include <vector>\nusing namespace std;\n\nclass Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        // 请在此处实现你的代码\n        return {};\n    }\n};'
    ),
    '第一行包含数组长度n和目标值target，第二行包含n个整数。',
    '输出两个整数的下标，用空格分隔。',
    TRUE,
    'TEXT',
    JSON_ARRAY(
        JSON_OBJECT(
            'input', '4 9\n2 7 11 15',
            'output', '0 1',
            'explanation', '因为 nums[0] + nums[1] == 9，返回 [0, 1]。'
        ),
        JSON_OBJECT(
            'input', '3 6\n3 2 4',
            'output', '1 2',
            'explanation', '因为 nums[1] + nums[2] == 6，返回 [1, 2]。'
        )
    ),
    1, -- 启用
    1, -- 公开
    JSON_ARRAY(
        '可以考虑使用哈希表来优化时间复杂度',
        '遍历数组时，对于每个元素，检查target减去当前元素的值是否存在于哈希表中',
        '如果存在，则找到了答案；否则将当前元素及其索引存入哈希表'
    ),
    '数组长度范围：2 ≤ nums.length ≤ 10^4\n数值范围：-10^9 ≤ nums[i] ≤ 10^9\n-10^9 ≤ target ≤ 10^9\n只会存在一个有效答案',
    '经典的哈希表应用题目，适合入门学习',
    JSON_OBJECT('tags', JSON_ARRAY('数组', '哈希表'), 'source', 'LeetCode'),
    1001
),

-- 2. 中等算法题：无重复字符的最长子串
(
    '无重复字符的最长子串',
    'Longest Substring Without Repeating Characters',
    '给定一个字符串 s ，请你找出其中不含有重复字符的最长子串的长度。',
    'Given a string s, find the length of the longest substring without repeating characters.',
    'ALGORITHM',
    1, -- 中等
    2000, -- 2秒
    268435456,
    JSON_ARRAY('JAVA', 'PYTHON', 'CPP', 'JAVASCRIPT'),
    JSON_OBJECT(
        'JAVA', 'class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        // 请在此处实现你的代码\n        return 0;\n    }\n}',
        'PYTHON', 'def length_of_longest_substring(s):\n    # 请在此处实现你的代码\n    pass'
    ),
    '输入一行字符串s。',
    '输出最长子串的长度。',
    TRUE,
    'TEXT',
    JSON_ARRAY(
        JSON_OBJECT(
            'input', 'abcabcbb',
            'output', '3',
            'explanation', '因为无重复字符的最长子串是 "abc"，所以其长度为 3。'
        ),
        JSON_OBJECT(
            'input', 'bbbbb',
            'output', '1',
            'explanation', '因为无重复字符的最长子串是 "b"，所以其长度为 1。'
        )
    ),
    1,
    1,
    JSON_ARRAY(
        '考虑使用滑动窗口技术',
        '使用双指针维护一个窗口，右指针扩展窗口，左指针收缩窗口',
        '可以使用Set或Map来记录窗口中的字符'
    ),
    '字符串长度：0 ≤ s.length ≤ 5 * 10^4\ns 由英文字母、数字、符号和空格组成',
    '滑动窗口经典题目',
    JSON_OBJECT('tags', JSON_ARRAY('字符串', '滑动窗口', '哈希表')),
    1001
),

-- 3. 困难算法题：合并K个升序链表
(
    '合并K个升序链表',
    'Merge k Sorted Lists',
    '给你一个链表数组，每个链表都已经按升序排列。请你将所有链表合并到一个升序链表中，返回合并后的链表。',
    'You are given an array of k linked-lists lists, each linked-list is sorted in ascending order. Merge all the linked-lists into one sorted linked-list and return it.',
    'ALGORITHM',
    2, -- 困难
    3000, -- 3秒
    536870912, -- 512MB
    JSON_ARRAY('JAVA', 'PYTHON', 'CPP'),
    JSON_OBJECT(
        'JAVA', '/**\n * Definition for singly-linked list.\n * public class ListNode {\n *     int val;\n *     ListNode next;\n *     ListNode() {}\n *     ListNode(int val) { this.val = val; }\n *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }\n * }\n */\nclass Solution {\n    public ListNode mergeKLists(ListNode[] lists) {\n        // 请在此处实现你的代码\n        return null;\n    }\n}',
        'PYTHON', '# Definition for singly-linked list.\n# class ListNode:\n#     def __init__(self, val=0, next=None):\n#         self.val = val\n#         self.next = next\ndef merge_k_lists(lists):\n    # 请在此处实现你的代码\n    pass'
    ),
    '第一行包含链表数量k，接下来k行，每行先输入链表长度n，然后输入n个升序整数。',
    '输出合并后的升序链表，用空格分隔。如果结果为空，输出空行。',
    TRUE,
    'TEXT',
    JSON_ARRAY(
        JSON_OBJECT(
            'input', '3\n3 1 4 5\n3 1 3 4\n2 2 6',
            'output', '1 1 2 3 4 4 5 6',
            'explanation', '合并后的链表为：1->1->2->3->4->4->5->6'
        ),
        JSON_OBJECT(
            'input', '0',
            'output', '',
            'explanation', '输入为空，输出也为空'
        )
    ),
    1,
    1,
    JSON_ARRAY(
        '可以考虑使用分治法，将k个链表两两合并',
        '也可以使用优先队列（最小堆）来维护当前最小的节点',
        '逐一合并的方法时间复杂度较高，不推荐'
    ),
    'k == lists.length\n0 ≤ k ≤ 10^4\n0 ≤ lists[i].length ≤ 500\n-10^4 ≤ lists[i][j] ≤ 10^4\nlists[i] 按升序排列',
    '分治算法和堆的经典应用',
    JSON_OBJECT('tags', JSON_ARRAY('链表', '分治', '堆', '归并排序')),
    1002
),

-- 4. 编程练习题：实现栈
(
    '实现一个栈',
    'Implement Stack',
    '请你实现一个栈，支持以下操作：\n1. push(x)：将元素x压入栈顶\n2. pop()：弹出栈顶元素\n3. top()：获取栈顶元素\n4. empty()：判断栈是否为空',
    'Implement a stack that supports the following operations: push, pop, top, and empty.',
    'PRACTICE',
    0, -- 简单
    1000,
    134217728, -- 128MB
    JSON_ARRAY('JAVA', 'PYTHON', 'CPP'),
    JSON_OBJECT(
        'JAVA', 'class MyStack {\n    \n    public MyStack() {\n        // 初始化代码\n    }\n    \n    public void push(int x) {\n        // 实现push操作\n    }\n    \n    public int pop() {\n        // 实现pop操作\n        return -1;\n    }\n    \n    public int top() {\n        // 实现top操作\n        return -1;\n    }\n    \n    public boolean empty() {\n        // 实现empty操作\n        return true;\n    }\n}',
        'PYTHON', 'class MyStack:\n    def __init__(self):\n        # 初始化代码\n        pass\n    \n    def push(self, x):\n        # 实现push操作\n        pass\n    \n    def pop(self):\n        # 实现pop操作\n        pass\n    \n    def top(self):\n        # 实现top操作\n        pass\n    \n    def empty(self):\n        # 实现empty操作\n        pass'
    ),
    '第一行包含操作数量n，接下来n行每行包含一个操作。操作格式：\n- "push x"：将x压入栈\n- "pop"：弹出栈顶元素\n- "top"：获取栈顶元素\n- "empty"：判断是否为空',
    '对于pop和top操作，输出对应的值；对于empty操作，输出true或false。',
    TRUE,
    'TEXT',
    JSON_ARRAY(
        JSON_OBJECT(
            'input', '6\npush 1\npush 2\ntop\npop\nempty\npop',
            'output', '2\n2\nfalse\n1',
            'explanation', '依次执行操作：压入1，压入2，获取栈顶(2)，弹出栈顶(2)，判断是否为空(false)，弹出栈顶(1)'
        )
    ),
    1,
    1,
    JSON_ARRAY(
        '可以使用数组或链表来实现栈',
        '注意维护栈顶指针',
        '考虑边界情况，如空栈时的pop和top操作'
    ),
    '1 ≤ n ≤ 1000\n1 ≤ x ≤ 10^9\n保证操作的合法性',
    '数据结构基础练习',
    JSON_OBJECT('tags', JSON_ARRAY('栈', '数据结构', '设计')),
    1003
),

-- 5. 代码调试题：数组越界问题
(
    'Debug：修复数组越界',
    'Debug: Fix Array Index Out of Bounds',
    '以下代码试图找到数组中的最大值，但存在数组越界的问题。请找出并修复这个bug。\n\n```java\npublic class Solution {\n    public int findMax(int[] nums) {\n        int max = nums[0];\n        for (int i = 1; i <= nums.length; i++) {\n            if (nums[i] > max) {\n                max = nums[i];\n            }\n        }\n        return max;\n    }\n}\n```',
    'The following code tries to find the maximum value in an array, but has an array index out of bounds issue. Please identify and fix the bug.',
    'DEBUG',
    0, -- 简单
    500,
    67108864, -- 64MB
    JSON_ARRAY('JAVA'),
    JSON_OBJECT(
        'JAVA', 'public class Solution {\n    public int findMax(int[] nums) {\n        int max = nums[0];\n        for (int i = 1; i <= nums.length; i++) {\n            if (nums[i] > max) {\n                max = nums[i];\n            }\n        }\n        return max;\n    }\n}'
    ),
    '第一行包含数组长度n，第二行包含n个整数。',
    '输出数组中的最大值。',
    TRUE,
    'TEXT',
    JSON_ARRAY(
        JSON_OBJECT(
            'input', '5\n3 7 2 9 1',
            'output', '9',
            'explanation', '数组中的最大值是9'
        ),
        JSON_OBJECT(
            'input', '3\n-1 -5 -2',
            'output', '-1',
            'explanation', '数组中的最大值是-1'
        )
    ),
    1,
    1,
    JSON_ARRAY(
        '仔细检查循环边界条件',
        '数组索引是从0开始的',
        '考虑数组长度为1的特殊情况'
    ),
    '1 ≤ n ≤ 100\n-1000 ≤ nums[i] ≤ 1000',
    '常见的数组越界错误，考察基础编程能力',
    JSON_OBJECT('tags', JSON_ARRAY('调试', '数组', '循环')),
    1003
),

-- 6. 字符串处理题
(
    '回文字符串判断',
    'Valid Palindrome',
    '给定一个字符串，验证它是否是回文串，只考虑字母和数字字符，可以忽略字母的大小写。',
    'Given a string, determine if it is a palindrome, considering only alphanumeric characters and ignoring cases.',
    'ALGORITHM',
    0, -- 简单
    1000,
    134217728,
    JSON_ARRAY('JAVA', 'PYTHON', 'CPP', 'JAVASCRIPT'),
    JSON_OBJECT(
        'JAVA', 'class Solution {\n    public boolean isPalindrome(String s) {\n        // 请在此处实现你的代码\n        return false;\n    }\n}',
        'PYTHON', 'def is_palindrome(s):\n    # 请在此处实现你的代码\n    pass'
    ),
    '输入一行字符串s。',
    '如果是回文串输出true，否则输出false。',
    TRUE,
    'TEXT',
    JSON_ARRAY(
        JSON_OBJECT(
            'input', 'A man, a plan, a canal: Panama',
            'output', 'true',
            'explanation', '去除非字母数字字符并转换为小写后为"amanaplanacanalpanama"，这是一个回文串。'
        ),
        JSON_OBJECT(
            'input', 'race a car',
            'output', 'false',
            'explanation', '去除非字母数字字符并转换为小写后为"raceacar"，这不是一个回文串。'
        )
    ),
    1,
    1,
    JSON_ARRAY(
        '可以使用双指针技术',
        '先过滤掉非字母数字字符',
        '比较时忽略大小写'
    ),
    '1 ≤ s.length ≤ 2 * 10^5\ns 仅由可打印的 ASCII 字符组成',
    '双指针和字符串处理的基础题',
    JSON_OBJECT('tags', JSON_ARRAY('字符串', '双指针')),
    1001
),

-- 7. 动态规划题
(
    '斐波那契数列',
    'Fibonacci Number',
    '斐波那契数列通常用F(n)表示，形成的序列称为斐波那契数列。该数列由0和1开始，后面的每一项数字都是前面两项数字的和。',
    'The Fibonacci numbers, commonly denoted F(n) form a sequence, called the Fibonacci sequence.',
    'ALGORITHM',
    0, -- 简单
    1000,
    134217728,
    JSON_ARRAY('JAVA', 'PYTHON', 'CPP'),
    JSON_OBJECT(
        'JAVA', 'class Solution {\n    public int fib(int n) {\n        // 请在此处实现你的代码\n        return 0;\n    }\n}',
        'PYTHON', 'def fib(n):\n    # 请在此处实现你的代码\n    pass'
    ),
    '输入一个非负整数n。',
    '输出F(n)的值。',
    TRUE,
    'TEXT',
    JSON_ARRAY(
        JSON_OBJECT(
            'input', '2',
            'output', '1',
            'explanation', 'F(2) = F(1) + F(0) = 1 + 0 = 1'
        ),
        JSON_OBJECT(
            'input', '3',
            'output', '2',
            'explanation', 'F(3) = F(2) + F(1) = 1 + 1 = 2'
        ),
        JSON_OBJECT(
            'input', '4',
            'output', '3',
            'explanation', 'F(4) = F(3) + F(2) = 2 + 1 = 3'
        )
    ),
    1,
    1,
    JSON_ARRAY(
        '可以使用递归，但要注意效率问题',
        '动态规划可以避免重复计算',
        '也可以用迭代的方式，空间复杂度更优'
    ),
    '0 ≤ n ≤ 30',
    '动态规划入门题',
    JSON_OBJECT('tags', JSON_ARRAY('动态规划', '递归', '数学')),
    1002
);

-- 更新统计数据（模拟一些提交记录）
UPDATE problem SET submission_count = 150, accepted_count = 98 WHERE title = '两数之和';
UPDATE problem SET submission_count = 89, accepted_count = 34 WHERE title = '无重复字符的最长子串';
UPDATE problem SET submission_count = 45, accepted_count = 12 WHERE title = '合并K个升序链表';
UPDATE problem SET submission_count = 76, accepted_count = 65 WHERE title = '实现一个栈';
UPDATE problem SET submission_count = 123, accepted_count = 108 WHERE title = 'Debug：修复数组越界';
UPDATE problem SET submission_count = 67, accepted_count = 54 WHERE title = '回文字符串判断';
UPDATE problem SET submission_count = 91, accepted_count = 78 WHERE title = '斐波那契数列';
