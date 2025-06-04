package fun.timu.oj.judge.manager.impl.Problem;

import fun.timu.oj.judge.controller.request.BatchProblemRequest;
import fun.timu.oj.judge.manager.Problem.ProblemStatisticsManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.VTO.UnifiedStatisticsVTO;
import fun.timu.oj.judge.model.criteria.DistributionCriteria;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.model.criteria.TrendCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目统计管理器实现类
 * 专门负责题目相关的所有统计功能，包括基础统计、分布统计、趋势分析、排行榜等
 *
 * @author zhengke
 */
@Component
@RequiredArgsConstructor
public class ProblemStatisticsManagerImpl implements ProblemStatisticsManager {

    private final ProblemMapper problemMapper;

    @Override
    public List<Map<String, Object>> getDistributionStatistics(DistributionCriteria criteria) {
        // TODO 返回值需要修改成实体类
        List<HashMap<String, Object>> result = problemMapper.getDistributionStatistics(criteria);
        return result.stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());
    }


    @Override
    public List<Map<String, Object>> getDifficultyTypeDistribution() {
        List<HashMap<String, Object>> result = problemMapper.getDifficultyTypeDistribution();
        return result.stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAcceptanceRateDistribution(Double bucketSize) {

        List<HashMap<String, Object>> result = problemMapper.getAcceptanceRateDistribution(bucketSize);

        return result.stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());

    }

    @Override
    public List<Map<String, Object>> getSubmissionCountDistribution() {

        List<HashMap<String, Object>> result = problemMapper.getSubmissionCountDistribution();

        return result.stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());

    }

    // ==================== 趋势分析功能 ====================

    @Override
    public List<Map<String, Object>> getTrendAnalysis(TrendCriteria criteria) {

        List<HashMap<String, Object>> result = problemMapper.getTrendAnalysis(criteria);

        return result.stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());
    }

    // ==================== 排行榜功能 ====================

    @Override
    public List<Map<String, Object>> getProblemRanking(RankingCriteria criteria) {

        List<HashMap<String, Object>> result = problemMapper.getProblemRanking(criteria);

        return result.stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());
    }

    // ==================== 健康度和监控功能 ====================

    @Override
    public Map<String, Object> getProblemHealthReport() {
        Map<String, Object> result = problemMapper.getProblemHealthReport();
        return result;
    }

    @Override
    public Map<String, Object> getPlatformGrowthMetrics(Integer timeRange) {
        Map<String, Object> result = problemMapper.getPlatformGrowthMetrics(timeRange);
        return result;
    }

    @Override
    public Map<String, Object> getRealTimeProblemStatus() {
        Map<String, Object> result = problemMapper.getRealTimeProblemStatus();
        return result;
    }

    @Override
    public Map<String, Object> getRealTimeSubmissionMonitoring(Integer timeWindow) {
        Map<String, Object> result = problemMapper.getRealTimeSubmissionMonitoring(timeWindow);
        return result;
    }

    // ==================== 报表功能 ====================

    @Override
    public Map<String, Object> getMonthlyReport(Integer year, Integer month) {
        Map<String, Object> result = problemMapper.getMonthlyReport(year, month);
        return result;
    }

    @Override
    public Map<String, Object> getAnnualReport(Integer year) {
        Map<String, Object> result = problemMapper.getAnnualReport(year);
        return result;
    }

    @Override
    public Map<String, Object> getCustomRangeReport(Date startDate, Date endDate, List<String> metrics) {
        Map<String, Object> result = problemMapper.getCustomRangeReport(startDate, endDate, metrics);
        return result;
    }


    // ==================== 统一统计接口 ====================

    @Override
    public UnifiedStatisticsVTO getUnifiedStatistics(BatchProblemRequest.UnifiedStatisticsRequest request) {

        // 参数校验
        if (request == null) {
            throw new IllegalArgumentException("统计请求参数不能为空");
        }
        if (request.getScope() == null) {
            throw new IllegalArgumentException("统计范围不能为空");
        }

        // 调用Mapper层获取原始统计数据
        List<HashMap<String, Object>> rawData = problemMapper.getUnifiedStatisticsRaw(request);

        if (rawData == null) {
            rawData = new ArrayList<>();
        }

        // 构建结构化响应
        UnifiedStatisticsVTO response = UnifiedStatisticsVTO.builder()
                .scope(request.getScope())
                .timestamp(LocalDateTime.now())
                .version("1.0")
                .data(rawData)
                .metadata(buildMetadata(request, rawData))
                .build();

        return response;
    }

    @Override
    public List<HashMap<String, Object>> getUnifiedStatisticsRaw(BatchProblemRequest.UnifiedStatisticsRequest request) {

        // 参数校验
        if (request == null) {
            throw new IllegalArgumentException("统计请求参数不能为空");
        }
        if (request.getScope() == null) {
            throw new IllegalArgumentException("统计范围不能为空");
        }

        // 查询结果现在是List<HashMap>
        List<HashMap<String, Object>> resultList = problemMapper.getUnifiedStatisticsRaw(request);

        if (resultList == null) {
            resultList = new ArrayList<>();
        }
        return resultList;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建统计元数据信息
     *
     * @param request 统计请求
     * @param rawData 原始数据
     * @return 元数据对象
     */
    private UnifiedStatisticsVTO.StatisticsMetadata buildMetadata(BatchProblemRequest.UnifiedStatisticsRequest request, Map<String, Object> rawData) {
        // 创建 StatisticsMetadata 对象
        UnifiedStatisticsVTO.StatisticsMetadata metadata = new UnifiedStatisticsVTO.StatisticsMetadata();

        // 设置基本统计信息
        metadata.setTotalCount(rawData.size() > 0 ? Long.valueOf(rawData.size()) : 0L);

        // 设置分页信息
        if (request.getPageNum() != null && request.getPageSize() != null) {
            metadata.setCurrentPage(request.getPageNum());
            metadata.setPageSize(request.getPageSize());

            // 计算总页数
            long totalItems = metadata.getTotalCount();
            int pageSize = request.getPageSize();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            metadata.setTotalPages(totalPages);
        }

        // 设置数据来源
        metadata.setDataSource("Problem Statistics Manager");

        // 设置附加信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("scope", request.getScope());

        // 添加过滤条件
        if (request.getDifficulties() != null) {
            additionalInfo.put("difficulty", request.getDifficulties());
        }
        if (request.getProblemTypes() != null) {
            additionalInfo.put("problemType", request.getProblemTypes());
        }
        if (request.getStatuses() != null) {
            additionalInfo.put("status", request.getStatuses());
        }

        // 添加日期范围
        if (request.getStartDate() != null) {
            additionalInfo.put("startDate", request.getStartDate());
        }
        if (request.getEndDate() != null) {
            additionalInfo.put("endDate", request.getEndDate());
        }

        metadata.setAdditionalInfo(additionalInfo);

        return metadata;
    }

    /**
     * 构建统计元数据信息
     *
     * @param request 统计请求
     * @param rawData 原始数据列表
     * @return 元数据对象
     */
    private UnifiedStatisticsVTO.StatisticsMetadata buildMetadata(BatchProblemRequest.UnifiedStatisticsRequest request,
                                                                  List<HashMap<String, Object>> rawData) {
        // 创建 StatisticsMetadata 对象
        UnifiedStatisticsVTO.StatisticsMetadata metadata = new UnifiedStatisticsVTO.StatisticsMetadata();

        // 设置基本统计信息
        metadata.setTotalCount(rawData != null ? (long) rawData.size() : 0L);

        // 设置分页信息
        if (request.getPageNum() != null && request.getPageSize() != null) {
            metadata.setCurrentPage(request.getPageNum());
            metadata.setPageSize(request.getPageSize());

            // 计算总页数
            long totalItems = metadata.getTotalCount();
            int pageSize = request.getPageSize();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            metadata.setTotalPages(totalPages);
        }

        // 设置数据来源
        metadata.setDataSource("Problem Statistics Manager");

        // 设置附加信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("scope", request.getScope());

        // 添加过滤条件
        if (request.getDifficulties() != null) {
            additionalInfo.put("difficulty", request.getDifficulties());
        }
        if (request.getProblemTypes() != null) {
            additionalInfo.put("problemType", request.getProblemTypes());
        }
        if (request.getStatuses() != null) {
            additionalInfo.put("status", request.getStatuses());
        }

        // 添加日期范围
        if (request.getStartDate() != null) {
            additionalInfo.put("startDate", request.getStartDate());
        }
        if (request.getEndDate() != null) {
            additionalInfo.put("endDate", request.getEndDate());
        }

        metadata.setAdditionalInfo(additionalInfo);

        return metadata;
    }

}
