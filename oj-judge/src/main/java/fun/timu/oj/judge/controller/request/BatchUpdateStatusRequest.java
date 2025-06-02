package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchUpdateStatusRequest {
    /**
     * 题目ID列表
     */
    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> problemIds;

    /**
     * 要更新的状态值
     */
    @NotNull(message = "状态值不能为空")
    @Min(value = 0, message = "最小值不能小于0")
    @Max(value = 2, message = "最大值不能超过2")
    private Integer status;
}