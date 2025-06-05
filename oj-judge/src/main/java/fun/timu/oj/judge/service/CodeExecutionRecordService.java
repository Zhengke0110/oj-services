package fun.timu.oj.judge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.model.DO.CodeExecutionRecordDO;

import java.util.HashMap;
import java.util.List;

/**
 * 代码执行记录服务接口
 * 提供代码执行记录相关的业务服务
 *
 * @author zhengke
 */
public interface CodeExecutionRecordService {

    // ================== 基础统计查询 ==================

    /**
     * 获取语言使用统计
     *
     * @return 语言统计列表
     */
    List<HashMap<String, Object>> getLanguageStatistics();

    /**
     * 获取执行状态统计
     *
     * @return 执行状态统计列表
     */
    List<HashMap<String, Object>> getExecutionStatusStatistics();

    /**
     * 获取热门问题统计（按执行次数排序）
     *
     * @param limit 限制数量
     * @return 热门问题统计列表
     */
    List<HashMap<String, Object>> getPopularProblems(int limit);

    /**
     * 获取活跃用户统计（按执行次数排序）
     *
     * @param limit 限制数量
     * @return 活跃用户统计列表
     */
    List<HashMap<String, Object>> getActiveUsers(int limit);

    /**
     * 批量插入代码执行记录
     *
     * @param records 记录列表
     * @return 插入的行数
     */
    int batchInsert(List<CodeExecutionRecordDO> records);

    // ================== 时间范围统计查询 ==================

    /**
     * 按时间范围统计执行记录数量
     *
     * @param startTime 开始时间戳
     * @param endTime   结束时间戳
     * @return 统计结果
     */
    HashMap<String, Object> getExecutionCountByTimeRange(long startTime, long endTime);

    /**
     * 按小时统计执行记录（过去24小时）
     *
     * @return 小时统计列表
     */
    List<HashMap<String, Object>> getExecutionCountByHour();

    /**
     * 按天统计执行记录（过去30天）
     *
     * @return 天统计列表
     */
    List<HashMap<String, Object>> getExecutionCountByDay();

    /**
     * 按月统计执行记录（过去12个月）
     *
     * @return 月统计列表
     */
    List<HashMap<String, Object>> getExecutionCountByMonth();

    // ================== 性能分析统计 ==================

    /**
     * 获取执行时间统计信息
     *
     * @return 执行时间统计
     */
    HashMap<String, Object> getExecutionTimeStatistics();

    /**
     * 获取内存使用统计信息
     *
     * @return 内存使用统计
     */
    HashMap<String, Object> getMemoryUsageStatistics();

    /**
     * 按语言统计平均执行时间和内存使用
     *
     * @return 语言性能统计列表
     */
    List<HashMap<String, Object>> getPerformanceByLanguage();

    /**
     * 按问题统计平均执行时间和内存使用
     *
     * @param limit 限制数量
     * @return 问题性能统计列表
     */
    List<HashMap<String, Object>> getPerformanceByProblem(int limit);

    // ================== 用户行为分析 ==================

    /**
     * 获取用户提交频率统计（按小时分布）
     *
     * @return 用户提交时间分布
     */
    List<HashMap<String, Object>> getSubmissionTimeDistribution();

    /**
     * 获取用户成功率分布统计
     *
     * @return 用户成功率分布
     */
    List<HashMap<String, Object>> getSuccessRateDistribution();

    /**
     * 获取最活跃的时间段统计
     *
     * @return 活跃时间段统计
     */
    List<HashMap<String, Object>> getActiveTimePeriods();

    // ================== 问题难度分析 ==================

    /**
     * 按问题统计成功率（用于判断问题难度）
     *
     * @return 问题成功率统计
     */
    List<HashMap<String, Object>> getProblemSuccessRates();

    /**
     * 获取最难的问题排行（成功率最低）
     *
     * @param limit 限制数量
     * @return 最难问题列表
     */
    List<HashMap<String, Object>> getHardestProblems(int limit);

    /**
     * 获取最简单的问题排行（成功率最高）
     *
     * @param limit 限制数量
     * @return 最简单问题列表
     */
    List<HashMap<String, Object>> getEasiestProblems(int limit);

    // ================== 代码质量分析 ==================

    /**
     * 获取代码复用统计
     *
     * @return 代码复用统计
     */
    List<HashMap<String, Object>> getCodeReuseStatistics();

    /**
     * 获取最常用的代码模式
     *
     * @param limit 限制数量
     * @return 常用代码模式列表
     */
    List<HashMap<String, Object>> getCommonCodePatterns(int limit);

    // ================== 系统性能监控 ==================

    /**
     * 获取系统负载统计
     *
     * @return 系统负载统计
     */
    List<HashMap<String, Object>> getSystemLoadStatistics();

    /**
     * 获取容器使用统计
     *
     * @return 容器使用统计
     */
    List<HashMap<String, Object>> getContainerUsageStatistics();

    /**
     * 获取错误类型统计
     *
     * @return 错误类型统计
     */
    List<HashMap<String, Object>> getErrorTypeStatistics();

    // ================== 趋势分析 ==================

    /**
     * 获取用户增长趋势
     *
     * @param days 分析天数
     * @return 用户增长趋势
     */
    List<HashMap<String, Object>> getUserGrowthTrend(int days);

    /**
     * 获取执行量趋势
     *
     * @param days 分析天数
     * @return 执行量趋势
     */
    List<HashMap<String, Object>> getExecutionVolumeTrend(int days);

