package fun.timu.oj.judge.model.VTO;

import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目详细统计信息（面向视图对象）
 * 屏蔽敏感数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailStatisticsVTO {
    // 题目难度统计
    private Integer easyCount;
    private Integer mediumCount;
    private Integer hardCount;

    // 题目类型统计
    private Integer algorithmCount;
    private Integer practiceCount;
    private Integer debugCount;

    // 提交情况统计 - 屏蔽具体数值
    private String submissionLevel; // 总提交量级别描述
    private Double avgAcceptanceRate; // 保留平均通过率
    private String submissionRange; // 提交次数范围描述

    // 总计
    private Integer totalProblems;

    /**
     * 将DTO转换为VTO，屏蔽敏感信息
     *
     * @param dto 数据传输对象
     * @return 视图传输对象
     */
    public static ProblemDetailStatisticsVTO fromDTO(ProblemDetailStatisticsDTO dto) {
        if (dto == null) {
            return null;
        }

        // 生成提交量级别描述，替代具体提交次数
        String submissionLevel = calculateSubmissionLevel(dto.getTotalSubmissions());

        // 生成提交次数范围描述
        String submissionRange = calculateSubmissionRange(dto.getMinSubmissions(), dto.getMaxSubmissions());

        return ProblemDetailStatisticsVTO.builder()
                .easyCount(dto.getEasyCount())
                .mediumCount(dto.getMediumCount())
                .hardCount(dto.getHardCount())
                .algorithmCount(dto.getAlgorithmCount())
                .practiceCount(dto.getPracticeCount())
                .debugCount(dto.getDebugCount())
                .submissionLevel(submissionLevel)
                .avgAcceptanceRate(dto.getAvgAcceptanceRate())
                .submissionRange(submissionRange)
                .totalProblems(dto.getTotalProblems())
                .build();
    }

    /**
     * 根据总提交次数计算提交量级别描述
     *
     * @param totalSubmissions 总提交次数
     * @return 提交量级别描述
     */
    private static String calculateSubmissionLevel(Integer totalSubmissions) {
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
            return "较少";
        }
    }

    /**
     * 根据最小和最大提交次数计算提交次数范围描述
     *
     * @param minSubmissions 最小提交次数
     * @param maxSubmissions 最大提交次数
     * @return 提交次数范围描述
     */
    private static String calculateSubmissionRange(Integer minSubmissions, Integer maxSubmissions) {
        if (minSubmissions == null || maxSubmissions == null) {
            return "未知范围";
        }

        // 使用区间描述而非具体数值
        if (maxSubmissions < 10) {
            return "极少提交";
        } else if (maxSubmissions < 50) {
            return "较少提交";
        } else if (maxSubmissions < 200) {
            return "中等提交量";
        } else if (maxSubmissions < 1000) {
            return "较多提交";
        } else {
            return "大量提交";
        }
    }
}