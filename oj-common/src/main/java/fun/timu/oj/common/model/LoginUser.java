package fun.timu.oj.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {
    /**
     * 用户唯一标识
     */
    private Long accountNo;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String headImg;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 认证级别：DEFAULT，REALNAME，ENTERPRISE
     */
    private String auth;

    /**
     * 账户状态：0-禁用，1-启用，2-锁定
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private Date lastLoginAt;
}