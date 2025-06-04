package fun.timu.oj.judge.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.enmus.ProblemStatusEnum;
import fun.timu.oj.common.enmus.ProblemVisibilityEnum;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.controller.request.ProblemUpdateRequest;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;

import fun.timu.oj.judge.model.Enums.RankingType;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.VTO.PopularProblemCategoryVTO;
import fun.timu.oj.judge.model.VTO.ProblemDetailStatisticsVTO;
import fun.timu.oj.judge.model.VTO.ProblemStatisticsVTO;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import fun.timu.oj.judge.service.Problem.*;
import fun.timu.oj.judge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 题目服务实现类
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemManager problemManager;
    private final ProblemCoreService coreService;
    private final ProblemFilterService filterService;
    private final ProblemRankingService rankingService;
    private final ProblemRecommendationService recommendationService;
    private final ProblemReportService reportService;
    private final ProblemStatisticsService statisticsService;
    private final ProblemSubmissionService submissionService;
    private final ProblemTrendService trendService;

    @Override
    public ProblemVO getById(Long id) {
        return coreService.getById(id);
    }

    @Override
    public PageResult<ProblemVO> getProblemsWithConditions(ProblemQueryRequest request) {
        return coreService.getProblemsWithConditions(request);
    }

    @Override
    public List<ProblemVO> getProblemsWithCurrentUser() {
        return coreService.getProblemsWithCurrentUser();
    }

    @Override
    @Transactional
    public Long createProblem(ProblemCreateRequest request) {
        return coreService.createProblem(request);
    }

    @Override
    @Transactional
    public boolean updateProblem(ProblemUpdateRequest request) {
        return coreService.updateProblem(request);
    }

    @Override
    @Transactional
    public boolean deleteProblem(Long id) {
        return coreService.deleteProblem(id);
    }

    @Override
    public boolean updateSubmissionStats(Long problemId, boolean isAccepted) {
        return submissionService.updateSubmissionStats(problemId, isAccepted);
    }

    @Override
    public List<ProblemVO> selectHotProblems(String problemType, Integer difficulty, Integer limit) {
        return recommendationService.selectHotProblems(problemType, difficulty, limit);
    }

    /**
     * 根据指定条件选择推荐的问题
     *
     * @param minAcceptanceRate 最小接受率，用于筛选问题
     * @param maxAcceptanceRate 最大接受率，用于筛选问题
     * @param difficulty        难度级别，用于筛选问题
     * @param limit             最多返回的问题数量
     * @param minAcceptanceRate 最小通过率
     * @param maxAcceptanceRate 最大通过率
     * @param difficulty        难度级别，用于筛选问题
     * @param limit             最多返回的问题数量
     * @return 返回一个ProblemVO对象列表，包含根据条件筛选出的问题
     * <p>
     * 该方法首先调用problemManager的selectRecommendedProblems方法来获取符合条件的问题列表，
     * 然后将这些问题从ProblemDO对象转换为ProblemVO对象，以便于后续处理或传输
     * 如果在处理过程中遇到异常，将记录错误日志并抛出运行时异常
     * /**
     * 查询推荐题目（旧接口，推荐使用新的统一接口）
     * 为了保持向后兼容性，此方法将调用转换为新的统一接口
     * @return 返回一个ProblemVO对象列表，包含根据条件筛选出的问题
     * <p>
     * 该方法首先调用problemManager的selectRecommendedProblems方法来获取符合条件的问题列表，
     * 然后将这些问题从ProblemDO对象转换为ProblemVO对象，以便于后续处理或传输
     * 如果在处理过程中遇到异常，将记录错误日志并抛出运行时异常
     * @deprecated 此方法已弃用，请使用 {@link #getRecommendedProblems(RecommendationCriteria)} 替代
     */
    @Override
    @Deprecated
    public List<ProblemVO> selectRecommendedProblems(Double minAcceptanceRate, Double maxAcceptanceRate, Integer difficulty, Integer limit) {
        // 调用新的统一接口
        RecommendationCriteria criteria = RecommendationCriteria.forAcceptanceRate(minAcceptanceRate, maxAcceptanceRate, difficulty, limit);
        return getRecommendedProblems(criteria);
    }


    @Override
    public List<ProblemVO> getRecommendedProblems(RecommendationCriteria criteria) {
        return recommendationService.getRecommendedProblems(criteria);
    }


    @Override
    public List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria) {
        return recommendationService.getRecommendedProblemsWithScore(criteria);
    }


    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> problemIds, Integer status) {
        return coreService.batchUpdateStatus(problemIds, status);
    }


    @Override
    public List<ProblemStatisticsVTO> getProblemStatistics() {
        return statisticsService.getProblemStatistics();
    }

    @Override
    public Long countByCreator(Long creatorId) {
        return statisticsService.countByCreator(creatorId);
    }

    @Override
    public int batchSoftDelete(List<Long> problemIds) {
        return coreService.batchSoftDelete(problemIds);
    }

    @Override
    public Double getAcceptanceRate(Long problemId) {
        return statisticsService.getAcceptanceRate(problemId);
    }

    @Override
    public ProblemDetailStatisticsVTO getProblemDetailStatistics() {
        return statisticsService.getProblemDetailStatistics();
    }

    @Override
    public List<PopularProblemCategoryVTO> getPopularProblemCategories(Integer limit) {
        return statisticsService.getPopularProblemCategories(limit);
    }

    @Override
    public PageResult<ProblemVO> selectByDateRange(Date startDate, Date endDate, int pageNum, int pageSize) {
        return filterService.selectByDateRange(startDate, endDate, pageNum, pageSize);
    }


    @Override
    public List<ProblemVO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit) {
        return recommendationService.findSimilarProblems(problemId, difficulty, problemType, limit);
    }

    @Override
    @Transactional
    public boolean batchUpdateVisibility(List<Long> problemIds, Integer visibility) {
        return coreService.batchUpdateVisibility(problemIds, visibility);
    }

    @Override
    @Transactional
    public boolean batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit) {
        return coreService.batchUpdateLimits(problemIds, timeLimit, memoryLimit);
    }

    @Override
    @Transactional
    public int batchResetStatistics(List<Long> problemIds) {
        return submissionService.batchResetStatistics(problemIds);
    }

    @Override
    public PageResult<ProblemVO> selectStaleProblems(int days, int pageNum, int pageSize) {
        return filterService.selectStaleProblems(days, pageNum, pageSize);
    }

    @Override
    public PageResult<ProblemVO> selectProblemsWithoutSubmissions(int pageNum, int pageSize) {
        return submissionService.selectProblemsWithoutSubmissions(pageNum, pageSize);
    }

    @Override
    public boolean publishProblem(Long id) {
        return coreService.publishProblem(id);
    }

    @Override
    public boolean unpublishProblem(Long id) {
        return coreService.unpublishProblem(id);
    }

    // ==================== 分布统计类方法实现 ====================

    @Override
    public List<Map<String, Object>> getStatisticsByDifficulty() {
        return statisticsService.getStatisticsByDifficulty();
    }

    @Override
    public List<Map<String, Object>> getStatisticsByType() {
        return statisticsService.getStatisticsByType();
    }

    @Override
    public List<Map<String, Object>> getStatisticsByLanguage() {
        return statisticsService.getStatisticsByLanguage();
    }

    /**
     * 按状态获取统计信息
     *
     * @return 各状态的统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByStatus() {
        return statisticsService.getStatisticsByStatus();
    }

    // ==================== 趋势分析类方法实现 ====================

    @Override
    public List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity) {
        return trendService.getProblemCreationTrend(startDate, endDate, granularity);
    }


    // ==================== 排名类方法实现 ====================

    @Override
    public List<Map<String, Object>> getPopularProblemsRanking(Integer limit, Integer timeRange) {
        return rankingService.getPopularProblemsRanking(limit, timeRange);
    }

    @Override
    public List<Map<String, Object>> getHardestProblemsRanking(Integer limit) {
        return rankingService.getHardestProblemsRanking(limit);
    }

    @Override
    public List<Map<String, Object>> getEasiestProblemsRanking(Integer limit) {
        return rankingService.getEasiestProblemsRanking(limit);
    }

    @Override
    public List<Map<String, Object>> getMostSubmittedProblemsRanking(Integer limit, Integer timeRange) {
        return rankingService.getMostSubmittedProblemsRanking(limit, timeRange);
    }

    @Override
    public List<Map<String, Object>> getZeroSubmittedProblemsRanking(Integer limit, Integer timeRange) {
        return rankingService.getZeroSubmittedProblemsRanking(limit, timeRange);
    }


    @Override
    public List<Map<String, Object>> getRecentPopularProblemsRanking(Integer limit, Integer days) {
        return rankingService.getRecentPopularProblemsRanking(limit, days);
    }


    @Override
    public List<Map<String, Object>> getProblemRanking(RankingType type, Integer limit) {
        return rankingService.getProblemRanking(type, limit);
    }

    // ==================== 统一统计接口实现 ====================


    @Override
    public List<Map<String, Object>> getProblemRanking(String rankingType, Map<String, Object> criteria) {
        return rankingService.getProblemRanking(rankingType, criteria);
    }

    // ==================== 报告类方法实现 ====================

    @Override
    public Map<String, Object> getMonthlyReport(int year, int month) {
        return reportService.getMonthlyReport(year, month);
    }

    @Override
    public Map<String, Object> getAnnualReport(int year) {
        return reportService.getAnnualReport(year);
    }

    @Override
    public Map<String, Object> getCustomReport(Date startDate, Date endDate, List<String> metrics) {
        return reportService.getCustomReport(startDate, endDate, metrics);
    }

    @Override
    public List<ProblemVO> selectBasicInfoByIds(List<Long> problemIds) {
        return filterService.selectBasicInfoByIds(problemIds);
    }


    @Override
    public List<ProblemVO> selectRecentProblems(int pageNum, int pageSize, Integer limit) {
        return filterService.selectRecentProblems(pageNum, pageSize, limit);
    }


    @Override
    public PageResult<ProblemVO> selectByLanguage(int pageNum, int pageSize, String language) {
        return filterService.selectByLanguage(pageNum, pageSize, language);
    }

    @Override
    public int batchRestore(List<Long> problemIds) {
        return coreService.batchRestore(problemIds);
    }
}















