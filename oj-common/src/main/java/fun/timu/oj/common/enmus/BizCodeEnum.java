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
    ACCOUNT_UPDATE_ERROR(20009, "账号更新失败"),

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
     * 题目相关错误码：60000开头
     */
    PROBLEM_NOT_EXIST(60001, "题目不存在"),
    PROBLEM_TITLE_EMPTY(60002, "题目标题不能为空"),
    PROBLEM_TITLE_TOO_LONG(60003, "题目标题过长"),
    PROBLEM_DESCRIPTION_EMPTY(60004, "题目描述不能为空"),
    PROBLEM_DESCRIPTION_TOO_LONG(60005, "题目描述过长"),
    PROBLEM_TYPE_INVALID(60006, "题目类型不正确"),
    PROBLEM_DIFFICULTY_INVALID(60007, "题目难度级别不正确"),
    PROBLEM_TIME_LIMIT_INVALID(60008, "时间限制不正确"),
    PROBLEM_MEMORY_LIMIT_INVALID(60009, "内存限制不正确"),
    PROBLEM_LANGUAGE_NOT_SUPPORTED(60010, "不支持的编程语言"),
    PROBLEM_TEMPLATE_INVALID(60011, "代码模板格式不正确"),
    PROBLEM_INPUT_FORMAT_INVALID(60012, "输入格式不正确"),
    PROBLEM_EXAMPLES_INVALID(60013, "示例格式不正确"),
    PROBLEM_STATUS_INVALID(60014, "题目状态不正确"),
    PROBLEM_VISIBILITY_INVALID(60015, "题目可见性设置不正确"),
    PROBLEM_CREATE_FAILED(60016, "题目创建失败"),
    PROBLEM_UPDATE_FAILED(60017, "题目更新失败"),
    PROBLEM_DELETE_FAILED(60018, "题目删除失败"),
    PROBLEM_PERMISSION_DENIED(60019, "无权限操作该题目"),
    PROBLEM_ALREADY_DELETED(60020, "题目已被删除"),
    PROBLEM_TITLE_EXIST(60021, "题目标题已存在"),
    PROBLEM_CONSTRAINTS_TOO_LONG(60022, "约束条件描述过长"),
    PROBLEM_HINTS_INVALID(60023, "提示信息格式不正确"),
    PROBLEM_METADATA_INVALID(60024, "元数据格式不正确"),
    PROBLEM_SUPPORTED_LANGUAGES_EMPTY(60025, "支持的编程语言不能为空"),
    PROBLEM_INPUT_DESCRIPTION_TOO_LONG(60026, "输入描述过长"),
    PROBLEM_OUTPUT_DESCRIPTION_TOO_LONG(60027, "输出描述过长"),
    PROBLEM_NOTES_TOO_LONG(60028, "备注信息过长"),
    PROBLEM_IN_USE(60029, "题目正在使用中，无法删除"),
    PROBLEM_BATCH_OPERATION_FAILED(60030, "题目批量操作失败"),

    /**
     * 标签相关错误码：70000开头
     */
    TAG_NAME_EXIST(70001, "标签名称已存在"),
    TAG_NOT_EXIST(70002, "标签不存在"),
    TAG_NAME_INVALID(70003, "标签名称格式不正确"),
    TAG_DESCRIPTION_TOO_LONG(70004, "标签描述过长"),
    TAG_CATEGORY_INVALID(70005, "标签分类不正确"),
    TAG_IN_USE(70006, "标签正在使用中，无法删除"),
    TAG_RELATION_EXIST(70007, "问题标签关联已存在"),
    TAG_RELATION_NOT_EXIST(70008, "问题标签关联不存在"),
    TAG_UPDATE_FAILED(70009, "标签更新失败"),
    TAG_DELETE_FAILED(70010, "标签删除失败"),
    TAG_CREATE_FAILED(70011, "标签创建失败"),
    TAG_COLORS_FAILED(70012, "颜色列表获取失败"),
    TAG_GAT_FAILED(70013, "标签获取失败"),
    ;

    private String message;
    private int code;

    private BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