    /**
     * 获取成功率趋势
     *
     * @param days 分析天数
     * @return 成功率趋势
     */
    List<HashMap<String, Object>> getSuccessRateTrend(int days);

    // ================== 高级分析查询 ==================

    /**
     * 获取用户能力分析
     *
     * @param limit 限制数量
     * @return 用户能力分析列表
     */
    List<HashMap<String, Object>> getUserAbilityAnalysis(int limit);

    /**
     * 获取语言流行度趋势
     *
     * @param months 分析月数
     * @return 语言流行度趋势
     */
    List<HashMap<String, Object>> getLanguagePopularityTrend(int months);

    /**
     * 获取问题热度分析
     *
     * @param limit 限制数量
     * @return 问题热度分析列表
     */
    List<HashMap<String, Object>> getProblemHeatAnalysis(int limit);

    /**
     * 获取性能异常检测
     *
     * @param executionTimeThreshold 执行时间阈值
     * @param memoryThreshold        内存使用阈值
     * @return 性能异常检测列表
     */
    List<HashMap<String, Object>> getPerformanceAnomalies(long executionTimeThreshold, long memoryThreshold);

    // ================== 竞赛和比赛分析 ==================

    /**
     * 获取竞赛统计分析
     *
     * @param startTime 竞赛开始时间
     * @param endTime   竞赛结束时间
     * @return 竞赛统计分析
     */
    List<HashMap<String, Object>> getContestAnalysis(long startTime, long endTime);

    /**
     * 获取排行榜数据
     *
     * @param limit 限制数量
     * @return 排行榜数据列表
     */
    List<HashMap<String, Object>> getLeaderboard(int limit);

    /**
     * 获取用户进步分析
     *
     * @param accountNo 账号
     * @param days      分析天数
     * @return 用户进步分析
     */
    HashMap<String, Object> getUserProgressAnalysis(String accountNo, int days);

    // ================== 实时监控和预警 ==================

    /**
     * 获取实时系统健康度指标
     *
     * @param minutes 时间窗口（分钟）
     * @return 系统健康度指标
     */
    HashMap<String, Object> getSystemHealthMetrics(int minutes);

    /**
     * 获取异常执行监控
     *
     * @param minutes 时间窗口（分钟）
     * @return 异常执行监控列表
     */
    List<HashMap<String, Object>> getAbnormalExecutions(int minutes);

    /**
     * 获取资源使用预警统计
     *
     * @return 资源使用预警统计
     */
    List<HashMap<String, Object>> getResourceUsageAlerts();

    // ================== 预测分析和机器学习支持 ==================

    /**
     * 获取用户行为预测数据
     *
     * @param accountNo 账号
     * @return 用户行为预测数据
     */
    HashMap<String, Object> getUserBehaviorFeatures(String accountNo);

    /**
     * 获取问题难度预测特征
     *
     * @param problemId 问题ID
     * @return 问题难度预测特征
     */
    HashMap<String, Object> getProblemDifficultyFeatures(long problemId);

    /**
     * 获取执行时间预测特征
     *
     * @param language  编程语言
     * @param problemId 问题ID
     * @return 执行时间预测特征
     */
    HashMap<String, Object> getExecutionTimePredictionFeatures(String language, long problemId);

    // ================== 地理和时区分析 ==================

    /**
     * 获取地理位置统计
     *
     * @return 地理位置统计
     */
    List<HashMap<String, Object>> getGeographicStatistics();

    /**
     * 获取时区活跃度分析
     *
     * @return 时区活跃度分析
     */
    List<HashMap<String, Object>> getTimezoneActivityAnalysis();

    // ================== A/B测试和实验分析 ==================

    /**
     * 获取实验组对比分析
     *
     * @param experimentId 实验ID
     * @return 实验组对比分析
     */
    List<HashMap<String, Object>> getExperimentAnalysis(String experimentId);

    /**
     * 获取功能使用情况分析
     *
     * @return 功能使用情况分析
     */
    List<HashMap<String, Object>> getFeatureUsageAnalysis();

    // ================== 安全和合规分析 ==================

    /**
     * 获取安全风险检测
     *
     * @return 安全风险检测
     */
    List<HashMap<String, Object>> getSecurityRiskDetection();

    /**
     * 获取代码抄袭检测统计
     *
     * @return 代码抄袭检测统计
     */
    List<HashMap<String, Object>> getPlagiarismDetection();

    /**
     * 获取合规性报告
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 合规性报告
     */
    HashMap<String, Object> getComplianceReport(long startTime, long endTime);

    // ================== 高级业务智能分析 ==================

    /**
     * 获取用户留存分析
     *
     * @param cohortDays 队列天数
     * @return 用户留存分析
     */
    List<HashMap<String, Object>> getUserRetentionAnalysis(int cohortDays);

    /**
     * 获取学习路径分析
     *
     * @param accountNo 账号
     * @return 学习路径分析
     */
    List<HashMap<String, Object>> getLearningPathAnalysis(String accountNo);

    /**
     * 获取知识点掌握度分析
     *
     * @param accountNo 账号
     * @return 知识点掌握度分析
     */
    List<HashMap<String, Object>> getKnowledgePointMastery(String accountNo);

    /**
     * 获取个性化推荐数据
     *
     * @param accountNo 账号
     * @param limit     限制数量
     * @return 个性化推荐数据
     */
    List<HashMap<String, Object>> getPersonalizationRecommendations(String accountNo, int limit);
}
