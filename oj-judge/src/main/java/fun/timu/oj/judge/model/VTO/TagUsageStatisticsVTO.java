package fun.timu.oj.judge.model.VTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagUsageStatisticsVTO {
    private Long id;
    private String tagName;
    private String tagNameEn;
    private String tagColor;
    private String category;
    private String categoryDisplayName;
    private Long storedUsageCount;
    private Long actualUsageCount;
    private Double usagePercentage;
    private Integer status;
    private String description;
}