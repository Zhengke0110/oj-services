package fun.timu.oj.judge.service.impl.Problem;

import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.Enums.RankingType;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.service.Problem.ProblemRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemRankingServiceImpl implements ProblemRankingService {
    private final ProblemManager problemManager;

    /**
     * 获取热门题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 热门题目排行榜
     */
    @Override
    public List<Map<String, Object>> getPopularProblemsRanking(Integer limit, Integer timeRange) {
        try {
            log.info("ProblemRankingService--->获取热门题目排行榜, 限制数量: {}, 时间范围: {}", limit, timeRange);

            // 构建排行榜条件
            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.POPULARITY)
                    .limit(limit != null ? limit : 10)
                    .timeRange(timeRange)
                    .build();

            // TODO 多表联查优化：在ProblemStatisticsManager中新增getPopularProblemsWithDetails()方法
            // TODO 通过LEFT JOIN problem_tag_relation、problem_tag、user 表获取热门题目的完整信息
            // TODO 包括题目标签、创建者信息等，为排行榜提供更丰富的展示数据
            List<Map<String, Object>> result = problemManager.getProblemRanking(criteria);

            log.info("ProblemRankingService--->获取热门题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取热门题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取热门题目排行榜失败", e);
        }
    }

    /**
     * 获取高质量题目排行榜
     * <p>
     * 基于多维度指标（如通过率、评分、提交次数等）获取质量最高的题目列表
     *
     * @param limit 限制返回数量，默认为10
     * @return 高质量题目排行榜数据
     */
    @Override
    public List<Map<String, Object>> getHighQualityProblemsRanking(Integer limit) {
        try {
            log.info("ProblemRankingService--->获取高质量题目排行榜, 限制数量: {}", limit);

            // 构建查询条件，使用QUALITY类型
            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.QUALITY)
                    .limit(limit != null ? limit : 10)
                    .build();
            // 调用通用排行榜方法获取结果
            List<Map<String, Object>> result = problemManager.getProblemRanking(criteria);
            log.info("ProblemRankingService--->成功获取高质量题目排行榜, 获取数量: {}", result.size());

            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取高质量题目排行榜失败", e);
            throw new RuntimeException("获取高质量题目排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取最难题目排行榜
     *
     * @param limit 限制数量
     * @return 最难题目排行榜
     */
    @Override
    public List<Map<String, Object>> getHardestProblemsRanking(Integer limit) {
        try {
            log.info("ProblemRankingService--->获取最难题目排行榜, 限制数量: {}", limit);
            // TODO 多表联查优化：在ProblemStatisticsManager中新增getHardestProblemsWithAnalysis()方法
            // TODO 通过JOIN submission 表统计题目的真实难度数据（通过率、平均提交次数等）
            // TODO 通过JOIN problem_tag_relation 和 problem_tag 表获取题目标签分布
            // TODO 调用ProblemTagRelationManager.getTagNamesByProblemIds()获取标签信息
            List<Map<String, Object>> result = problemManager.getHardestProblemsRanking(limit);
            log.info("ProblemRankingService--->获取最难题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取最难题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取最难题目排行榜失败", e);
        }
    }

    /**
     * 获取最容易题目排行榜
     *
     * @param limit 限制数量
     * @return 最容易题目排行榜
     */
    @Override
    public List<Map<String, Object>> getEasiestProblemsRanking(Integer limit) {
        try {
            log.info("ProblemRankingService--->获取最简单题目排行榜, 限制数量: {}", limit);
            // TODO 多表联查优化：在ProblemStatisticsManager中新增getEasiestProblemsWithRecommendation()方法
            // TODO 通过JOIN submission 表获取题目的通过率和用户反馈数据
            // TODO 通过JOIN problem_tag_relation 和 problem_tag 表分析简单题目的标签特征
            // TODO 调用ProblemTagRelationManager.getTagNamesByProblemIds()为初学者推荐合适的学习路径
            List<Map<String, Object>> result = problemManager.getEasiestProblemsRanking(limit);
            log.info("ProblemRankingService--->获取最简单题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取最简单题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取最简单题目排行榜失败", e);
        }
    }

    /**
     * 获取最常提交题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 最常提交题目排行榜
     */
    @Override
    public List<Map<String, Object>> getMostSubmittedProblemsRanking(Integer limit, Integer timeRange) {
        try {
            log.info("ProblemRankingService--->获取提交最多题目排行榜, 限制数量: {}, 时间范围: {}", limit, timeRange);
            // TODO 多表联查优化：在ProblemStatisticsManager中新增getMostSubmittedProblemsWithTrend()方法
            // TODO 通过JOIN submission 表按时间范围统计提交趋势和用户参与度
            // TODO 通过JOIN problem_tag_relation 和 problem_tag 表分析热门题目的标签分布
            // TODO 通过JOIN user 表获取提交用户的统计信息，分析题目受欢迎程度
            List<Map<String, Object>> result = problemManager.getMaxSubmissionProblemsRanking(limit, timeRange);
            log.info("ProblemRankingService--->获取提交最多题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取提交最多题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取提交最多题目排行榜失败", e);
        }
    }

    /**
     * 获取提交零提交题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 提交最多题目排行榜
     */
    @Override
    public List<Map<String, Object>> getZeroSubmittedProblemsRanking(Integer limit, Integer timeRange) {
        try {
            log.info("ProblemRankingService--->获取零提交题目排行榜, 限制数量: {}, 时间范围: {}", limit, timeRange);
            // TODO 多表联查优化：在ProblemStatisticsManager中新增getZeroSubmissionProblemsWithAnalysis()方法
            // TODO 通过LEFT JOIN submission 表确认零提交状态
            // TODO 通过JOIN problem_tag_relation 和 problem_tag 表分析零提交题目的标签特征
            // TODO 通过JOIN user 表获取创建者信息，分析题目质量和推广情况
            // TODO 为题目运营提供改进建议和推广策略
            List<Map<String, Object>> result = problemManager.getZeroSubmissionProblemsRanking(limit, timeRange);
            log.info("ProblemRankingService--->获取零提交题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取零提交题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取零提交题目排行榜失败", e);
        }
    }

    /**
     * 获取最近热门题目排行榜
     *
     * @param limit 限制数量
     * @param days  最近天数
     * @return 最近热门题目排行榜
     */
    @Override
    public List<Map<String, Object>> getRecentPopularProblemsRanking(Integer limit, Integer days) {
        try {
            log.info("ProblemRankingService--->获取最近热门题目排行榜, 限制数量: {}, 最近天数: {}", limit, days);

            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.POPULARITY)
                    .limit(limit != null ? limit : 10)
                    .dayRange(days)
                    .build();

            // TODO 多表联查优化：在ProblemStatisticsManager中新增getRecentPopularProblemsWithTrend()方法
            // TODO 通过JOIN submission 表按日期范围统计最近的提交活跃度
            // TODO 通过JOIN problem_tag_relation 和 problem_tag 表分析最近热门题目的标签趋势
            // TODO 通过JOIN user 表分析参与用户的活跃度和分布情况
            // TODO 为推荐系统提供实时热门题目数据支持
            List<Map<String, Object>> result = problemManager.getProblemRanking(criteria);

            log.info("ProblemRankingService--->获取最近热门题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取最近热门题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取最近热门题目排行榜失败", e);
        }
    }

    /**
     * 统一的排行榜接口
     *
     * @param type  排排行榜类型
     * @param limit 限制数量
     * @return 排行榜数据列表
     */
    @Override
    public List<Map<String, Object>> getProblemRanking(RankingType type, Integer limit) {
        try {
            log.info("ProblemRankingService--->获取题目排行榜, 类型: {}", type);

            // 构建排行榜条件
            RankingCriteria criteria = RankingCriteria.fromRankingType(type, limit, null, null, null);

            // TODO 多表联查优化：在ProblemStatisticsManager中新增getUnifiedRankingWithDetails()方法
            // TODO 根据不同排行榜类型，动态JOIN相关表获取完整信息
            // TODO 对于热门排行榜：JOIN submission、user 表获取提交统计和用户参与数据
            // TODO 对于难度排行榜：JOIN submission 表计算真实通过率和用户反馈
            // TODO 统一调用ProblemTagRelationManager.getTagNamesByProblemIds()获取标签信息
            // 调用管理器方法获取排行榜数据
            List<Map<String, Object>> result = problemManager.getProblemRanking(criteria);

            log.info("ProblemRankingService--->获取题目排行榜成功, 数量: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目排行榜失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取题目排名
     *
     * @param rankingType 排名类型
     * @param criteria    排名条件
     * @return 题目排名结果
     */
    @Override
    public List<Map<String, Object>> getProblemRanking(String rankingType, Map<String, Object> criteria) {
        try {
            log.info("ProblemRankingService--->获取题目排名开始, 排名类型: {}, 条件: {}", rankingType, criteria);

            // 创建RankingCriteria对象并设置参数
            RankingCriteria rankingCriteria = new RankingCriteria();
            rankingCriteria.setType(RankingType.valueOf(rankingType));
            // 将Map中的参数设置到RankingCriteria对象中
            if (criteria != null) {
                if (criteria.containsKey("limit")) {
                    rankingCriteria.setLimit((Integer) criteria.get("limit"));
                }
                if (criteria.containsKey("timeRange")) {
                    rankingCriteria.setTimeRange((Integer) criteria.get("timeRange"));
                }
                if (criteria.containsKey("minSubmissions")) {
                    rankingCriteria.setMinSubmissions((Integer) criteria.get("minSubmissions"));
                }
            }

            // TODO 多表联查优化：在ProblemStatisticsManager中新增getDynamicRankingWithCriteria()方法
            // TODO 根据动态条件构建复杂的多表联查，支持灵活的排行榜需求
            // TODO 通过JOIN problem_tag_relation 和 problem_tag 表支持按标签筛选的排行榜
            // TODO 通过JOIN user 表支持按创建者或地区筛选的排行榜
            // TODO 通过JOIN submission 表支持按时间范围、提交状态等动态条件筛选
            List<Map<String, Object>> result = problemManager.getProblemRanking(rankingCriteria);

            log.info("ProblemRankingService--->获取题目排名成功, 排名类型: {}, 返回数量: {}", rankingType, result.size());
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取题目排名失败, 排名类型: {}, 错误: {}", rankingType, e.getMessage(), e);
            throw new RuntimeException("获取题目排名失败", e);
        }
    }
}
