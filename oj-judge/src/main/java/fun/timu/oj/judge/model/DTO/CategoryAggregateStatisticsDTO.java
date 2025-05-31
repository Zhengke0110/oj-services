package fun.timu.oj.judge.model.DTO;

import lombok.Data;

@Data
public class CategoryAggregateStatisticsDTO {
    private String category;
    private Long totalTags;
    private Long storedUsageCount;
    private Long actualUsageCount;
    private Long activeTags;
}