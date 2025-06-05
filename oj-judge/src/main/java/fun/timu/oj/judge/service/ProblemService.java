package fun.timu.oj.judge.service;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.controller.request.ProblemUpdateRequest;
import fun.timu.oj.judge.model.Enums.RankingType;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.VTO.PopularProblemCategoryVTO;
import fun.timu.oj.judge.model.VTO.ProblemDetailStatisticsVTO;
import fun.timu.oj.judge.model.VTO.ProblemStatisticsVTO;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;

import java.util.Date;
import java.util.List;
import java.util.Map;


public interface ProblemService {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    JsonData getById(Long id);

    /**
     * 根据条件分页查询题目列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    JsonData getProblemsWithConditions(ProblemQueryRequest request);

    /**
     * 获取当前用户创建的题目列表
     *
     * @return 题目列表
     */
    List<ProblemVO> getProblemsWithCurrentUser();

    /**
     * 创建题目
     *
     * @param request 创建题目请求
     * @return 创建的题目ID
     */
    Long createProblem(ProblemCreateRequest request);

    /**
     * 更新题目信息
     *
     * @param request 更新题目请求
     * @return 是否更新成功
     */
    boolean updateProblem(ProblemUpdateRequest request);

    /**
     * 删除题目
     *
     * @param id 题目ID
     * @return 是否删除成功
     */
    boolean deleteProblem(Long id);

    /**
     * 更新题目提交统计
     *
     * @param problemId  题目ID
     * @param isAccepted 提交是否被接受
     * @return 更新是否成功
     */
    boolean updateSubmissionStats(Long problemId, boolean isAccepted);

    /**
     * 获取热门题目列表
     *
     * @param problemType 题目类型
     * @param difficulty  题目难度
     * @param limit       返回数量限制，默认为10
     * @return 热门题目列表
     */
    List<ProblemVO> selectHotProblems(String problemType, Integer difficulty, Integer limit);

    /**
     * 获取推荐题目（统一接口）
     * 支持多种推荐算法：通过率、相似性、热度、算法数据
     *
     * @param criteria 推荐条件，包含推荐类型和相关参数
     * @return 推荐题目列表
     * @since 2.0
     */
    List<ProblemVO> getRecommendedProblems(RecommendationCriteria criteria);

