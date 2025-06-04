package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 题目排名请求对象
 *
 * @author zhengke
 */
@Data
public class ProblemRankingRequest {

    /**
     * 排行榜类型
     */
    @NotBlank(message = "排行榜类型不能为空")
    private String rankingType;

    /**
     * 排名条件
     */
    @NotNull(message = "排名条件不能为空")
    private Map<String, Object> criteria;
}
