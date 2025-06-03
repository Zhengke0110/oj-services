package fun.timu.oj.judge.model.Enums;

/**
 * 推荐类型枚举
 * 
 * @author zhengke
 */
public enum RecommendationType {
    /**
     * 基于通过率的推荐
     */
    ACCEPTANCE_RATE,
    
    /**
     * 基于相似性的推荐
     */
    SIMILARITY,
    
    /**
     * 基于热度的推荐
     */
    POPULARITY,
    
    /**
     * 基于算法数据的推荐
     */
    ALGORITHM_DATA
}
