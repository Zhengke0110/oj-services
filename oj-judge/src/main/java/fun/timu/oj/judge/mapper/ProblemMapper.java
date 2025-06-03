package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.criteria.DistributionCriteria;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.model.criteria.TrendCriteria;
import fun.timu.oj.judge.model.request.UnifiedStatisticsRequest;
import fun.timu.oj.judge.model.response.UnifiedStatisticsResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface ProblemMapper extends BaseMapper<ProblemDO> {
    /**
     * 统一的统计信息接口
     * 根据不同的统计范围返回相应的统计数据
     *
     * @param request 统一统计请求参数
     * @return 统一统计响应数据
     */
    UnifiedStatisticsResponse getUnifiedStatistics(@Param("request") UnifiedStatisticsRequest request);

    /**
     * 统一的统计信息接口（原始数据版本）
     * 返回原始的HashMap格式数据，用于特殊场景
     *
     * @param request 统一统计请求参数
     * @return 统计数据的原始HashMap
     */
    HashMap<String, Object> getUnifiedStatisticsRaw(@Param("request") UnifiedStatisticsRequest request);

    /**
     * 统一的推荐题目接口
     * 根据不同的推荐条件返回推荐题目列表
     *
     * @param criteria 推荐条件
     * @return 推荐题目列表
     */
    List<ProblemDO> getRecommendedProblems(@Param("criteria") RecommendationCriteria criteria);

    /**
     * 统一的推荐算法数据接口
     * 返回包含推荐评分等详细信息的数据
     *
     * @param criteria 推荐条件
     * @return 推荐数据列表
     */
    List<HashMap<String, Object>> getRecommendedProblemsWithScore(@Param("criteria") RecommendationCriteria criteria);

    /**
     * 统一的排行榜接口
     * 根据不同的排行榜类型返回相应的排行榜数据
     *
     * @param criteria 排行榜条件
     * @return 排行榜数据列表
     */
    List<HashMap<String, Object>> getProblemRanking(@Param("criteria") RankingCriteria criteria);

    /**
     * 统一的排行榜接口（题目实体版本）
     * 返回ProblemDO实体列表，用于需要完整题目信息的场景
     *
     * @param criteria 排行榜条件
     * @return 题目实体列表
     */
    List<ProblemDO> getProblemRankingEntities(@Param("criteria") RankingCriteria criteria);

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    List<HashMap<String, Object>> getPopularProblemCategories(@Param("limit") Integer limit);

    // ===== 新增：统一分布分析接口 =====

    /**
     * 统一的分布分析接口
     * 根据不同的维度返回相应的分布统计数据
     *
     * @param criteria 分布分析条件
     * @return 分布统计数据列表
     */
    List<HashMap<String, Object>> getDistributionStatistics(@Param("criteria") DistributionCriteria criteria);

    /**
     * 统一的趋势分析接口（替代原有的4个冗余接口）
     *
     * @param criteria 趋势分析条件
     * @return 趋势数据列表
     */
    List<HashMap<String, Object>> getTrendAnalysis(@Param("criteria") TrendCriteria criteria);

    /**
     * 获取难度-类型分布矩阵
     *
     * @return 分布矩阵数据
     */
    List<HashMap<String, Object>> getDifficultyTypeDistribution();

    /**
     * 获取通过率分布统计
     *
     * @param bucketSize 区间大小（如0.1表示10%一个区间）
     * @return 通过率分布数据
     */
    List<HashMap<String, Object>> getAcceptanceRateDistribution(@Param("bucketSize") Double bucketSize);

    /**
     * 获取提交量分布统计
     *
     * @return 提交量分布数据
     */
    List<HashMap<String, Object>> getSubmissionCountDistribution();

    /**
     * 获取题目时间限制分布
     *
     * @return 时间限制分布数据
     */
    List<HashMap<String, Object>> getTimeLimitDistribution();

    /**
     * 获取题目内存限制分布
     *
     * @return 内存限制分布数据
     */
    List<HashMap<String, Object>> getMemoryLimitDistribution();

    /**
     * 获取题目可见性分布
     *
     * @return 可见性分布数据
     */
    List<HashMap<String, Object>> getVisibilityDistribution();

    /**
     * 获取编程语言使用分布
     *
     * @return 语言使用分布数据
     */
    List<HashMap<String, Object>> getLanguageUsageDistribution();

    /**
     * 获取创建者活跃度分析
     *
     * @param timeRange 时间范围（天数）
     * @return 创建者活跃度数据
     */
    List<HashMap<String, Object>> getCreatorActivityAnalysis(@Param("timeRange") Integer timeRange);

    /**
     * 获取题目被访问热度分析
     *
     * @param limit 限制数量
     * @return 访问热度数据
     */
    List<HashMap<String, Object>> getProblemAccessHeatmap(@Param("limit") Integer limit);

    /**
     * 获取题目解题模式分析
     *
     * @param limit 限制数量
     * @return 解题模式数据
     */
    List<HashMap<String, Object>> getProblemSolvingPatternAnalysis(@Param("limit") Integer limit);

    /**
     * 获取题目难度偏好分析
     *
     * @return 难度偏好数据
     */
    List<HashMap<String, Object>> getDifficultyPreferenceAnalysis();

    // ===== V2新增：性能分析聚合接口 =====

    /**
     * 获取题目性能指标分析
     *
     * @param limit 限制数量
     * @return 性能指标数据
     */
    List<HashMap<String, Object>> getProblemPerformanceMetrics(@Param("limit") Integer limit);

    /**
     * 获取资源使用效率分析
     *
     * @return 资源使用效率数据
     */
    List<HashMap<String, Object>> getResourceUsageEfficiencyAnalysis();

    /**
     * 获取题目响应时间分析
     *
     * @param timeRange 时间范围（天数）
     * @return 响应时间分析数据
     */
    List<HashMap<String, Object>> getProblemResponseTimeAnalysis(@Param("timeRange") Integer timeRange);


    /**
     * 获取题目综合健康度报告
     *
     * @return 健康度报告数据
     */
    HashMap<String, Object> getProblemHealthReport();

    /**
     * 获取题目标签云数据
     *
     * @param limit 标签数量限制
     * @return 标签云数据
     */
    List<HashMap<String, Object>> getTagCloudData(@Param("limit") Integer limit);

    /**
     * 获取平台竞争力分析
     *
     * @return 竞争力分析数据
     */
    HashMap<String, Object> getPlatformCompetitivenessAnalysis();

    /**
     * 获取题目生态健康度指标
     *
     * @return 生态健康度数据
     */
    HashMap<String, Object> getProblemEcosystemHealth();

    /**
     * 获取题目相关性分析（基于用户行为）
     *
     * @param problemId 目标题目ID
     * @param limit     相关题目数量
     * @return 相关题目数据
     */
    List<HashMap<String, Object>> getProblemCorrelationAnalysis(@Param("problemId") Long problemId, @Param("limit") Integer limit);

    /**
     * 获取题目难度预测数据
     *
     * @param problemId 题目ID
     * @return 难度预测数据
     */
    HashMap<String, Object> getDifficultyPredictionData(@Param("problemId") Long problemId);

    /**
     * 获取题目生命周期分析
     *
     * @param problemId 题目ID
     * @return 生命周期数据
     */
    HashMap<String, Object> getProblemLifecycleAnalysis(@Param("problemId") Long problemId);

    /**
     * 获取平台增长指标
     *
     * @param timeRange 时间范围（天数）
     * @return 增长指标数据
     */
    HashMap<String, Object> getPlatformGrowthMetrics(@Param("timeRange") Integer timeRange);

    /**
     * 获取题目集群分析
     *
     * @param clusterMethod 聚类方法
     * @param clusterCount  聚类数量
     * @return 聚类分析数据
     */
    List<HashMap<String, Object>> getProblemClusterAnalysis(@Param("clusterMethod") String clusterMethod, @Param("clusterCount") Integer clusterCount);

    /**
     * 获取题目异常检测分析
     *
     * @param detectionMethod 检测方法
     * @return 异常检测数据
     */
    List<HashMap<String, Object>> getProblemAnomalyDetection(@Param("detectionMethod") String detectionMethod);

    // ===== V2新增：导出和报表聚合接口 =====

    /**
     * 获取题目月度报表数据
     *
     * @param year  年份
     * @param month 月份
     * @return 月度报表数据
     */
    HashMap<String, Object> getMonthlyReport(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 获取题目年度报表数据
     *
     * @param year 年份
     * @return 年度报表数据
     */
    HashMap<String, Object> getAnnualReport(@Param("year") Integer year);

    /**
     * 获取自定义时间范围统计报表
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param metrics   指标列表
     * @return 自定义报表数据
     */
    HashMap<String, Object> getCustomRangeReport(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("metrics") List<String> metrics);

    /**
     * 获取周度报表数据
     *
     * @param year 年份
     * @param week 周数
     * @return 周度报表数据
     */
    HashMap<String, Object> getWeeklyReport(@Param("year") Integer year, @Param("week") Integer week);

    /**
     * 获取季度报表数据
     *
     * @param year    年份
     * @param quarter 季度
     * @return 季度报表数据
     */
    HashMap<String, Object> getQuarterlyReport(@Param("year") Integer year, @Param("quarter") Integer quarter);

    // ===== V2新增：实时监控聚合接口 =====

    /**
     * 获取实时题目状态监控
     *
     * @return 实时状态数据
     */
    HashMap<String, Object> getRealTimeProblemStatus();

    /**
     * 获取实时提交监控统计
     *
     * @param timeWindow 时间窗口（分钟）
     * @return 实时提交统计
     */
    HashMap<String, Object> getRealTimeSubmissionMonitoring(@Param("timeWindow") Integer timeWindow);

    /**
     * 获取实时错误率监控
     *
     * @param timeWindow 时间窗口（分钟）
     * @return 实时错误率数据
     */
    HashMap<String, Object> getRealTimeErrorRateMonitoring(@Param("timeWindow") Integer timeWindow);

    /**
     * 获取实时性能监控
     *
     * @param timeWindow 时间窗口（分钟）
     * @return 实时性能数据
     */
    HashMap<String, Object> getRealTimePerformanceMonitoring(@Param("timeWindow") Integer timeWindow);

    // ===== V2新增：预测分析聚合接口 =====

    /**
     * 获取题目提交量预测
     *
     * @param problemId   题目ID
     * @param predictDays 预测天数
     * @return 提交量预测数据
     */
    HashMap<String, Object> getProblemSubmissionPrediction(@Param("problemId") Long problemId, @Param("predictDays") Integer predictDays);

    /**
     * 获取平台增长预测
     *
     * @param predictMonths 预测月数
     * @return 增长预测数据
     */
    HashMap<String, Object> getPlatformGrowthPrediction(@Param("predictMonths") Integer predictMonths);

    /**
     * 获取题目流行度预测
     *
     * @param timeRange 时间范围
     * @param limit     限制数量
     * @return 流行度预测数据
     */
    List<HashMap<String, Object>> getProblemPopularityPrediction(@Param("timeRange") Integer timeRange, @Param("limit") Integer limit);
}




