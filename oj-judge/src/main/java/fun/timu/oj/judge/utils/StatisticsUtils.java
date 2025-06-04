package fun.timu.oj.judge.utils;

import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计数据处理工具类
 * 提供处理统计数据的通用方法
 *
 * @author zhengke
 */
@Slf4j
public abstract class StatisticsUtils {

    /**
     * 解析语言JSON数组字符串
     */
    public static List<String> parseLanguageArray(String jsonArray) {
        try {
            if (jsonArray == null || jsonArray.isEmpty()) {
                return Collections.emptyList();
            }

            // 简单解析JSON数组字符串
            jsonArray = jsonArray.replace("[", "").replace("]", "").replace("\"", "");
            String[] languages = jsonArray.split(",\\s*");
            return Arrays.asList(languages);
        } catch (Exception e) {
            log.warn("解析语言数组失败: {}", jsonArray, e);
            return Collections.emptyList();
        }
    }

    /**
     * 初始化语言统计数据
     */
    public static Map<String, Object> initLanguageStats(String language) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("dimension_value", language);
        stats.put("dimension_name", language);
        stats.put("active_problems", 0L);
        stats.put("total_problems", 0L);
        stats.put("total_submissions", 0L);
        stats.put("total_accepted", 0L);
        stats.put("acceptance_rate", 0.0);
        stats.put("percentage", 0.0);
        return stats;
    }

    /**
     * 更新语言统计数据
     */
    public static void updateLanguageStats(Map<String, Object> stats, long activeProblems, long totalProblems,
                                           long totalSubmissions, long totalAccepted) {
        stats.put("active_problems", ((Number) stats.get("active_problems")).longValue() + activeProblems);
        stats.put("total_problems", ((Number) stats.get("total_problems")).longValue() + totalProblems);
        stats.put("total_submissions", ((Number) stats.get("total_submissions")).longValue() + totalSubmissions);
        stats.put("total_accepted", ((Number) stats.get("total_accepted")).longValue() + totalAccepted);
    }

    /**
     * 计算各语言百分比和通过率
     */
    public static void calculateLanguagePercentages(Map<String, Map<String, Object>> languageStatsMap) {
        // 计算题目总数
        long totalProblems = languageStatsMap.values().stream()
                .mapToLong(stats -> ((Number) stats.get("total_problems")).longValue())
                .sum();

        // 计算每种语言的百分比和通过率
        for (Map<String, Object> stats : languageStatsMap.values()) {
            long problems = ((Number) stats.get("total_problems")).longValue();
            long submissions = ((Number) stats.get("total_submissions")).longValue();
            long accepted = ((Number) stats.get("total_accepted")).longValue();

            // 计算百分比
            double percentage = totalProblems > 0 ? (double) problems / totalProblems * 100 : 0.0;
            stats.put("percentage", Math.round(percentage * 100) / 100.0);

            // 通过率计算
            double acceptanceRate = submissions > 0 ? (double) accepted / submissions * 100 : 0.0;
            stats.put("acceptance_rate", Math.round(acceptanceRate * 100) / 100.0);
        }
    }

    /**
     * 添加语言描述
     */
    public static Map<String, Object> addLanguageDescription(Map<String, Object> stats) {
        String language = (String) stats.get("dimension_value");
        stats.put("language_description", getLanguageDescription(language));
        return stats;
    }

    /**
     * 获取语言的友好描述
     */
    public static String getLanguageDescription(String languageCode) {
        if (languageCode == null) {
            return "未知语言";
        }

        // 根据语言代码返回友好名称
        switch (languageCode.trim().toUpperCase()) {
            case "JAVA":
                return "Java";
            case "PYTHON":
                return "Python";
            case "CPP":
                return "C++";
            case "JAVASCRIPT":
                return "JavaScript";
            case "GO":
                return "Go";
            case "CSHARP":
                return "C#";
            default:
                return languageCode;
        }
    }

    /**
     * 安全地从Map中获取Long值
     */
    public static long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("从Map中获取Long值失败: key={}, value={}", key, value, e);
            return 0L;
        }
    }

    /**
     * 添加题目类型的中文描述
     */
    public static Map<String, Object> addTypeDescription(Map<String, Object> stats) {
        String typeCode = (String) stats.get("dimension_value");
        stats.put("type_description", getProblemTypeDescription(typeCode));
        return stats;
    }

    /**
     * 获取题目类型的中文描述
     */
    public static String getProblemTypeDescription(String typeCode) {
        if (typeCode == null) {
            return "未知类型";
        }

        switch (typeCode.trim().toUpperCase()) {
            case "ALGORITHM":
                return "算法";
            case "DATABASE":
                return "数据库";
            case "SHELL":
                return "Shell";
            case "PRACTICE":
                return "练习";
            case "DEBUG":
                return "调试";
            default:
                return typeCode;
        }
    }

    /**
     * 添加难度描述
     */
    public static Map<String, Object> addDifficultyDescription(Map<String, Object> stats) {
        Integer difficulty = (Integer) stats.get("dimension_value");
        stats.put("difficulty_label", ProblemDifficultyEnum.getDescriptionByCode(difficulty));
        return stats;
    }

    /**
     * 增强题目创建趋势数据
     * TODO 后续这将会转成实体类
     * @param trendData 原始趋势数据
     * @return 增强后的趋势数据
     */
    public static Map<String, Object> enhanceProblemTrendData(Map<String, Object> trendData) {
        if (trendData == null) {
            return new HashMap<>();
        }

        // 创建新的Map，避免修改原始数据
        Map<String, Object> enhancedData = new HashMap<>(trendData);

        // 处理内存限制 - 从字节转换为MB
        if (enhancedData.containsKey("avg_memory_limit")) {
            double memoryLimitBytes = ((Number) enhancedData.get("avg_memory_limit")).doubleValue();
            double memoryLimitMB = memoryLimitBytes / (1024 * 1024);
            enhancedData.put("memory_limit_mb", Math.round(memoryLimitMB));
            enhancedData.put("memory_limit_readable", String.format("%.0f MB", memoryLimitMB));
        }

        // 处理时间限制 - 从毫秒美化显示
        if (enhancedData.containsKey("avg_time_limit")) {
            double timeLimitMs = ((Number) enhancedData.get("avg_time_limit")).doubleValue();
            if (timeLimitMs >= 1000) {
                enhancedData.put("time_limit_readable", String.format("%.1f s", timeLimitMs / 1000));
            } else {
                enhancedData.put("time_limit_readable", String.format("%.0f ms", timeLimitMs));
            }
        }

        // 计算难度分布百分比
        int totalProblems = ((Number) enhancedData.getOrDefault("problems_created", 0)).intValue();
        if (totalProblems > 0) {
            int easyProblems = ((Number) enhancedData.getOrDefault("easy_problems", 0)).intValue();
            int mediumProblems = ((Number) enhancedData.getOrDefault("medium_problems", 0)).intValue();
            int hardProblems = ((Number) enhancedData.getOrDefault("hard_problems", 0)).intValue();

            enhancedData.put("easy_percent", Math.round((double) easyProblems / totalProblems * 100));
            enhancedData.put("medium_percent", Math.round((double) mediumProblems / totalProblems * 100));
            enhancedData.put("hard_percent", Math.round((double) hardProblems / totalProblems * 100));
        }

        return enhancedData;
    }
}