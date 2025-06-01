package fun.timu.oj.judge.controller.request;

import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.enmus.ProblemStatusEnum;
import fun.timu.oj.common.enmus.ProblemVisibilityEnum;
import fun.timu.oj.judge.model.VO.ExampleVO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * 题目更新请求类
 * 只更新请求中包含的非空字段
 */
@Data
public class ProblemUpdateRequest {

    /**
     * 题目ID - 更新时必需
     */
    @NotNull(message = "题目ID不能为空")
    @Positive(message = "题目ID必须为正数")
    private Long id;

    /**
     * 题目标题
     */
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;

    /**
     * 英文标题
     */
    @Size(max = 200, message = "英文标题长度不能超过200个字符")
    private String titleEn;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 英文描述
     */
    private String descriptionEn;

    /**
     * 题目类型
     */
    private String problemType;

    /**
     * 难度级别
     */
    private ProblemDifficultyEnum difficulty;

    /**
     * 时间限制(ms)
     */
    private Long timeLimit;

    /**
     * 内存限制(byte)
     */
    private Long memoryLimit;

    /**
     * 支持的编程语言列表
     */
    private List<String> supportedLanguages;

    /**
     * 解题代码模板
     */
    private Map<String, String> solutionTemplates;

    /**
     * 输入描述
     */
    private String inputDescription;

    /**
     * 输出描述
     */
    private String outputDescription;

    /**
     * 是否需要输入数据
     */
    private Boolean hasInput;

    /**
     * 输入格式
     */
    private String inputFormat;

    /**
     * 题目示例
     */
    private List<ExampleVO> examples;

    /**
     * 题目状态 0-禁用 1-启用 2-草稿
     */
    private ProblemStatusEnum status;

    /**
     * 可见性 0-私有 1-公开 2-仅注册用户
     */
    private ProblemVisibilityEnum visibility;

    /**
     * 提示信息
     */
    private List<String> hints;

    /**
     * 约束条件
     */
    private String constraints;

    /**
     * 备注
     */
    private String notes;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;
}