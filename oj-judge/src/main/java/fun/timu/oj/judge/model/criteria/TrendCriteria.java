package fun.timu.oj.judge.model.criteria;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 趋势分析条件类
 * 统一所有时间趋势分析的查询条件
 *
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendCriteria {

    /**
     * 趋势分析类型
     */
    private TrendType type;

    /**
     * 时间粒度（DAY/WEEK/MONTH/YEAR）
     */
    private String timeGranularity;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 题目ID（可选，用于特定题目的趋势分析）
     */
    private Long problemId;

    /**
     * 用户ID（可选，用于特定用户的趋势分析）
     */
    private Long userId;

    /**
     * 数据限制条数（可选）
     */
    private Integer limit;
}
