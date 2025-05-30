package fun.timu.oj.account.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户账户表(优化版，兼容沙箱执行记录)
 * @TableName account
 */
@TableName(value ="account")
@Data
public class AccountDO implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户唯一标识(业务主键)
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
     * 密码(加密后)
     */
    private String pwd;

    /**
     * 盐，用于个人敏感信息处理
     */
    private String secret;

    /**
     * 认证级别：DEFAULT，REALNAME，ENTERPRISE
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
    private Long lastExecutionTime;

    /**
     * 单次执行超时时间(毫秒)
     */
    private Integer executionTimeout;

    /**
     * 内存限制(字节,默认256MB)
     */
    private Long memoryLimit;

    /**
     * 是否已删除(软删除)
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 最后登录时间
     */
    private Date lastLoginAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}