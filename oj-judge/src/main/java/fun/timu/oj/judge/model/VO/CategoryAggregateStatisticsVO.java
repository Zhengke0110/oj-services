package fun.timu.oj.judge.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAggregateStatisticsVO {
    /**
     * 标签分类
     */
    private String category;

    /**
     * 分类显示名称
     */
    private String categoryDisplayName;

    /**
     * 该分类下的标签总数
     */
    private Long totalTags;

    /**
     * 存储的使用次数总和
     */
    private Long storedUsageCount;

    /**
     * 实际使用次数（通过关联表计算）
     */
    private Long actualUsageCount;

    /**
     * 活跃标签数量
     */
    private Long activeTags;

    /**
     * 使用率（活跃标签/总标签）
     */
    private Double activeRate;

    /**
     * 平均每个标签的使用次数
     */
    private Double averageUsage;
}