package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 热门题目查询请求
 */
@Data
public class HotProblemRequest {
    /**
     * 题目类型
     */
    @Size(max = 50, message = "题目类型长度不能超过50个字符")
    private String problemType;

    /**
     * 题目难度
     * 0-简单，1-中等，2-困难
     */
    @Min(value = 0, message = "题目难度最小为1")
    @Max(value = 2, message = "题目难度最大为3")
    private Integer difficulty;

    /**
     * 返回数量限制，默认为10
     */
    @Min(value = 1, message = "返回数量最小为1")
    @Max(value = 100, message = "返回数量最大为100")
    private Integer limit;
}