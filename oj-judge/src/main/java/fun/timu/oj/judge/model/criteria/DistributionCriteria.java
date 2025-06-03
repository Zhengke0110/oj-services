package fun.timu.oj.judge.model.criteria;


import fun.timu.oj.judge.model.Enums.DistributionDimension;
import lombok.*;

import java.util.Date;

/**
 * 分布分析条件
 * 
 * @author zhengke
 * @since 2025-06-03
 */
@Data
@Builder
@AllArgsConstructor
@ToString
public class DistributionCriteria {
    
    /**
     * 分布维度
     */
    private DistributionDimension dimension;
    
    /**
     * 时间范围（天数）- 用于创建者活跃度等时间相关统计
     */
    private Integer timeRange;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
    
    /**
     * 是否包含详细统计信息
     */
    private Boolean includeDetails = true;
    
    /**
     * 是否只返回有数据的维度
     */
    private Boolean onlyNonEmpty = false;
    
    /**
     * 排序字段（count, percentage, name等）
     */
    private String orderBy = "count";
    
    /**
     * 排序方向（ASC, DESC）
     */
    private String orderDirection = "DESC";
    
    /**
     * 限制返回数量
     */
    private Integer limit;
    
    /**
     * 分桶大小（用于通过率、时间限制等连续值的分布统计）
     */
    private Double bucketSize;
    
    // 构造器
    public DistributionCriteria() {}
    
    public DistributionCriteria(DistributionDimension dimension) {
        this.dimension = dimension;
    }
    
    // 静态构建方法
    public static DistributionCriteria forDimension(DistributionDimension dimension) {
        return new DistributionCriteria(dimension);
    }
    
    public static DistributionCriteria forDifficulty() {
        return new DistributionCriteria(DistributionDimension.DIFFICULTY);
    }
    
    public static DistributionCriteria forType() {
        return new DistributionCriteria(DistributionDimension.TYPE);
    }
    
    public static DistributionCriteria forLanguage() {
        return new DistributionCriteria(DistributionDimension.LANGUAGE);
    }
    
    public static DistributionCriteria forStatus() {
        return new DistributionCriteria(DistributionDimension.STATUS);
    }
    
    public static DistributionCriteria forCreator(Integer timeRange) {
        DistributionCriteria criteria = new DistributionCriteria(DistributionDimension.CREATOR);
        criteria.setTimeRange(timeRange);
        return criteria;
    }

    // Builder模式
    public DistributionCriteria timeRange(Integer timeRange) {
        this.timeRange = timeRange;
        return this;
    }

    public DistributionCriteria startTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public DistributionCriteria endTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    public DistributionCriteria includeDetails(Boolean includeDetails) {
        this.includeDetails = includeDetails;
        return this;
    }

    public DistributionCriteria onlyNonEmpty(Boolean onlyNonEmpty) {
        this.onlyNonEmpty = onlyNonEmpty;
        return this;
    }

    public DistributionCriteria orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public DistributionCriteria orderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
        return this;
    }

    public DistributionCriteria limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public DistributionCriteria bucketSize(Double bucketSize) {
        this.bucketSize = bucketSize;
        return this;
    }
}
