package fun.timu.oj.judge.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签使用统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagUsageStatisticsDTO {
    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签英文名称
     */
    private String tagNameEn;

    /**
     * 标签颜色
     */
    private String tagColor;

    /**
     * 标签分类
     */
    private String category;

    /**
     * 分类显示名称
     */
    private String categoryDisplayName;

    /**
     * 记录的使用次数
     */
    private Long storedUsageCount;

    /**
     * 实际使用次数（通过关联表计算）
     */
    private Long actualUsageCount;

    /**
     * 使用比例（针对同分类）
     */
    private Double usagePercentage;

    /**
     * 标签状态
     */
    private Integer status;

    /**
     * 标签描述
     */
    private String description;
}