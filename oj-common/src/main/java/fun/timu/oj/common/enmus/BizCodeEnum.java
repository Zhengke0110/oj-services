package fun.timu.oj.common.enmus;

import lombok.Getter;

@Getter

public enum BizCodeEnum {
    /**
     * 通用错误码：10000开头
     */
    SYSTEM_ERROR(10000, "系统错误"),
    PARAM_ERROR(10001, "参数错误"),

    /**
     * 账号相关错误码：20000开头
     */
    ACCOUNT_PHONE_INVALID(20001, "手机号格式不正确"),
    ACCOUNT_PWD_TOO_SHORT(20002, "密码太短，请输入至少6位"),
    ACCOUNT_NICKNAME_INVALID(20003, "昵称不能为空或超过20个字符"),
    ACCOUNT_PHONE_EXIST(20004, "手机号已存在"),
    ACCOUNT_REPEAT(20005, "账号已经存在"),
    ACCOUNT_UNREGISTER(20006, "账号不存在"),
    ACCOUNT_PWD_ERROR(20007, "账号或者密码错误"),
    ACCOUNT_UNLOGIN(20008, "账号未登录"),
    ACCOUNT_OPERATION_FAILED(20009, "账户操作失败"),
    ACCOUNT_STATUS_INVALID(20010, "账户状态不正确"),
    ACCOUNT_PERMISSION_DENIED(20011, "账户权限不足"),
    ACCOUNT_PARAM_INVALID(20012, "账户参数设置不正确"),

    /**
     * 验证码相关错误码：30000开头
     */
    CODE_TO_ERROR(30001, "接收号码不合规"),
    CODE_LIMITED(30002, "验证码发送过快"),
    CODE_ERROR(30003, "验证码错误"),
    CODE_CAPTCHA_ERROR(30004, "图形验证码错误"),

    /**
     * 文件相关错误码：40000开头
     */
    FILE_UPLOAD_USER_IMG_FAIL(40001, "用户头像文件上传失败"),

    /**
     * 判题相关错误码：50000开头
     */
    JUDGE_CODE_EMPTY(50001, "代码内容不能为空"),
    JUDGE_LANGUAGE_NOT_SUPPORT(50002, "不支持的编程语言"),
    JUDGE_CODE_TOO_LONG(50003, "代码长度超出限制"),
    JUDGE_COMPILATION_ERROR(50004, "代码编译失败"),
    JUDGE_RUNTIME_ERROR(50005, "代码运行时错误"),
    JUDGE_TIMEOUT_ERROR(50006, "代码执行超时"),
    JUDGE_MEMORY_LIMIT_EXCEEDED(50007, "内存使用超出限制"),
    JUDGE_WRONG_ANSWER(50008, "答案错误"),
    JUDGE_PRESENTATION_ERROR(50009, "格式错误"),
    JUDGE_SYSTEM_ERROR(50010, "判题系统错误"),
    JUDGE_DOCKER_ERROR(50011, "Docker容器执行错误"),
    JUDGE_QUEUE_FULL(50012, "判题队列已满，请稍后重试"),
    JUDGE_PROBLEM_NOT_EXIST(50013, "题目不存在"),
    JUDGE_TEST_CASE_NOT_EXIST(50014, "测试用例不存在"),
    JUDGE_SUBMIT_TOO_FREQUENT(50015, "提交过于频繁，请稍后重试"),
    JUDGE_EXECUTION_INTERRUPTED(50016, "代码执行被中断"),
    JUDGE_OUTPUT_LIMIT_EXCEEDED(50017, "输出长度超出限制"),
    JUDGE_SECURITY_VIOLATION(50018, "代码包含不安全操作"),
    JUDGE_UNKNOWN_ERROR(50019, "未知判题错误"),

    /**
     * 代码执行记录相关错误码：51000开头
     */
    EXECUTION_RECORD_NOT_EXIST(51001, "代码执行记录不存在"),
    EXECUTION_RECORD_OPERATION_FAILED(51002, "代码执行记录操作失败"),
    EXECUTION_RECORD_PARAM_INVALID(51003, "代码执行记录参数不正确"),
    EXECUTION_RECORD_STATUS_INVALID(51004, "代码执行记录状态不正确"),
    EXECUTION_RECORD_EXECUTION_ERROR(51005, "代码执行环境错误"),

