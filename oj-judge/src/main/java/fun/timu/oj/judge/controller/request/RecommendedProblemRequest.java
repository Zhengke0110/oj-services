package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

@Data
public class RecommendedProblemRequest {

    @DecimalMin(value = "0.0", message = "最小通过率不能小于0.0")
    @DecimalMax(value = "1.0", message = "最小通过率不能大于1.0")
    private Double minAcceptanceRate;

    @DecimalMin(value = "0.0", message = "最大通过率不能小于0.0")
    @DecimalMax(value = "1.0", message = "最大通过率不能大于1.0")
    private Double maxAcceptanceRate;

    private Integer difficulty;

    @Min(value = 1, message = "数量限制必须大于0")
    private Integer limit;
}