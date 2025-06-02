package fun.timu.oj.judge.model.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class ProblemDetailStatisticsDTO {
    // 题目难度统计
    private Integer easyCount;
    private Integer mediumCount;
    private Integer hardCount;

    // 题目类型统计
    private Integer algorithmCount;
    private Integer practiceCount;
    private Integer debugCount;

    // 提交情况统计
    private Integer totalSubmissions;
    private Integer totalAccepted;
    private Double avgAcceptanceRate;
    private Integer maxSubmissions;
    private Integer minSubmissions;

    // 总计
    private Integer totalProblems;

    /**
     * 将 Map 转换为 DTO 对象
     *
     * @param map 包含统计数据的 Map
     * @return 转换后的 DTO 对象
     */
    public static ProblemDetailStatisticsDTO fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        ProblemDetailStatisticsDTO dto = new ProblemDetailStatisticsDTO();

        // 设置题目难度统计
        dto.setEasyCount(getIntValue(map, "easy_count"));
        dto.setMediumCount(getIntValue(map, "medium_count"));
        dto.setHardCount(getIntValue(map, "hard_count"));

        // 设置题目类型统计
        dto.setAlgorithmCount(getIntValue(map, "algorithm_count"));
        dto.setPracticeCount(getIntValue(map, "practice_count"));
        dto.setDebugCount(getIntValue(map, "debug_count"));

        // 设置提交情况统计
        dto.setTotalSubmissions(getIntValue(map, "total_submissions"));
        dto.setTotalAccepted(getIntValue(map, "total_accepted"));
        dto.setAvgAcceptanceRate(getDoubleValue(map, "avg_acceptance_rate"));
        dto.setMaxSubmissions(getIntValue(map, "max_submissions"));
        dto.setMinSubmissions(getIntValue(map, "min_submissions"));

        // 设置总计
        dto.setTotalProblems(getIntValue(map, "total_problems"));

        return dto;
    }

    /**
     * 安全地从 Map 中获取 Integer 值
     */
    private static Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 安全地从 Map 中获取 Double 值
     */
    private static Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}