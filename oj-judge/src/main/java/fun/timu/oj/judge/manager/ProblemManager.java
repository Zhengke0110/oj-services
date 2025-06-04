package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.controller.request.BatchProblemRequest;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import fun.timu.oj.judge.model.criteria.DistributionCriteria;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import fun.timu.oj.judge.model.criteria.TrendCriteria;
import fun.timu.oj.judge.model.VTO.UnifiedStatisticsVTO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ProblemManager {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemDO getById(Long id);


    /**
     * 分页查询题目列表
     *
     * @param pageNum            当前页码
     * @param pageSize           每页数量
     * @param problemType        题目类型
     * @param difficulty         难度等级
     * @param status             状态
     * @param visibility         可见性
     * @param supportedLanguages 支持的语言列表
     * @param hasInput           是否有输入
     * @param MinAcceptanceRate  最小通过率
     * @param MaxAcceptanceRate  最大通过率
     */
    public IPage<ProblemDO> findTagListWithPage(int pageNum, int pageSize, String problemType, Integer difficulty, Integer status, Integer visibility, List<String> supportedLanguages, Boolean hasInput, Double MinAcceptanceRate, Double MaxAcceptanceRate);

    /**
     * 根据创建者id查询题目列表
     *
     * @param creatorId 创建者id
     * @return 题目列表
     */
    public List<ProblemDO> findByCreatorId(Long creatorId);

    /**
     * 保存题目
     *
     * @param problemDO
     * @return
     */
    public int save(ProblemDO problemDO);

    /**
     * 更新题目
     *
     * @param problemDO
     * @return
     */
    public int updateById(ProblemDO problemDO);

    /**
     * 删除题目
     *
     * @param id
     * @return
     */
    public int deleteById(Long id);

    /**
     * 检查指定标题的题目是否已存在
     *
     * @param title 题目标题
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByTitle(String title);

    /**
     * 更新题目提交统计
     *
     * @param problemId  题目ID
     * @param isAccepted 是否通过
     * @return 更新的记录数
     */
    public int updateSubmissionStats(Long problemId, LoginUser loginUser, boolean isAccepted);

    /**
     * 查询热门题目（按提交次数排序）
     *
     * @param problemType 题目类型
     * @param difficulty  题目难度分级
     * @param limit       限制返回的题目数量，默认10个
     * @return 分页结果
     */
    List<ProblemDO> selectHotProblems(String problemType, Integer difficulty, Integer limit);

    /**
     * 统一的推荐题目接口
     * 支持多种推荐算法：通过率推荐、相似性推荐、热门推荐、算法数据推荐
     *
     * @param criteria 推荐条件
     * @return 推荐题目列表
     */
    List<ProblemDO> getRecommendedProblems(RecommendationCriteria criteria);

    /**
     * 统一的推荐算法数据接口
     * 返回包含推荐评分等详细信息的数据
     *
     * @param criteria 推荐条件
     * @return 推荐数据列表（包含评分信息）
     */
    List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria);


    /**
     * 查询推荐题目（通过率适中的题目）
     *
     * @param minAcceptanceRate 最小通过率
     * @param maxAcceptanceRate 最大通过率
     * @param difficulty        难度限制
     * @param limit             限制数量
     * @return 分页结果
     */
    List<ProblemDO> selectRecommendedProblems(Double minAcceptanceRate, Double maxAcceptanceRate, Integer difficulty, Integer limit);


    /**
     * 获取题目统计信息
     *
     * @return 统计信息列表（包含各难度级别的题目数量等）
     */
    List<ProblemStatisticsDTO> getProblemStatistics();

    /**
     * 批量更新题目状态
     *
     * @param problemIds 题目ID列表
     * @param status     要更新的状态值
     * @return 更新的记录数
     */
    int batchUpdateStatus(List<Long> problemIds, Integer status);

    /**
     * 根据创建者查询题目数量
     *
     * @param creatorId 创建者ID
     * @return 题目数量
     */
    Long countByCreator(Long creatorId);

    /**
     * 查询最近创建的题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param limit    限制返回的题目总数（可为 null，表示无上限）
     * @return 分页结果
     */
    IPage<ProblemDO> selectRecentProblems(int pageNum, int pageSize, Integer limit);

    /**
     * 根据支持的编程语言查询题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param language 编程语言
     * @return 分页结果
     */
    IPage<ProblemDO> selectByLanguage(int pageNum, int pageSize, String language);

    /**
     * 软删除题目（批量）
     *
     * @param problemIds 题目ID列表
     * @return 删除的记录数
     */
    int batchSoftDelete(List<Long> problemIds);

    /**
     * 恢复已删除的题目（批量）
     *
     * @param problemIds 题目ID列表
     * @return 恢复的记录数
     */
    int batchRestore(List<Long> problemIds);

    /**
     * 查询题目的通过率
     *
     * @param problemId 题目ID
     * @return 通过率（小数形式，如0.6表示60%）
     */
    Double getAcceptanceRate(Long problemId);

    /**
     * 批量获取题目的基本信息
     *
     * @param problemIds 题目ID列表
     * @return 题目信息列表
     */
    List<ProblemDO> selectBasicInfoByIds(List<Long> problemIds);

    /**
     * 获取题目详细统计信息
     *
     * @return 包含各种维度统计数据的HashMap，包括题目总数、难度分布、类型分布、提交情况等
     */
    ProblemDetailStatisticsDTO getProblemDetailStatistics();

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    List<PopularProblemCategoryDTO> getPopularProblemCategories(Integer limit);

    /**
     * 根据创建时间范围查询题目
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param status    状态筛选
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页结果
     */
    IPage<ProblemDO> selectByDateRange(Date startDate, Date endDate, Integer status, int pageNum, int pageSize);

    /**
     * 查询相似题目（基于标签和难度）
     *
     * @param problemId   题目ID
     * @param difficulty  难度限制
     * @param problemType 题目类型限制
     * @param limit       返回数量限制
     * @return 相似题目列表
     */
    List<ProblemDO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit);

    /**
     * 批量更新题目可见性
     *
     * @param problemIds 题目ID列表
     * @param visibility 可见性值
     * @return 更新的记录数
     */
    int batchUpdateVisibility(List<Long> problemIds, Integer visibility);

    /**
     * 批量更新题目的时间和内存限制
     *
     * @param problemIds  需要更新的题目ID列表
     * @param timeLimit   新的时间限制值（毫秒）
     * @param memoryLimit 新的内存限制值（MB）
     * @return 成功更新的记录数
     */
    public int batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit);


    /**
     * 查询长时间未更新的题目
     *
     * @param lastUpdateBefore 上次更新时间早于此日期的题目将被视为陈旧题目
     * @param pageNum          页码（从1开始）
     * @param pageSize         每页大小
     * @return 分页结果，包含符合条件的题目列表
     */
    public IPage<ProblemDO> selectStaleProblems(Date lastUpdateBefore, int pageNum, int pageSize);

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public IPage<ProblemDO> selectProblemsWithoutSubmissions(int pageNum, int pageSize);

    /**
     * 获取题目总体统计信息（增强版）
     *
     * @return 统计信息
     */
    Map<String, Object> getOverallStatistics();

    /**
     * 获取统一的分布统计信息
     * 此接口替代了多个按不同维度统计的方法，提供更灵活的分布分析功能
     *
     * @param criteria 分布统计条件，包含统计维度、过滤条件等参数
     * @return 分布统计数据列表
     */
    List<Map<String, Object>> getDistributionStatistics(DistributionCriteria criteria);

    /**
     * 按难度获取统计信息
     *
     * @return 难度统计列表
     */
    List<Map<String, Object>> getStatisticsByDifficulty();

    /**
     * 按类型获取统计信息
     *
     * @return 类型统计列表
     */
    List<Map<String, Object>> getStatisticsByType();

    /**
     * 按语言获取统计信息
     *
     * @return 语言统计列表
     */
    List<Map<String, Object>> getStatisticsByLanguage();

    /**
     * 按状态获取统计信息
     *
     * @return 状态统计列表
     */
    List<Map<String, Object>> getStatisticsByStatus();

    /**
     * 统一的趋势分析接口（替代原有的4个冗余接口）
     *
     * @param criteria 趋势分析条件
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getTrendAnalysis(TrendCriteria criteria);

    /**
     * 获取题目创建趋势
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 创建趋势数据
     */
    List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity);

    /**
     * 获取提交趋势分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 提交趋势数据
     */
    List<Map<String, Object>> getSubmissionTrendAnalysis(Date startDate, Date endDate, String granularity);

    /**
     * 获取通过率趋势分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 通过率趋势数据
     */
    List<Map<String, Object>> getAcceptanceRateTrend(Date startDate, Date endDate, String granularity);


    /**
     * 获取热门题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 热门题目排行榜
     */
    List<Map<String, Object>> getPopularProblemsRanking(Integer limit, Integer timeRange);

    /**
     * 获取最难题目排行榜
     *
     * @param limit 限制数量
     * @return 最难题目排行榜
     */
    List<Map<String, Object>> getHardestProblemsRanking(Integer limit);

    /**
     * 获取简单题目排行榜
     *
     * @param limit 限制数量
     * @return 最易题目排行榜
     */
    List<Map<String, Object>> getEasiestProblemsRanking(Integer limit);

    /**
     * 获取最多提交题目排行榜
     *
     * @param limit 限制数量
     * @return 最易题目排行榜
     */
    List<Map<String, Object>> getMaxSubmissionProblemsRanking(Integer limit, Integer timeRange);

    /**
     * 获取零提交题目排行榜
     *
     * @param limit 限制数量
     * @return 最易题目排行榜
     */
    List<Map<String, Object>> getZeroSubmissionProblemsRanking(Integer limit, Integer timeRange);

    /**
     * 获取创建者贡献排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 创建者贡献排行榜
     */
    List<Map<String, Object>> getCreatorContributionRanking(Integer limit, Integer timeRange);

    /**
     * 获取题目质量排行榜
     *
     * @param limit 限制数量
     * @return 质量排行榜
     */
    List<Map<String, Object>> getQualityProblemsRanking(Integer limit);

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

    /**
     * 获取题目综合健康度报告
     *
     * @return 健康度报告
     */
    Map<String, Object> getProblemHealthReport();

    /**
     * 获取平台数据大屏统计
     *
     * @return 大屏统计数据
     */
    Map<String, Object> getDashboardStatistics();

    /**
     * 获取题目推荐数据
     *
     * @param difficulty  难度偏好
     * @param problemType 类型偏好
     * @param limit       推荐数量
     * @return 推荐数据
     */
    List<Map<String, Object>> getRecommendationData(Integer difficulty, String problemType, Integer limit);

    /**
     * 获取题目相关性分析
     *
     * @param problemId 题目ID
     * @param limit     相关题目数量
     * @return 相关性分析数据
     */
    List<Map<String, Object>> getProblemCorrelationAnalysis(Long problemId, Integer limit);

    /**
     * 获取平台增长指标
     *
     * @param timeRange 时间范围
     * @return 增长指标数据
     */
    Map<String, Object> getPlatformGrowthMetrics(Integer timeRange);

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

    /**
     * 批量重置题目统计
     *
     * @param problemIds 题目ID列表
     * @return 重置记录数
     */
    int batchResetStats(List<Long> problemIds);


    /**
     * 统一推荐方法
     * <p>
     * 此方法根据给定的推荐标准（RecommendationCriteria）返回题目推荐列表
     * 支持根据难度、题目类型、标签、创建者等多种条件进行灵活组合查询
     *
     * @param criteria 推荐标准，包含多种筛选条件
     * @return 符合条件的题目列表
     */
    public List<ProblemDO> recommendProblems(RecommendationCriteria criteria);


    /**
     * 统一的统计信息接口
     * 根据不同的统计范围返回相应的统计数据
     *
     * @param request 统一统计请求参数
     * @return 统一统计响应数据
     */
    UnifiedStatisticsVTO getUnifiedStatistics(BatchProblemRequest.UnifiedStatisticsRequest request);

    /**
     * 统一的统计信息接口（原始数据版本）
     * 返回原始的HashMap格式数据，用于特殊场景
     *
     * @param request 统一统计请求参数
     * @return 统计数据的原始HashMap
     */
    List<HashMap<String, Object>> getUnifiedStatisticsRaw(BatchProblemRequest.UnifiedStatisticsRequest request);


    /**
     * 统一的排行榜接口
     * 根据不同的排行榜类型返回相应的排行榜数据
     *
     * @param criteria 排行榜条件
     * @return 排行榜数据列表
     */
    List<Map<String, Object>> getProblemRanking(RankingCriteria criteria);

    /**
     * 统一的排行榜接口（题目实体版本）
     * 返回ProblemDO实体列表，用于需要完整题目信息的场景
     *
     * @param criteria 排行榜条件
     * @return 题目实体列表
     */
    List<ProblemDO> getProblemRankingEntities(RankingCriteria criteria);
}
