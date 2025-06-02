package fun.timu.oj.judge.model.criteria;

/**
 * 趋势分析类型枚举
 *
 * @author zhengke
 */
public enum TrendType {
    
    /**
     * 题目创建趋势
     */
    PROBLEM_CREATION,
    
    /**
     * 提交趋势分析
     */
    SUBMISSION_TREND,
    
    /**
     * 通过率趋势分析
     */
    ACCEPTANCE_RATE_TREND,
    
    /**
     * 题目活跃度时间趋势
     */
    PROBLEM_ACTIVITY_TREND
}
