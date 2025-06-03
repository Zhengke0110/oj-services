package fun.timu.oj.judge.model.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagStatistics {
    private Long tagId;
    private String tagName;
    private Long problemCount;
    private Double score;
}
