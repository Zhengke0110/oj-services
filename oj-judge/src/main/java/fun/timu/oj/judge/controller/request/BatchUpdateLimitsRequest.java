package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class BatchUpdateLimitsRequest {

    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> problemIds;

    @Positive(message = "时间限制必须为正数")
    private Integer timeLimit;

    @Positive(message = "内存限制必须为正数")
    private Integer memoryLimit;
}