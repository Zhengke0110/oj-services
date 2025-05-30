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
    FILE_UPLOAD_USER_IMG_FAIL(40001, "用户头像文件上传失败");

    private String message;
    private int code;

    private BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
