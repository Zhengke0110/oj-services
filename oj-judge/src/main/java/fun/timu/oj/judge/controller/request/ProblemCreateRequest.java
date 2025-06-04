package fun.timu.oj.judge.controller.request;

import fun.timu.oj.judge.model.Enums.ProblemDifficultyEnum;
import fun.timu.oj.judge.model.Enums.ProblemStatusEnum;
import fun.timu.oj.judge.model.Enums.ProblemVisibilityEnum;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
public class ProblemCreateRequest {

    @NotBlank(message = "题目标题不能为空")
    @Size(max = 200, message = "题目标题长度不能超过200")
    private String title;

    @Size(max = 200, message = "英文标题长度不能超过200")
    private String titleEn;

    @NotBlank(message = "题目描述不能为空")
    private String description;

    private String descriptionEn;

    @NotBlank(message = "题目类型不能为空")
    @Pattern(regexp = "ALGORITHM|PRACTICE|DEBUG", message = "题目类型必须是 ALGORITHM, PRACTICE 或 DEBUG")
    private String problemType;

    @NotNull(message = "难度级别不能为空")
    private ProblemDifficultyEnum difficulty;

    @NotNull(message = "时间限制不能为空")
    @Min(value = 100, message = "时间限制最小为100ms")
    private Integer timeLimit;

    @NotNull(message = "内存限制不能为空")
    @Min(value = 1048576, message = "内存限制最小为1MB")
    private Long memoryLimit;

    @NotNull(message = "支持的编程语言不能为空")
    @Size(min = 1, message = "至少需要支持一种编程语言")
    private List<String> supportedLanguages;

    private Map<String, String> solutionTemplates;

    @Size(max = 1000, message = "输入描述长度不能超过1000")
    private String inputDescription;

    @Size(max = 1000, message = "输出描述长度不能超过1000")
    private String outputDescription;

    @NotNull(message = "是否需要输入数据不能为空")
    private Boolean hasInput;

    @NotBlank(message = "输入格式不能为空")
    @Pattern(regexp = "TEXT|JSON|ARGS", message = "输入格式必须是 TEXT, JSON 或 ARGS")
    private String inputFormat;

    private List<@NotNull Map<String, String>> examples;

    @NotNull(message = "状态不能为空")
    private ProblemStatusEnum status;

    @NotNull(message = "可见性不能为空")
    private ProblemVisibilityEnum visibility;

    private List<@Size(max = 500, message = "单个提示信息长度不能超过500") String> hints;

    @Size(max = 2000, message = "约束条件长度不能超过2000")
    private String constraints;

    @Size(max = 2000, message = "备注信息长度不能超过2000")
    private String notes;

    private Object metadata;

    // TODO 需要对接Tags
    private List<Long> tagIds;
}