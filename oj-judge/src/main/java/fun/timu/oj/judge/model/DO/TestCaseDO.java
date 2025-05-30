package fun.timu.oj.judge.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 测试用例表(优化版)
 *
 * @TableName test_case
 */
@TableName(value = "test_case")
@Data
public class TestCaseDO implements Serializable {
    /**
     * 测试用例ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的题目ID
     */
    private Long problemId;

    /**
     * 测试用例名称
     */
    private String caseName;

    /**
     * 测试用例类型：FUNCTIONAL(功能)，BOUNDARY(边界)，PERFORMANCE(性能)，EXAMPLE(示例)
     */
    private String caseType;

    /**
     * 输入数据(支持多行，JSON等格式)
     */
    private String inputData;

    /**
     * 期望输出结果
     */
    private String expectedOutput;

    /**
     * 输入格式：TEXT(文本)，JSON(JSON)，ARGS(命令行参数)，NONE(无输入)
     */
    private String inputFormat;

    /**
     * 当input_format为ARGS时，存储参数数组
     */
    private Object inputArgs;

    /**
     * 是否为示例测试用例
     */
    private Integer isExample;

    /**
     * 是否为公开测试用例(用户可见)
     */
    private Integer isPublic;

    /**
     * 测试用例权重
     */
    private Integer weight;

    /**
     * 执行顺序
     */
    private Integer orderIndex;

    /**
     * 时间限制覆盖(ms)，NULL表示使用题目默认值
     */
    private Integer timeLimitOverride;

    /**
     * 内存限制覆盖(字节)，NULL表示使用题目默认值
     */
    private Long memoryLimitOverride;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 执行次数统计
     */
    private Long executionCount;

    /**
     * 成功次数统计
     */
    private Long successCount;

    /**
     * 测试用例说明
     */
    private String description;

    /**
     * 预期行为描述
     */
    private String expectedBehavior;

    /**
     * 备注信息
     */
    private String notes;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}