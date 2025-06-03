package fun.timu.oj.judge.model.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationStatisticsReport {
    private long totalRelations;
    private long totalProblems;
    private long totalTags;
    private long problemsWithoutTags;
    private long tagsWithoutProblems;
    private double averageTagsPerProblem;
    private double averageProblemsPerTag;
    private LocalDateTime reportTime;
}
