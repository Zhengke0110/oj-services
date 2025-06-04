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

            List<Map<String, Object>> result = problemManager.getProblemRanking(criteria);

            log.info("ProblemRankingService--->获取热门题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取热门题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取热门题目排行榜失败", e);
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
     * @param type  排行榜类型
     * @param limit 限制数量
     * @return 排行榜数据列表
     */
    @Override
    public List<Map<String, Object>> getProblemRanking(RankingType type, Integer limit) {
        try {
            log.info("ProblemRankingService--->获取题目排行榜, 类型: {}", type);

            // 构建排行榜条件
            RankingCriteria criteria = RankingCriteria.fromRankingType(type, limit, null, null, null);

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

            List<Map<String, Object>> result = problemManager.getProblemRanking(rankingCriteria);

            log.info("ProblemRankingService--->获取题目排名成功, 排名类型: {}, 返回数量: {}", rankingType, result.size());
            return result;
        } catch (Exception e) {
            log.error("ProblemRankingService--->获取题目排名失败, 排名类型: {}, 错误: {}", rankingType, e.getMessage(), e);
            throw new RuntimeException("获取题目排名失败", e);
        }
    }
}
