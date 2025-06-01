package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Data
public class ProblemQueryRequest {
    @Min(value = 1, message = "当前页码不能小于1")
    private Integer current = 1;

    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 20;

    // 题目类型：ALGORITHM(算法)，PRACTICE(编程练习)，DEBUG(代码调试)
    private String problemType;

    // 难度级别：0-简单，1-中等，2-困难
    private Integer difficulty;

    // 状态：0-禁用，1-启用，2-草稿
    private Integer status;

    // 题目可见性枚举：0-私有，1-公开，2-仅注册用户
    private Integer visibility;

    // 支持的编程语言列表
    private List<String> supportedLanguages;

    // 是否需要输入数据
    private Boolean hasInput;

    // 最小通过率
    private Double minAcceptanceRate;

    // 最大通过率
    private Double maxAcceptanceRate;
}