package fun.timu.oj.judge.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionTrendDTO {
    private String month;            // 月份，格式为YYYY-MM
    private Integer submissionCount; // 提交次数

    // 从Map转换为DTO的工厂方法
    public static SubmissionTrendDTO fromMap(java.util.Map<String, Object> map) {
        if (map == null) return null;

        return SubmissionTrendDTO.builder()
                .month((String) map.get("month"))
                .submissionCount(convertToInteger(map.get("submission_count")))
                .build();
    }

    // 辅助转换方法
    private static Integer convertToInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}