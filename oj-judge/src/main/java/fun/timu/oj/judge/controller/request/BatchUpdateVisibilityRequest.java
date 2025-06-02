package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchUpdateVisibilityRequest {
    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> problemIds;

    @NotNull(message = "可见性不能为空")
    private Integer visibility;
}