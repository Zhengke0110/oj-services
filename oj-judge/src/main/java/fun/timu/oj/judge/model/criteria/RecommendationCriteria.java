package fun.timu.oj.judge.model.criteria;

import fun.timu.oj.judge.model.Enums.RecommendationType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 推荐条件类
 * 统一所有推荐算法的查询条件
 *
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationCriteria {

    /**
     * 推荐类型
     */
    private RecommendationType type;

    /**
     * 最小通过率（用于ACCEPTANCE_RATE类型）
     */
    private Double minAcceptanceRate;

    /**
     * 最大通过率（用于ACCEPTANCE_RATE类型）
     */
    private Double maxAcceptanceRate;

    /**
     * 难度限制
     */
    private Integer difficulty;

    /**
     * 题目类型
     */
    private String problemType;

    /**
     * 基础题目ID（用于SIMILARITY类型）
     */
    private Long baseProblemId;

    /**
     * 时间范围（天数，用于POPULARITY类型）
     */
    private Integer timeRange;

    /**
     * 返回数量限制
     */
    private Integer limit;

    /**
     * 题目标签列表
     */
    private List<String> tags;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 可见性（0-私有，1-公开）
     */
    private Integer visibility;

    /**
     * 状态（0-草稿，1-发布）
     */
    private Integer status;

    /**
     * 创建基于通过率的推荐条件
     */
    public static RecommendationCriteria forAcceptanceRate(Double minRate, Double maxRate, Integer difficulty, Integer limit) {
        return RecommendationCriteria.builder()
                .type(RecommendationType.ACCEPTANCE_RATE)
                .minAcceptanceRate(minRate)
                .maxAcceptanceRate(maxRate)
                .difficulty(difficulty)
                .limit(limit)
                .build();
    }

    /**
     * 创建基于相似性的推荐条件
     */
    public static RecommendationCriteria forSimilarity(Long baseProblemId, Integer difficulty, String problemType, Integer limit) {
        return RecommendationCriteria.builder()
                .type(RecommendationType.SIMILARITY)
                .baseProblemId(baseProblemId)
                .difficulty(difficulty)
                .problemType(problemType)
                .limit(limit)
                .build();
    }

    /**
     * 创建基于热度的推荐条件
     */
    public static RecommendationCriteria forPopularity(Integer limit, Integer timeRange) {
        return RecommendationCriteria.builder()
                .type(RecommendationType.POPULARITY)
                .limit(limit)
                .timeRange(timeRange)
                .build();
    }

    /**
     * 创建基于算法数据的推荐条件
     */
    public static RecommendationCriteria forAlgorithmData(Integer difficulty, String problemType, Integer limit) {
        return RecommendationCriteria.builder()
                .type(RecommendationType.ALGORITHM_DATA)
                .difficulty(difficulty)
                .problemType(problemType)
                .limit(limit)
                .build();
    }
}