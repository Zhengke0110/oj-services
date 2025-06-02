package fun.timu.oj.judge.service;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.controller.request.ProblemUpdateRequest;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.VTO.PopularProblemCategoryVTO;
import fun.timu.oj.judge.model.VTO.ProblemDetailStatisticsVTO;
import fun.timu.oj.judge.model.VTO.ProblemStatisticsVTO;
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
    ProblemVO getById(Long id);

    /**
     * 根据条件分页查询题目列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<ProblemVO> getProblemsWithConditions(ProblemQueryRequest request);

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
}
