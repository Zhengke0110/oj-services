package fun.timu.oj.judge.controller.request;

import lombok.Data;

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
    private Integer status;
}