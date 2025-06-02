package fun.timu.oj.judge.model.VTO;

import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目统计信息（面向视图对象）
 * 屏蔽敏感数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemStatisticsVTO {
    // 非敏感字段，直接保留
    private String problemType;
    private Integer difficulty;
    private Integer totalCount;
    private Integer activeCount;
    private Double avgAcceptanceRate;

    // 敏感字段，转换为描述性文本
    private String activityLevel; // 替代 totalSubmissions
    private String popularityStatus; // 题目受欢迎程度的综合描述

    /**
     * 将DTO转换为VTO，屏蔽敏感信息
     *
     * @param dto 数据传输对象
     * @return 视图传输对象
     */
    public static ProblemStatisticsVTO fromDTO(ProblemStatisticsDTO dto) {
        if (dto == null) {
            return null;
        }

        // 计算活跃度级别，替代具体的提交次数
        String activityLevel = calculateActivityLevel(dto.getTotalSubmissions());

        // 计算题目受欢迎程度，综合考虑提交次数和通过率
        String popularityStatus = calculatePopularityStatus(
                dto.getTotalSubmissions(),
                dto.getAvgAcceptanceRate());

        return ProblemStatisticsVTO.builder()
                .problemType(dto.getProblemType())
                .difficulty(dto.getDifficulty())
                .totalCount(dto.getTotalCount())
                .activeCount(dto.getActiveCount())
                .avgAcceptanceRate(dto.getAvgAcceptanceRate())
                .activityLevel(activityLevel)
                .popularityStatus(popularityStatus)
                .build();
    }

    /**
     * 根据提交次数计算活跃度级别
     *
     * @param totalSubmissions 总提交次数
     * @return 活跃度级别描述
     */
    private static String calculateActivityLevel(Integer totalSubmissions) {
        if (totalSubmissions == null) {
            return "未知";
        }

        if (totalSubmissions >= 10000) {
            return "非常活跃";
        } else if (totalSubmissions >= 5000) {
            return "活跃";
        } else if (totalSubmissions >= 1000) {
            return "较活跃";
        } else if (totalSubmissions >= 100) {
            return "一般";
        } else {
            return "较少活跃";
        }
    }

    /**
     * 根据提交次数和通过率计算题目受欢迎程度
     *
     * @param totalSubmissions 总提交次数
     * @param acceptanceRate   通过率
     * @return 受欢迎程度描述
     */
    private static String calculatePopularityStatus(Integer totalSubmissions, Double acceptanceRate) {
        if (totalSubmissions == null || acceptanceRate == null) {
            return "未知";
        }

        // 先根据提交次数判断基础受欢迎程度
        String basePopularity;
        if (totalSubmissions >= 5000) {
            basePopularity = "热门";
        } else if (totalSubmissions >= 1000) {
            basePopularity = "受欢迎";
        } else if (totalSubmissions >= 100) {
            basePopularity = "一般";
        } else {
            basePopularity = "冷门";
        }

        // 再根据通过率给出评价
        String difficultyLevel;
        if (acceptanceRate >= 0.7) {
            difficultyLevel = "易于掌握";
        } else if (acceptanceRate >= 0.4) {
            difficultyLevel = "适中难度";
        } else {
            difficultyLevel = "具有挑战性";
        }

        return basePopularity + "，" + difficultyLevel;
    }
}