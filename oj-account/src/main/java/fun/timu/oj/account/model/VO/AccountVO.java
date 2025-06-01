package fun.timu.oj.account.model.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountVO {
    /**
     * 用户唯一标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 用户权限：DEFAULT-默认用户，ADMIN-管理员
     */
    private String auth;

    /**
     * 账户状态：0-禁用，1-启用，2-锁定
     */
    private Integer status;

    /**
     * 总代码执行次数
     */
    private Long totalExecutionCount;

    /**
     * 最后执行时间戳(毫秒)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastExecutionTime;

    /**
     * 单次执行超时时间(毫秒)
     */
    private Integer executionTimeout;

    /**
     * 内存限制(字节)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memoryLimit;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginAt;
}
