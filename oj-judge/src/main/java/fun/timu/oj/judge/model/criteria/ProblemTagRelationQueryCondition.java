package fun.timu.oj.judge.model.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemTagRelationQueryCondition {
    private List<Long> problemIds;
    private List<Long> tagIds;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean isDeleted;
    private String orderBy;
    private String orderDirection;
}
