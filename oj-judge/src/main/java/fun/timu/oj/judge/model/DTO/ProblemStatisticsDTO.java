package fun.timu.oj.judge.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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

    /**
     * 将 Map 转换为 ProblemStatisticsDTO 对象
     *
     * @param map 包含统计数据的 Map
     * @return 转换后的 ProblemStatisticsDTO 对象
     */
    public static ProblemStatisticsDTO fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return ProblemStatisticsDTO.builder()
                .problemType((String) map.get("problem_type"))
                .difficulty(convertToInteger(map.get("difficulty")))
                .totalCount(convertToInteger(map.get("total_count")))
                .activeCount(convertToInteger(map.get("active_count")))
                .totalSubmissions(convertToInteger(map.get("total_submissions")))
                .totalAccepted(convertToInteger(map.get("total_accepted")))
                .avgAcceptanceRate(convertToDouble(map.get("avg_acceptance_rate")))
                .build();
    }

    /**
     * 将对象转换为 Integer 类型
     *
     * @param value 需要转换的对象
     * @return 转换后的 Integer 值，转换失败则返回0
     */
    private static Integer convertToInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 将对象转换为 Double 类型
     *
     * @param value 需要转换的对象
     * @return 转换后的 Double 值，转换失败则返回0.0
     */
    private static Double convertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}