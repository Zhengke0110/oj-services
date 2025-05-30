package fun.timu.oj.judge.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 代码执行记录表(优化版)
 *
 * @TableName code_execution_record
 */
@TableName(value = "code_execution_record")
@Data
public class CodeExecutionRecordDO implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求唯一标识
     */
    private String requestId;

    /**
     * 编程语言(JAVA/JAVASCRIPT/PYTHON)
     */
    private String language;

    /**
     * 执行的代码内容
     */
    private String codeContent;

    /**
     * 代码内容SHA256哈希值
     */
    private String codeHash;

    /**
     * 使用的Docker镜像
     */
    private String dockerImage;

    /**
     * 使用的容器ID
     */
    private String containerId;

    /**
     * 执行主机
     */
    private String executionHost;

    /**
     * 执行模式(NO_INPUT/WITH_ARGS)
     */
    private String executionMode;

    /**
     * 命令行参数(JSON数组格式)
     */
    private Object commandArgs;

    /**
     * 请求执行次数
     */
    private Integer executionCount;

    /**
     * 实际执行次数
     */
    private Integer actualExecutionCount;

    /**
     * 是否成功执行
     */
    private Integer success;

    /**
     * 执行状态(SUCCEED/FAILED/COMPILATION_ERROR/RUNTIME_ERROR/TIMEOUT_ERROR)
     */
    private String executionStatus;

    /**
     * 实际输出(JSON数组格式，支持多次执行)
     */
    private Object actualOutput;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间(毫秒)
     */
    private Long executionTime;

    /**
     * 内存使用(字节)
     */
    private Long memoryUsed;

    /**
     * 最大执行时间(毫秒)
     */
    private Long maxExecutionTime;

    /**
     * 最大内存使用(字节)
     */
    private Long maxMemoryUsed;

    /**
     * 用户唯一标识
     */
    private Long accountNo;

    /**
     * 问题ID
     */
    private Long problemId;

    /**
     * 解决方案ID
     */
    private Long solutionId;

    /**
     * 预期输出(判题时使用)
     */
    private String expectedOutput;

    /**
     * 输出是否匹配预期(判题结果)
     */
    private Integer outputMatched;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 提交时间戳(毫秒)
     */
    private Long submissionTime;

    /**
     * 开始执行时间戳(毫秒)
     */
    private Long executionStartTime;

    /**
     * 结束执行时间戳(毫秒)
     */
    private Long executionEndTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 附加信息(JSON格式)
     */
    private Object additionalInfo;

    /**
     * 是否已删除(软删除)
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}