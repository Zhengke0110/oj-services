package fun.timu.oj.judge.controller.request;

import fun.timu.oj.judge.model.Enums.RecommendationType;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 统一推荐题目请求
 * 
 * @author zhengke
 */
@Data
public class UnifiedRecommendationRequest {
    
    /**
     * 推荐类型
     */
    @NotNull(message = "推荐类型不能为空")
    private RecommendationType type;
    
    /**
     * 最小通过率（用于ACCEPTANCE_RATE类型）
     */
    @DecimalMin(value = "0.0", message = "最小通过率不能小于0.0")
    @DecimalMax(value = "1.0", message = "最小通过率不能大于1.0")
    private Double minAcceptanceRate;
    
    /**
     * 最大通过率（用于ACCEPTANCE_RATE类型）
     */
    @DecimalMin(value = "0.0", message = "最大通过率不能小于0.0")
    @DecimalMax(value = "1.0", message = "最大通过率不能大于1.0")
    private Double maxAcceptanceRate;
    
    /**
     * 难度限制
     */
    @Min(value = 1, message = "难度必须大于0")
    @Max(value = 5, message = "难度不能大于5")
    private Integer difficulty;
    
    /**
     * 题目类型
     */
    private String problemType;
    
    /**
     * 基础题目ID（用于SIMILARITY类型）
     */
    @Positive(message = "基础题目ID必须为正数")
    private Long baseProblemId;
    
    /**
     * 时间范围（天数，用于POPULARITY类型）
     */
    @Positive(message = "时间范围必须为正数")
    private Integer timeRange;
    
    /**
     * 返回数量限制
     */
    @Min(value = 1, message = "数量限制必须大于0")
    @Max(value = 100, message = "数量限制不能大于100")
    private Integer limit = 10;
    
    /**
     * 是否需要评分信息
     */
    private Boolean includeScore = false;
}