    /**
     * 题目相关错误码：60000开头
     */
    PROBLEM_NOT_EXIST(60001, "题目不存在"),
    PROBLEM_PARAM_INVALID(60002, "题目参数不正确"),
    PROBLEM_CONTENT_INVALID(60003, "题目内容格式不正确"),
    PROBLEM_CONSTRAINT_INVALID(60004, "题目约束条件不正确"),
    PROBLEM_LANGUAGE_NOT_SUPPORTED(60005, "不支持的编程语言"),
    PROBLEM_STATUS_INVALID(60006, "题目状态不正确"),
    PROBLEM_OPERATION_FAILED(60007, "题目操作失败"),
    PROBLEM_PERMISSION_DENIED(60008, "无权限操作该题目"),
    PROBLEM_ALREADY_DELETED(60009, "题目已被删除"),
    PROBLEM_TITLE_EXIST(60010, "题目标题已存在"),
    PROBLEM_IN_USE(60011, "题目正在使用中，无法删除"),

    /**
     * 标签相关错误码：70000开头
     */
    TAG_NOT_EXIST(70001, "标签不存在"),
    TAG_NAME_EXIST(70002, "标签名称已存在"),
    TAG_PARAM_INVALID(70003, "标签参数不正确"),
    TAG_OPERATION_FAILED(70004, "标签操作失败"),
    TAG_IN_USE(70005, "标签正在使用中，无法删除"),
    TAG_RELATION_NOT_EXIST(70006, "问题标签关联不存在"),
    TAG_RELATION_EXIST(70007, "问题标签关联已存在"),

    /**
     * 测试用例相关错误码：80000开头
     */
    TEST_CASE_NOT_EXIST(80001, "测试用例不存在"),
    TEST_CASE_OPERATION_FAILED(80002, "测试用例操作失败"),
    TEST_CASE_PARAM_INVALID(80003, "测试用例参数不正确"),
    TEST_CASE_DATA_INVALID(80004, "测试用例数据格式不正确"),
    TEST_CASE_LIMIT_INVALID(80005, "测试用例限制条件不正确"),
    TEST_CASE_CONTENT_TOO_LONG(80006, "测试用例内容过长"),
    TEST_CASE_IN_USE(80007, "测试用例正在使用中，无法删除"),
    TEST_CASE_EXECUTION_FAILED(80008, "测试用例执行失败"),

    /**
     * 数据库操作相关错误码：90000开头
     */
    DATABASE_CONNECTION_ERROR(90001, "数据库连接错误"),
    DATABASE_OPERATION_ERROR(90002, "数据库操作错误"),
    DATABASE_TRANSACTION_ERROR(90003, "数据库事务错误"),
    DATABASE_CONSTRAINT_VIOLATION(90004, "数据库约束违反"),
    DATABASE_TIMEOUT_ERROR(90005, "数据库操作超时"),

    /**
     * 权限和安全相关错误码：91000开头
     */
    PERMISSION_DENIED(91001, "权限不足"),
    UNAUTHORIZED_ACCESS(91002, "未授权访问"),
    TOKEN_INVALID(91003, "令牌无效或已过期"),
    RATE_LIMIT_EXCEEDED(91004, "请求频率超限"),
    SECURITY_VALIDATION_FAILED(91005, "安全验证失败"),

    /**
     * 资源限制相关错误码：92000开头
     */
    RESOURCE_EXHAUSTED(92001, "系统资源不足"),
    RESOURCE_LIMIT_EXCEEDED(92002, "资源使用超限"),
    CONCURRENT_LIMIT_EXCEEDED(92003, "并发数量超限"),
    SERVICE_UNAVAILABLE(92004, "服务不可用"),

    /**
     * 系统集成相关错误码：93000开头
     */
    DOCKER_SERVICE_ERROR(93001, "Docker服务错误"),
    CONTAINER_OPERATION_FAILED(93002, "容器操作失败"),
    NETWORK_CONNECTION_ERROR(93003, "网络连接错误"),
    EXTERNAL_SERVICE_ERROR(93004, "外部服务错误"),
    SYSTEM_INTEGRATION_ERROR(93005, "系统集成错误"),

    /**
     * 批量处理相关错误码：94000开头
     */
    BATCH_OPERATION_FAILED(94001, "批量操作失败"),
    BATCH_REQUEST_EMPTY(94002, "批量请求数据为空"),
    BATCH_SIZE_EXCEEDED(94003, "批量操作数量超出限制"),
    BATCH_PARTIAL_SUCCESS(94004, "批量操作部分成功"),
    BATCH_VALIDATION_FAILED(94005, "批量数据验证失败"),
    BATCH_DUPLICATE_DATA(94006, "批量数据中存在重复项"),
    BATCH_INVALID_FORMAT(94007, "批量数据格式不正确"),
    BATCH_PROCESSING_INTERRUPTED(94008, "批量处理被中断"),
    BATCH_RESULT_INCONSISTENT(94009, "批量操作结果不一致"),
    BATCH_IMPORT_EXPORT_FAILED(94010, "批量导入导出失败"),
    ;

    private String message;
    private int code;

    private BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