    /**
     * 获取带评分的推荐题目（统一接口）
     * 支持多种推荐算法，返回题目及其推荐评分
     *
     * @param criteria 推荐条件，包含推荐类型和相关参数
     * @return 带评分的推荐题目列表
     * @since 2.0
     */
    List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria);

    /**
     * 查询推荐题目（旧接口，推荐使用新的统一接口）
     *
     * @param minAcceptanceRate 最小通过率
     * @param maxAcceptanceRate 最大通过率
     * @param difficulty        难度
     * @param limit             数量限制
     * @return 推荐题目列表
     * @deprecated 此方法已弃用，请使用 {@link #getRecommendedProblems(RecommendationCriteria)} 替代
     */
    @Deprecated
    List<ProblemVO> selectRecommendedProblems(Double minAcceptanceRate, Double maxAcceptanceRate, Integer difficulty, Integer limit);

    /**
     * 获取题目统计信息
     *
     * @return 统计信息列表（包含各难度级别的题目数量等）
     */
    List<ProblemStatisticsVTO> getProblemStatistics();

    /**
     * 批量更新题目状态
     *
     * @param problemIds 题目ID列表
     * @param status     状态值
     * @return 是否更新成功
     */
    boolean batchUpdateStatus(List<Long> problemIds, Integer status);

    /**
     * 根据创建者ID统计题目数量
     *
     * @param creatorId 创建者ID
     * @return 创建者创建的题目数量
     */
    Long countByCreator(Long creatorId);

    /**
     * 查询最近创建的题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param limit    限制返回的题目总数（可为null，表示无上限）
     * @return 最近创建的题目列表
     */
    List<ProblemVO> selectRecentProblems(int pageNum, int pageSize, Integer limit);

    /**
     * 根据支持的编程语言查询题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param language 编程语言
     * @return 分页题目列表结果
     */
    PageResult<ProblemVO> selectByLanguage(int pageNum, int pageSize, String language);

    /**
     * 批量软删除题目
     *
     * @param problemIds 题目ID列表
     * @return 成功删除的题目数量
     */
    int batchSoftDelete(List<Long> problemIds);

    /**
     * 批量恢复已删除的题目
     *
     * @param problemIds 需要恢复的题目ID列表
     * @return 成功恢复的题目数量
     */
    int batchRestore(List<Long> problemIds);

    /**
     * 获取指定题目的通过率
     *
     * @param problemId 题目ID
     * @return 题目的通过率，如果题目不存在或从未被提交过，则返回0.0
     */
    Double getAcceptanceRate(Long problemId);

    /**
     * 根据题目ID列表获取题目基本信息
     *
     * @param problemIds 题目ID列表
     * @return 包含题目基本信息的列表
     */
    List<ProblemVO> selectBasicInfoByIds(List<Long> problemIds);

    /**
     * 获取题目详细统计信息
     *
     * @return 包含各种维度统计数据的HashMap，包括题目总数、难度分布、类型分布、提交情况等
     */
    ProblemDetailStatisticsVTO getProblemDetailStatistics();

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    List<PopularProblemCategoryVTO> getPopularProblemCategories(Integer limit);

    /**
     * 根据创建时间范围查询题目
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页题目列表
     */
    PageResult<ProblemVO> selectByDateRange(Date startDate, Date endDate, int pageNum, int pageSize);

    /**
     * 查询相似题目（基于标签和难度）
     *
     * @param problemId   题目ID
     * @param difficulty  难度限制
     * @param problemType 题目类型限制
     * @param limit       返回数量限制
     * @return 相似题目列表
     */
    List<ProblemVO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit);

    /**
     * 批量更新题目可见性
     *
     * @param problemIds 题目ID列表
     * @param visibility 可见性值
     * @return 是否更新成功
     */
    boolean batchUpdateVisibility(List<Long> problemIds, Integer visibility);

    /**
     * 批量更新题目的时间和内存限制
     *
     * @param problemIds  题目ID列表
     * @param timeLimit   时间限制（秒）
     * @param memoryLimit 内存限制（MB）
     * @return 是否更新成功
     */
    boolean batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit);

    /**
     * 批量重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 题目ID列表
     * @return 成功重置的题目数量
     */
    int batchResetStatistics(List<Long> problemIds);

    /**
     * 查询长时间未更新的题目
     *
     * @param days     超过多少天未更新视为长时间未更新
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ProblemVO> selectStaleProblems(int days, int pageNum, int pageSize);

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ProblemVO> selectProblemsWithoutSubmissions(int pageNum, int pageSize);

    /**
     * 发布问题
     */
    boolean publishProblem(Long id);

    /**
     * 下线问题
     */
    boolean unpublishProblem(Long id);

    // ==================== 分布统计类方法 ====================

    /**
     * 按难度获取统计信息
     *
     * @return 各难度级别的题目统计信息
     */
    List<Map<String, Object>> getStatisticsByDifficulty();

    /**
     * 按题目类型获取统计信息
     *
     * @return 各题目类型的统计信息
     */
    List<Map<String, Object>> getStatisticsByType();

    /**
     * 按编程语言获取统计信息
     *
     * @return 各编程语言的统计信息
     */
    List<Map<String, Object>> getStatisticsByLanguage();

    /**
     * 按状态获取统计信息
     *
     * @return 各状态的统计信息
     */
    List<Map<String, Object>> getStatisticsByStatus();

    // ==================== 趋势分析类方法 ====================

    /**
     * 获取题目创建趋势
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 题目创建趋势数据
     */
    List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity);


    // ==================== 排名类方法 ====================

    /**
     * 获取热门题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 热门题目排行榜
     */
    List<Map<String, Object>> getPopularProblemsRanking(Integer limit, Integer timeRange);

    /**
     * 获取高质量题目排行榜
     *
     * @param limit 限制返回数量，默认为10
     * @return 高质量题目排行榜数据
     */
    List<Map<String, Object>> getHighQualityProblemsRanking(Integer limit);

    /**
     * 获取最难题目排行榜
     *
     * @param limit 限制数量
     * @return 最难题目排行榜
     */
    List<Map<String, Object>> getHardestProblemsRanking(Integer limit);

    /**
     * 获取最容易题目排行榜
     *
     * @param limit 限制数量
     * @return 最容易题目排行榜
     */
    List<Map<String, Object>> getEasiestProblemsRanking(Integer limit);

    /**
     * 获取最常提交题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 最常提交题目排行榜
     */
    List<Map<String, Object>> getMostSubmittedProblemsRanking(Integer limit, Integer timeRange);

    /**
     * 获取提交零提交题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 提交最多题目排行榜
     */
    public List<Map<String, Object>> getZeroSubmittedProblemsRanking(Integer limit, Integer timeRange);

    /**
     * 获取最近热门题目排行榜
     *
     * @param limit 限制数量
     * @param days  最近天数
     * @return 最近热门题目排行榜
     */

    List<Map<String, Object>> getRecentPopularProblemsRanking(Integer limit, Integer days);

    /**
     * 统一的排行榜接口
     *
     * @param type 排行榜类型
     * @return 排行榜数据列表
     */
    List<Map<String, Object>> getProblemRanking(RankingType type, Integer limit);
    // ==================== 统一统计接口 ====================

    /**
     * 获取题目排名
     *
     * @param rankingType 排名类型
     * @param criteria    排名条件
     * @return 题目排名结果
     */
    List<Map<String, Object>> getProblemRanking(String rankingType, Map<String, Object> criteria);

    // ==================== 报告类方法 ====================

    /**
     * 获取月度报告
     *
     * @param year  年份
     * @param month 月份
     * @return 月度报告数据
     */
    Map<String, Object> getMonthlyReport(int year, int month);

    /**
     * 获取年度报告
     *
     * @param year 年份
     * @return 年度报告数据
     */
    Map<String, Object> getAnnualReport(int year);

    /**
     * 获取自定义报告
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param metrics   指标列表
     * @return 自定义报告数据
     */
    Map<String, Object> getCustomReport(Date startDate, Date endDate, List<String> metrics);
}
