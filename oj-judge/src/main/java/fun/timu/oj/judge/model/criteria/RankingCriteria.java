package fun.timu.oj.judge.model.criteria;

import fun.timu.oj.judge.model.Enums.RankingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排行榜查询条件
 *
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingCriteria {

    /**
     * 排行榜类型
     */
    private RankingType type;

    /**
     * 限制数量
     */
    private Integer limit;

    /**
     * 时间范围（天数）
     */
    private Integer timeRange;

    /**
     * 天数范围（用于最新题目排行）
     */
    private Integer dayRange;

    /**
     * 最小提交数要求（用于最难题目排行）
     */
    private Integer minSubmissions;

    /**
     * 是否包含质量评分
     */
    private Boolean includeQualityScore;

    /**
     * 是否包含创建者信息
     */
    private Boolean includeCreatorInfo;

    /**
     * 创建热门题目排行条件
     */
    public static RankingCriteria forPopularity(Integer limit, Integer timeRange) {
        return RankingCriteria.builder()
                .type(RankingType.POPULARITY)
                .limit(limit)
                .timeRange(timeRange)
                .build();
    }

    /**
     * 创建最难题目排行条件
     */
    public static RankingCriteria forHardest(Integer limit, Integer minSubmissions) {
        return RankingCriteria.builder()
                .type(RankingType.HARDEST)
                .limit(limit)
                .minSubmissions(minSubmissions)
                .build();
    }

    /**
     * 创建高质量题目排行条件
     */
    public static RankingCriteria forQuality(Integer limit) {
        return RankingCriteria.builder()
                .type(RankingType.QUALITY)
                .limit(limit)
                .includeQualityScore(true)
                .build();
    }

    /**
     * 创建最新题目排行条件
     */
    public static RankingCriteria forNewest(Integer limit, Integer dayRange) {
        return RankingCriteria.builder()
                .type(RankingType.NEWEST)
                .limit(limit)
                .dayRange(dayRange)
                .build();
    }

    /**
     * 创建零提交题目排行条件
     */
    public static RankingCriteria forZeroSubmission(Integer limit) {
        return RankingCriteria.builder()
                .type(RankingType.ZERO_SUBMISSION)
                .limit(limit)
                .build();
    }

    /**
     * 创建创建者贡献排行条件
     */
    public static RankingCriteria forCreatorContribution(Integer limit, Integer timeRange) {
        return RankingCriteria.builder()
                .type(RankingType.CREATOR_CONTRIBUTION)
                .limit(limit)
                .timeRange(timeRange)
                .includeCreatorInfo(true)
                .build();
    }

    /**
     * 根据排行榜类型获取预设的排行榜条件（扩展版）
     *
     * @param type           排行榜类型
     * @param limit          结果数量限制
     * @param timeRange      时间范围（可选）
     * @param dayRange       天数范围（可选）
     * @param minSubmissions 最小提交次数（可选）
     * @return 对应类型的排行榜条件
     */
    public static RankingCriteria fromRankingType(RankingType type, Integer limit,
                                                  Integer timeRange, Integer dayRange, Integer minSubmissions) {
        if (type == null) {
            throw new IllegalArgumentException("排行榜类型不能为空");
        }

        RankingCriteria criteria;

        switch (type) {
            case POPULARITY:
                criteria = forPopularity(limit, timeRange);
                if (timeRange != null) {
                    criteria.setTimeRange(timeRange);
                }
                break;
            case HARDEST:
                criteria = forHardest(limit, minSubmissions);
                if (minSubmissions != null) {
                    criteria.setMinSubmissions(minSubmissions);
                }
                break;
            case QUALITY:
                criteria = forQuality(limit);
                break;
            case NEWEST:
                criteria = forNewest(limit, dayRange);
                if (dayRange != null) {
                    criteria.setDayRange(dayRange);
                }
                break;
            case ZERO_SUBMISSION:
                criteria = forZeroSubmission(limit);
                break;
            case CREATOR_CONTRIBUTION:
                criteria = forCreatorContribution(limit, timeRange);
                if (timeRange != null) {
                    criteria.setTimeRange(timeRange);
                }
                break;
            default:
                throw new IllegalArgumentException("不支持的排行榜类型: " + type);
        }

        return criteria;
    }

}
