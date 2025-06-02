package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 查询相似题目的请求参数
 */
@Data
public class SimilarProblemRequest {

    /**
     * 题目ID（必填）
     */
    @NotNull(message = "题目ID不能为空")
    @Positive(message = "题目ID必须为正数")
    private Long problemId;

    /**
     * 难度级别（可选）
     * 0-简单，1-中等，2-困难
     */
    @Min(value = 0, message = "难度级别最小为0")
    @Max(value = 2, message = "难度级别最大为2")
    private Integer difficulty;

    /**
     * 题目类型（可选）
     */
    @Size(max = 50, message = "题目类型长度不能超过50")
    private String problemType;

    /**
     * 返回结果数量限制（可选，默认为10）
     */
    @Positive(message = "返回数量必须为正数")
    @Max(value = 100, message = "返回数量不能超过100")
    private Integer limit = 10;
}