package fun.timu.oj.judge.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemStatisticsDTO {
    private String problemType;
    private Integer difficulty;
    private Integer totalCount;
    private Integer activeCount;
    private Integer totalSubmissions;
    private Integer totalAccepted;
    private Double avgAcceptanceRate;
}