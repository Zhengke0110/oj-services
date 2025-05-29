package fun.timu.oj.common.enmus;

import lombok.Getter;

@Getter

public enum BizCodeEnum {
    CODE_TO_ERROR(240001, "接收号码不合规"),

    CODE_LIMITED(240002, "验证码发送过快"),

    CODE_ERROR(240003, "验证码错误"),

    CODE_CAPTCHA_ERROR(240101, "图形验证码错误"),
    /**
     * 账号
     */
    ACCOUNT_REPEAT(250001, "账号已经存在"),

    ACCOUNT_UNREGISTER(250002, "账号不存在"),

    ACCOUNT_PWD_ERROR(250003, "账号或者密码错误"),

    ACCOUNT_UNLOGIN(250004, "账号未登录"),

    ACCOUNT_UPDATE_ERROR(250005, "账号更新失败"),
    ;

    private String message;
    private int code;

    private BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
