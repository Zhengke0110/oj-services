package fun.timu.oj.judge.model.VO;

import lombok.Data;
import java.util.Date;

/**
 * 测试用例视图对象
 * @author zhengke
 */
@Data
public class TestCaseVO {

    /**
     * 测试用例ID
     */
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
     * 测试用例类型描述
     */
    private String caseTypeLabel;

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
     * 输入格式描述
     */
    private String inputFormatLabel;

    /**
     * 当input_format为ARGS时，存储参数数组
     */
    private Object inputArgs;

    /**
     * 是否为示例测试用例
     */
    private Boolean isExample;

    /**
     * 是否为公开测试用例(用户可见)
     */
    private Boolean isPublic;

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
     * 状态描述
     */
    private String statusLabel;

    /**
     * 执行次数统计
     */
    private Long executionCount;

    /**
     * 成功次数统计
     */
    private Long successCount;

    /**
     * 成功率
     */
    private Double successRate;

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
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
