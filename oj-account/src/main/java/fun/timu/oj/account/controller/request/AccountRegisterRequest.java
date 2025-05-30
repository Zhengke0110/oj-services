package fun.timu.oj.account.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class AccountRegisterRequest {
    /**
     * 头像
     */
    private String headImg;
    /**
     * 昵称
     */
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20个字符之间")
    private String pwd;

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码长度不正确")
    private String code;
}
