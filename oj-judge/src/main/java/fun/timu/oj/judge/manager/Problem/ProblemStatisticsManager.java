package fun.timu.oj.judge.manager.Problem;

import fun.timu.oj.judge.controller.request.BatchProblemRequest;
import fun.timu.oj.judge.model.VTO.UnifiedStatisticsVTO;
import fun.timu.oj.judge.model.criteria.DistributionCriteria;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.model.criteria.TrendCriteria;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目统计管理器接口
 * 专门负责题目相关的所有统计功能，包括基础统计、分布统计、趋势分析、排行榜等
 *
 * @author zhengke
 */
public interface ProblemStatisticsManager {
    // ==================== 分布统计功能 ====================

    /**
     * 获取统一的分布统计信息
     * 根据不同的分布维度返回相应的统计数据
     *
     * @param criteria 分布统计条件，包含统计维度、过滤条件等参数
     * @return 分布统计数据列表
     */
    List<Map<String, Object>> getDistributionStatistics(DistributionCriteria criteria);


    /**
     * 获取难度-类型分布矩阵
     *
     * @return 分布矩阵数据
     */
    List<Map<String, Object>> getDifficultyTypeDistribution();

    /**
     * 获取通过率分布统计
     *
     * @param bucketSize 区间大小
     * @return 通过率分布数据
     */
    List<Map<String, Object>> getAcceptanceRateDistribution(Double bucketSize);

    /**
     * 获取提交量分布统计
     *
     * @return 提交量分布数据
     */
    List<Map<String, Object>> getSubmissionCountDistribution();

    // ==================== 趋势分析功能 ====================

    /**
     * 统一的趋势分析接口
     * 根据不同的趋势类型返回相应的趋势数据
     *
     * @param criteria 趋势分析条件
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getTrendAnalysis(TrendCriteria criteria);


    // ==================== 排行榜功能 ====================

    /**
     * 统一的排行榜接口
     * 根据不同的排行榜类型返回相应的排行榜数据
     *
     * @param criteria 排行榜条件
     * @return 排行榜数据列表
     */
    List<Map<String, Object>> getProblemRanking(RankingCriteria criteria);

    // ==================== 健康度和监控功能 ====================

    /**
     * 获取题目综合健康度报告
     *
     * @return 健康度报告
     */
    Map<String, Object> getProblemHealthReport();

    /**
     * 获取平台增长指标
     *
     * @param timeRange 时间范围
     * @return 增长指标数据
     */
    Map<String, Object> getPlatformGrowthMetrics(Integer timeRange);

    /**
     * 获取实时题目状态监控
     *
     * @return 实时状态数据
     */
    Map<String, Object> getRealTimeProblemStatus();

    /**
     * 获取实时提交监控
     *
     * @param timeWindow 时间窗口
     * @return 实时提交监控数据
     */
    Map<String, Object> getRealTimeSubmissionMonitoring(Integer timeWindow);

    // ==================== 报表功能 ====================

    /**
     * 获取月度报表
     *
     * @param year  年份
     * @param month 月份
     * @return 月度报表数据
     */
    Map<String, Object> getMonthlyReport(Integer year, Integer month);

    /**
     * 获取年度报表
     *
     * @param year 年份
     * @return 年度报表数据
     */
    Map<String, Object> getAnnualReport(Integer year);

    /**
     * 获取自定义时间范围报表
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param metrics   指标列表
     * @return 自定义报表数据
     */
    Map<String, Object> getCustomRangeReport(Date startDate, Date endDate, List<String> metrics);


    // ==================== 统一统计接口 ====================

    /**
     * 获取统一统计信息（结构化响应）
     * 使用统一的接口替代多个冗余的统计方法，支持不同范围和过滤条件
     *
     * @param request 统一统计请求，包含范围、过滤条件等参数
     * @return 结构化的统计响应，包含元数据和版本信息
     */
    UnifiedStatisticsVTO getUnifiedStatistics(BatchProblemRequest.UnifiedStatisticsRequest request);

    /**
     * 获取统一统计信息（原始数据）
     * 直接返回Map格式的原始统计数据，适合需要简单数据格式的场景
     *
     * @param request 统一统计请求，包含范围、过滤条件等参数
     * @return 统计数据的原始HashMap
     */
    List<HashMap<String, Object>> getUnifiedStatisticsRaw(BatchProblemRequest.UnifiedStatisticsRequest request);
}
