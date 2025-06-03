package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.controller.request.BatchProblemRequest;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.manager.ProblemCoreManager;
import fun.timu.oj.judge.manager.ProblemBatchManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import fun.timu.oj.judge.model.Enums.RankingType;
import fun.timu.oj.judge.model.Enums.RecommendationType;
import fun.timu.oj.judge.model.Enums.TrendType;
import fun.timu.oj.judge.model.criteria.*;
import fun.timu.oj.judge.model.Enums.DistributionDimension;
import fun.timu.oj.judge.model.enums.StatisticsScope;
import fun.timu.oj.judge.model.VTO.UnifiedStatisticsVTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemManagerImpl implements ProblemManager {
    private final ProblemMapper problemMapper;
    private final ProblemCoreManager problemCoreManager;
    private final ProblemBatchManager problemBatchManager;

    /**
     * 根据id查询题目
     *
     * @param id 题目id
     * @return 题目
     */
    @Override
    public ProblemDO getById(Long id) {
        return problemCoreManager.getById(id);
    }


    /**
     * 根据多个筛选条件分页查询题目列表
     *
     * @param current            当前页码
     * @param size               每页大小
     * @param problemType        题目类型
     * @param difficulty         难度
     * @param status             状态
     * @param supportedLanguages 支持的编程语言列表
     * @param hasInput           是否有输入
     * @param MinAcceptanceRate  最小通过率
     * @param MaxAcceptanceRate  最大通过率
     * @return 分页的题目列表
     */
    @Override
    public IPage<ProblemDO> findTagListWithPage(int current, int size, String problemType, Integer difficulty, Integer status, Integer visibility, List<String> supportedLanguages, Boolean hasInput, Double MinAcceptanceRate, Double MaxAcceptanceRate) {
        return problemCoreManager.findTagListWithPage(current, size, problemType, difficulty, status, visibility, supportedLanguages, hasInput, MinAcceptanceRate, MaxAcceptanceRate);
    }


    /**
     * 根据创建者ID查询题目列表
     * <p>
     * 此方法旨在通过创建者ID筛选出未删除的题目，并按照创建时间降序排列
     * 选择使用LambdaQueryWrapper是为了提高查询条件编写的可读性和维护性
     *
     * @param creatorId 创建者ID，用于筛选题目的创建者
     * @return 返回由创建者创建的、未删除的题目列表如果creatorId为null，则返回空列表
     */
    @Override
    public List<ProblemDO> findByCreatorId(Long creatorId) {
        return problemCoreManager.findByCreatorId(creatorId);
    }

    /**
     * 保存题目信息到数据库中
     * <p>
     * 此方法负责将一个题目数据对象（ProblemDO）插入到数据库中它主要用于题目的创建或更新操作
     * 通过调用problemMapper的insert方法来实现数据的插入功能
     *
     * @param problemDO 题目数据对象，包含需要保存的题目信息
     * @return 插入操作的结果，通常是一个表示受影响行数的整数
     */
    @Override
    public int save(ProblemDO problemDO) {
        return problemCoreManager.save(problemDO);
    }

    /**
     * 根据ID更新题目信息
     *
     * @param problemDO 包含要更新的题目信息的对象
     * @return 更新操作的结果，返回影响的行数
     */
    @Override
    public int updateById(ProblemDO problemDO) {
        return problemCoreManager.updateById(problemDO);
    }

    /**
     * 根据ID删除题目
     * 实际上，这个方法通过将题目标记为已删除来实现软删除它并不真正从数据库中删除记录
     * 软删除是一种常见的做法，可以保持数据的完整性，避免真正删除后无法恢复
     *
     * @param id 题目的唯一标识符
     *           这个参数用于标识数据库中的特定题目记录
     * @return 影响的行数
     * 这个返回值表示更新操作影响的数据库行数如果更新成功，返回1；否则，返回0
     */
    @Override
    public int deleteById(Long id) {
        return problemCoreManager.deleteById(id);
    }

    /**
     * 检查指定标题的题目是否已存在
     *
     * @param title 题目标题
     * @return 如果存在未删除的同名题目返回true，否则返回false
     */
    @Override
    public boolean existsByTitle(String title) {
        return problemCoreManager.existsByTitle(title);
    }

    /**
     * 更新题目提交统计信息
     *
     * @param problemId  题目ID
     * @param isAccepted 提交是否被接受
     * @return 更新操作的影响行数
     */
    @Override
    public int updateSubmissionStats(Long problemId, LoginUser loginUser, boolean isAccepted) {
        return problemCoreManager.updateSubmissionStats(problemId, loginUser, isAccepted);
    }


    /**
     * 选择热门题目
     * <p>
     * 根据题目类型、难度和限制数量选择热门题目如果没有指定限制或限制无效，则使用默认限制
     *
     * @param problemType 题目类型，用于过滤特定类型的题目
     * @param difficulty  题目难度，用于过滤特定难度的题目
     * @param limit       返回的题目数量限制如果为null或小于等于0，则使用默认值10
     * @return 包含热门题目的列表
     */
    @Override
    public List<ProblemDO> selectHotProblems(String problemType, Integer difficulty, Integer limit) {
        // 如果limit为null或小于等于0，设置默认值为10
        if (limit == null || limit <= 0) limit = 10;

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 未删除的题目
        queryWrapper.eq(ProblemDO::getIsDeleted, 0);

        // 状态为激活的题目
        queryWrapper.eq(ProblemDO::getStatus, 1);

        // 提交次数大于0的题目
        queryWrapper.gt(ProblemDO::getSubmissionCount, 0);

        // 可选的题目类型筛选
        if (problemType != null && !problemType.isEmpty()) {
            queryWrapper.eq(ProblemDO::getProblemType, problemType);
        }

        // 可选的难度筛选
        if (difficulty != null) {
            queryWrapper.eq(ProblemDO::getDifficulty, difficulty);
        }

        // 按提交次数降序排序，如果提交次数相同则按通过次数降序排序
        queryWrapper.orderByDesc(ProblemDO::getSubmissionCount, ProblemDO::getAcceptedCount);

        // 限制返回的结果数量
        Page<ProblemDO> page = new Page<>(1, limit);

        // 执行查询并返回结果
        IPage<ProblemDO> pageResult = problemMapper.selectPage(page, queryWrapper);

        return pageResult.getRecords();
    }

    /**
     * 查询推荐题目（通过率适中的题目）
     * 此方法覆盖了默认的行为，以提供更具体的实现
     *
     * @param minAcceptanceRate 最小接受率，用于过滤问题
     * @param maxAcceptanceRate 最大接受率，用于过滤问题
     * @param difficulty        问题的难度级别
     * @param limit             返回问题的数量限制
     * @return 返回一个包含推荐问题的列表
     */
    @Override
    public List<ProblemDO> selectRecommendedProblems(Double minAcceptanceRate, Double maxAcceptanceRate, Integer difficulty, Integer limit) {
        try {
            log.info("开始获取推荐题目（使用统一接口）");

            // 参数校验和默认值设置
            if (limit == null || limit <= 0) {
                limit = 10;
            }
            if (minAcceptanceRate == null) {
                minAcceptanceRate = 0.0;
            }
            if (maxAcceptanceRate == null) {
                maxAcceptanceRate = 1.0;
            }

            // 使用统一推荐接口替代旧的实现
            RecommendationCriteria criteria = RecommendationCriteria.builder()
                    .type(RecommendationType.ACCEPTANCE_RATE)
                    .difficulty(difficulty)
                    .minAcceptanceRate(minAcceptanceRate)
                    .maxAcceptanceRate(maxAcceptanceRate)
                    .limit(limit)
                    .build();

            return getRecommendedProblems(criteria);
        } catch (Exception e) {
            log.error("获取推荐题目失败", e);
            throw new RuntimeException("获取推荐题目失败", e);
        }
    }

    /**
     * 获取题目统计信息
     * 该方法返回按题目类型和难度分组的统计数据，包括总题目数量、活跃题目数量、
     * 总提交次数、总通过次数以及平均通过率等信息
     *
     * @return 统计信息列表
     */
    @Override
    public List<ProblemStatisticsDTO> getProblemStatistics() {
        try {
            log.info("开始获取题目统计信息（使用统一接口）");

            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.BASIC)
                    .build();

            Map<String, Object> rawData = getUnifiedStatisticsRaw(request);

            // 转换数据格式以保持向后兼容
            List<Map<String, Object>> statisticsList = (List<Map<String, Object>>) rawData.get("categoryStatistics");
            if (statisticsList == null) {
                return Collections.emptyList();
            }

            return statisticsList.stream()
                    .map(ProblemStatisticsDTO::fromMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取题目统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目统计信息失败", e);
        }
    }

    /**
     * 批量更新题目状态
     * <p>
     * 此方法用于一次性更新多个题目的状态，提高操作效率
     *
     * @param problemIds 题目ID列表
     * @param status     要更新的状态值
     * @return 更新的记录数
     */
    @Override
    public int batchUpdateStatus(List<Long> problemIds, Integer status) {
        return problemBatchManager.batchUpdateStatus(problemIds, status);
    }

    /**
     * 根据创建者ID统计问题数量
     * <p>
     * 此方法用于统计由特定用户创建的问题总数它首先检查传入的用户ID是否有效，
     * 如果无效，则抛出运行时异常如果用户ID有效，它将调用problemMapper中的相应方法
     * 来统计该创建者创建的问题数量
     *
     * @param creatorId 创建者ID，用于标识问题的创建者
     * @return 创建者创建的问题数量如果用户ID无效，将抛出异常而不是返回值
     * @throws RuntimeException 如果用户ID无效（null、负数或零），则抛出此异常
     */
    @Override
    public Long countByCreator(Long creatorId) {
        return problemCoreManager.countByCreator(creatorId);
    }


    /**
     * 选择最近创建的题目
     *
     * @param pageNum  页码，表示请求的数据位于第几页
     * @param pageSize 每页大小，表示每页包含的数据条数
     * @param limit    限制数量，如果指定，则只返回指定数量的数据，通常用于获取最新的一批数据
     * @return 返回一个分页对象，包含查询到的题目数据
     * <p>
     * 此方法用于查询系统中最近创建的题目，可以根据是否有limit参数来决定查询方式
     * 如果limit参数存在且大于0，则使用单页查询方式，直接返回指定数量的最新题目；
     * 否则，使用常规分页方式查询
     */
    @Override
    public IPage<ProblemDO> selectRecentProblems(int pageNum, int pageSize, Integer limit) {
        return problemCoreManager.selectRecentProblems(pageNum, pageSize, limit);
    }

    /**
     * 根据支持的编程语言查询题目
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param language 编程语言
     * @return 包含题目列表的分页结果
     */
    @Override
    public IPage<ProblemDO> selectByLanguage(int pageNum, int pageSize, String language) {
        return problemCoreManager.selectByLanguage(pageNum, pageSize, language);
    }

    /**
     * 批量软删除题目
     *
     * @param problemIds 题目ID列表，用于指定要删除的题目
     * @return 返回成功删除的题目数量
     */
    @Override
    public int batchSoftDelete(List<Long> problemIds) {
        return problemBatchManager.batchSoftDelete(problemIds);
    }

    /**
     * 批量恢复删除的题目
     *
     * @param problemIds 需要恢复的题目ID列表
     * @return 影响的行数，表示成功恢复的题目数量
     */
    @Override
    public int batchRestore(List<Long> problemIds) {
        return problemBatchManager.batchRestore(problemIds);
    }

    /**
     * 获取题目通过率
     *
     * @param problemId 题目ID，用于识别特定的题目
     * @return 返回题目的通过率，如果题目不存在或从未被提交过，则返回0.0
     * @throws RuntimeException 如果题目ID无效，抛出运行时异常
     */
    @Override
    public Double getAcceptanceRate(Long problemId) {
        return problemCoreManager.getAcceptanceRate(problemId);
    }

    /**
     * 根据问题ID列表选择基本信息
     * <p>
     * 此方法用于批量获取问题的基本信息，仅返回指定字段和未删除的问题
     * 它首先验证输入的ID列表，然后构建查询条件，最后执行数据库查询并按输入ID顺序返回结果
     *
     * @param problemIds 问题ID列表，用于指定需要查询的问题
     * @return 返回包含问题基本信息的列表，如果找不到任何问题，则返回空列表
     */
    @Override
    public List<ProblemDO> selectBasicInfoByIds(List<Long> problemIds) {
        return problemCoreManager.selectBasicInfoByIds(problemIds);
    }

    /**
     * 获取题目详细统计信息
     * 该方法通过调用Mapper层获取包含题目各维度的统计数据
     *
     * @return 包含详细统计数据的HashMap
     */
    @Override
    public ProblemDetailStatisticsDTO getProblemDetailStatistics() {
        try {
            log.info("开始获取题目详细统计信息（使用统一接口）");

            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.DETAILED)
                    .build();

            Map<String, Object> rawData = getUnifiedStatisticsRaw(request);
            if (rawData == null || rawData.isEmpty()) {
                throw new RuntimeException("获取题目详细统计信息失败");
            }

            return ProblemDetailStatisticsDTO.fromMap(rawData);
        } catch (Exception e) {
            log.error("获取题目详细统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目详细统计信息失败", e);
        }
    }

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    @Override
    public List<PopularProblemCategoryDTO> getPopularProblemCategories(Integer limit) {

        List<HashMap<String, Object>> result = problemMapper.getPopularProblemCategories(limit);

        // 处理结果，添加难度和类型的文本描述
        for (Map<String, Object> item : result) {
            Integer difficulty = (Integer) item.get("difficulty");

            // 添加难度文本描述
            item.put("difficulty_label", ProblemDifficultyEnum.getDescriptionByCode(difficulty));
        }
        return PopularProblemCategoryDTO.fromMapList(result);
    }

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
    @Override
    public IPage<ProblemDO> selectByDateRange(Date startDate, Date endDate, Integer status, int pageNum, int pageSize) {
        return problemCoreManager.selectByDateRange(startDate, endDate, status, pageNum, pageSize);
    }

    /**
     * 查询相似题目（基于标签和难度）
     *
     * @param problemId   题目ID
     * @param difficulty  难度限制
     * @param problemType 题目类型限制
     * @param limit       返回数量限制
     * @return 相似题目列表
     */
    @Override
    public List<ProblemDO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit) {
        try {
            log.info("开始查找相似题目（使用统一推荐接口）, 目标题目ID: {}, 难度: {}, 类型: {}, 限制: {}",
                    problemId, difficulty, problemType, limit);

            // 参数校验
            if (problemId == null || problemId <= 0) {
                return Collections.emptyList();
            }

            // 设置默认限制
            if (limit == null || limit <= 0) {
                limit = 10; // 默认返回10条记录
            }

            // 使用统一推荐接口替代旧的实现
            RecommendationCriteria criteria = RecommendationCriteria.builder()
                    .type(RecommendationType.SIMILARITY)
                    .baseProblemId(problemId)
                    .difficulty(difficulty)
                    .problemType(problemType)
                    .limit(limit)
                    .build();

            List<ProblemDO> result = getRecommendedProblems(criteria);
            log.info("查找相似题目完成（使用统一推荐接口）, 返回数量: {}", result.size());

            return result;
        } catch (Exception e) {
            log.error("查找相似题目失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量更新题目可见性
     *
     * @param problemIds 题目ID列表
     * @param visibility 可见性值
     * @return 更新的记录数
     */
    @Override
    public int batchUpdateVisibility(List<Long> problemIds, Integer visibility) {
        return problemBatchManager.batchUpdateVisibility(problemIds, visibility);
    }

    /**
     * 批量更新题目的时间和内存限制
     *
     * @param problemIds  需要更新的题目ID列表
     * @param timeLimit   新的时间限制值（毫秒）
     * @param memoryLimit 新的内存限制值（MB）
     * @return 成功更新的记录数
     */
    @Override
    public int batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit) {
        return problemBatchManager.batchUpdateLimits(problemIds, timeLimit, memoryLimit);
    }

    /**
     * 重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 需要重置统计数据的题目ID列表
     * @return 更新成功的记录数
     */
    @Override
    public int batchResetStats(List<Long> problemIds) {
        return problemBatchManager.batchResetStats(problemIds);
    }

    /**
     * 查询长时间未更新的题目
     *
     * @param lastUpdateBefore 上次更新时间早于此日期的题目将被视为陈旧题目
     * @param pageNum          页码（从1开始）
     * @param pageSize         每页大小
     * @return 分页结果，包含符合条件的题目列表
     */
    @Override
    public IPage<ProblemDO> selectStaleProblems(Date lastUpdateBefore, int pageNum, int pageSize) {
        return problemCoreManager.selectStaleProblems(lastUpdateBefore, pageNum, pageSize);
    }

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public IPage<ProblemDO> selectProblemsWithoutSubmissions(int pageNum, int pageSize) {
        return problemCoreManager.selectProblemsWithoutSubmissions(pageNum, pageSize);
    }

    /**
     * 获取题目总体统计信息（增强版）
     *
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getOverallStatistics() {
        try {
            log.info("开始获取总体统计信息（使用统一接口）");

            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.OVERALL)
                    .build();

            return getUnifiedStatisticsRaw(request);
        } catch (Exception e) {
            log.error("获取总体统计信息失败", e);
            throw new RuntimeException("获取总体统计信息失败", e);
        }
    }

    /**
     * 按难度获取统计信息
     *
     * @return 难度统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByDifficulty() {
        try {
            log.info("开始获取按难度统计信息（使用统一接口）");

            // 使用统一分布统计接口替代旧的实现
            DistributionCriteria criteria = DistributionCriteria.builder()
                    .dimension(DistributionDimension.DIFFICULTY)
                    .build();

            return getDistributionStatistics(criteria);
        } catch (Exception e) {
            log.error("按难度获取统计信息失败", e);
            throw new RuntimeException("按难度获取统计信息失败", e);
        }
    }

    /**
     * 按类型获取统计信息
     *
     * @return 类型统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByType() {
        try {
            log.info("开始获取按类型统计信息（使用统一接口）");

            // 使用统一分布统计接口替代旧的实现
            DistributionCriteria criteria = DistributionCriteria.builder()
                    .dimension(DistributionDimension.TYPE)
                    .build();

            return getDistributionStatistics(criteria);
        } catch (Exception e) {
            log.error("按类型获取统计信息失败", e);
            throw new RuntimeException("按类型获取统计信息失败", e);
        }
    }

    /**
     * 按语言获取统计信息
     *
     * @return 语言统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByLanguage() {
        try {
            log.info("开始获取按语言统计信息（使用统一接口）");

            // 使用统一分布统计接口替代旧的实现
            DistributionCriteria criteria = DistributionCriteria.builder()
                    .dimension(DistributionDimension.LANGUAGE)
                    .build();

            return getDistributionStatistics(criteria);
        } catch (Exception e) {
            log.error("按语言获取统计信息失败", e);
            throw new RuntimeException("按语言获取统计信息失败", e);
        }
    }

    /**
     * 按状态获取统计信息
     *
     * @return 状态统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByStatus() {
        try {
            log.info("开始获取按状态统计信息（使用统一接口）");

            // 使用统一分布统计接口替代旧的实现
            DistributionCriteria criteria = DistributionCriteria.builder()
                    .dimension(DistributionDimension.STATUS)
                    .build();

            return getDistributionStatistics(criteria);
        } catch (Exception e) {
            log.error("按状态获取统计信息失败", e);
            throw new RuntimeException("按状态获取统计信息失败", e);
        }
    }

    /**
     * 获取题目创建趋势
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 创建趋势数据
     */
    @Override
    public List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity) {
        try {
            log.info("开始获取题目创建趋势（使用统一接口）");

            // 使用统一接口替代旧的实现
            TrendCriteria criteria = TrendCriteria.builder()
                    .type(TrendType.PROBLEM_CREATION)
                    .startTime(startDate)
                    .endTime(endDate)
                    .timeGranularity(granularity)
                    .build();

            return getTrendAnalysis(criteria);
        } catch (Exception e) {
            log.error("获取题目创建趋势失败", e);
            throw new RuntimeException("获取题目创建趋势失败", e);
        }
    }

    /**
     * 获取提交趋势分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 提交趋势数据
     */
    @Override
    public List<Map<String, Object>> getSubmissionTrendAnalysis(Date startDate, Date endDate, String granularity) {
        try {
            log.info("开始获取提交趋势分析（使用统一接口）");

            // 使用统一接口替代旧的实现
            TrendCriteria criteria = TrendCriteria.builder()
                    .type(TrendType.SUBMISSION_TREND)
                    .startTime(startDate)
                    .endTime(endDate)
                    .timeGranularity(granularity)
                    .build();

            return getTrendAnalysis(criteria);
        } catch (Exception e) {
            log.error("获取提交趋势分析失败", e);
            throw new RuntimeException("获取提交趋势分析失败", e);
        }
    }

    /**
     * 获取通过率趋势分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 通过率趋势数据
     */
    @Override
    public List<Map<String, Object>> getAcceptanceRateTrend(Date startDate, Date endDate, String granularity) {
        try {
            log.info("开始获取通过率趋势分析（使用统一接口）");

            // 使用统一接口替代旧的实现
            TrendCriteria criteria = TrendCriteria.builder()
                    .type(TrendType.ACCEPTANCE_RATE_TREND)
                    .startTime(startDate)
                    .endTime(endDate)
                    .timeGranularity(granularity)
                    .build();

            return getTrendAnalysis(criteria);
        } catch (Exception e) {
            log.error("获取通过率趋势分析失败", e);
            throw new RuntimeException("获取通过率趋势分析失败", e);
        }
    }

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
            log.info("开始获取热门题目排行榜（使用统一接口）");

            // 使用统一排行榜接口替代旧的实现
            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.POPULARITY)
                    .limit(limit != null ? limit : 10)
                    .timeRange(timeRange)
                    .build();

            return getProblemRanking(criteria);
        } catch (Exception e) {
            log.error("获取热门题目排行榜失败", e);
            throw new RuntimeException("获取热门题目排行榜失败", e);
        }
    }

    /**
     * 获取最难题目排行榜
     *
     * @param limit          限制数量
     * @param minSubmissions 最小提交数
     * @return 最难题目排行榜
     */
    @Override
    public List<Map<String, Object>> getHardestProblemsRanking(Integer limit, Integer minSubmissions) {
        try {
            log.info("开始获取最难题目排行榜（使用统一接口）");

            // 使用统一排行榜接口替代旧的实现
            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.HARDEST)
                    .limit(limit != null ? limit : 10)
                    .minSubmissions(minSubmissions)
                    .build();

            return getProblemRanking(criteria);
        } catch (Exception e) {
            log.error("获取最难题目排行榜失败", e);
            throw new RuntimeException("获取最难题目排行榜失败", e);
        }
    }

    /**
     * 获取创建者贡献排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 创建者贡献排行榜
     */
    @Override
    public List<Map<String, Object>> getCreatorContributionRanking(Integer limit, Integer timeRange) {
        try {
            log.info("开始获取创建者贡献排行榜（使用统一接口）");

            // 使用统一排行榜接口替代旧的实现
            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.CREATOR_CONTRIBUTION)
                    .limit(limit != null ? limit : 10)
                    .timeRange(timeRange)
                    .build();

            return getProblemRanking(criteria);
        } catch (Exception e) {
            log.error("获取创建者贡献排行榜失败", e);
            throw new RuntimeException("获取创建者贡献排行榜失败", e);
        }
    }

    /**
     * 获取题目质量排行榜
     *
     * @param limit 限制数量
     * @return 质量排行榜
     */
    @Override
    public List<Map<String, Object>> getQualityProblemsRanking(Integer limit) {
        try {
            log.info("开始获取题目质量排行榜（使用统一接口）");

            // 使用统一排行榜接口替代旧的实现
            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.QUALITY)
                    .limit(limit != null ? limit : 10)
                    .build();

            return getProblemRanking(criteria);
        } catch (Exception e) {
            log.error("获取高质量题目排名失败", e);
            throw new RuntimeException("获取高质量题目排名失败", e);
        }
    }

    /**
     * 获取难度-类型分布矩阵
     *
     * @return 分布矩阵数据
     */
    @Override
    public List<Map<String, Object>> getDifficultyTypeDistribution() {
        try {
            List<HashMap<String, Object>> result = problemMapper.getDifficultyTypeDistribution();
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取难度类型分布失败", e);
            throw new RuntimeException("获取难度类型分布失败", e);
        }
    }

    /**
     * 获取通过率分布统计
     *
     * @param bucketSize 区间大小
     * @return 通过率分布数据
     */
    @Override
    public List<Map<String, Object>> getAcceptanceRateDistribution(Double bucketSize) {
        try {
            List<HashMap<String, Object>> result = problemMapper.getAcceptanceRateDistribution(bucketSize);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取通过率分布失败", e);
            throw new RuntimeException("获取通过率分布失败", e);
        }
    }

    /**
     * 获取提交量分布统计
     *
     * @return 提交量分布数据
     */
    @Override
    public List<Map<String, Object>> getSubmissionCountDistribution() {
        try {
            List<HashMap<String, Object>> result = problemMapper.getSubmissionCountDistribution();
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取提交量分布失败", e);
            throw new RuntimeException("获取提交量分布失败", e);
        }
    }

    /**
     * 获取题目综合健康度报告
     *
     * @return 健康度报告
     */
    @Override
    public Map<String, Object> getProblemHealthReport() {
        try {
            return problemMapper.getProblemHealthReport();
        } catch (Exception e) {
            log.error("获取题目健康度报告失败", e);
            throw new RuntimeException("获取题目健康度报告失败", e);
        }
    }

    /**
     * 获取平台数据大屏统计
     *
     * @return 大屏统计数据
     */
    @Override
    public Map<String, Object> getDashboardStatistics() {
        try {
            log.info("开始获取仪表盘统计数据（使用统一接口）");

            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.DASHBOARD)
                    .build();

            return getUnifiedStatisticsRaw(request);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            throw new RuntimeException("获取仪表盘统计数据失败", e);
        }
    }

    /**
     * 获取题目推荐数据
     *
     * @param difficulty  难度偏好
     * @param problemType 类型偏好
     * @param limit       推荐数量
     * @return 推荐数据
     */
    @Override
    public List<Map<String, Object>> getRecommendationData(Integer difficulty, String problemType, Integer limit) {
        try {
            log.info("开始获取题目推荐数据（使用统一接口）");

            // 使用统一推荐接口替代旧的实现
            RecommendationCriteria criteria = RecommendationCriteria.builder()
                    .type(RecommendationType.ALGORITHM_DATA)
                    .difficulty(difficulty)
                    .problemType(problemType)
                    .limit(limit != null ? limit : 10)
                    .build();

            return getRecommendedProblemsWithScore(criteria);
        } catch (Exception e) {
            log.error("获取题目推荐数据失败", e);
            throw new RuntimeException("获取题目推荐数据失败", e);
        }
    }


    /**
     * 获取题目相关性分析
     *
     * @param problemId 题目ID
     * @param limit     相关题目数量
     * @return 相关性分析数据
     */
    @Override
    public List<Map<String, Object>> getProblemCorrelationAnalysis(Long problemId, Integer limit) {
        try {
            List<HashMap<String, Object>> result = problemMapper.getProblemCorrelationAnalysis(problemId, limit);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取题目相关性分析失败", e);
            throw new RuntimeException("获取题目相关性分析失败", e);
        }
    }

    /**
     * 获取平台增长指标
     *
     * @param timeRange 时间范围
     * @return 增长指标数据
     */
    @Override
    public Map<String, Object> getPlatformGrowthMetrics(Integer timeRange) {
        try {
            return problemMapper.getPlatformGrowthMetrics(timeRange);
        } catch (Exception e) {
            log.error("获取平台增长指标失败", e);
            throw new RuntimeException("获取平台增长指标失败", e);
        }
    }

    /**
     * 获取月度报表
     *
     * @param year  年份
     * @param month 月份
     * @return 月度报表数据
     */
    @Override
    public Map<String, Object> getMonthlyReport(Integer year, Integer month) {
        try {
            return problemMapper.getMonthlyReport(year, month);
        } catch (Exception e) {
            log.error("获取月度报表失败", e);
            throw new RuntimeException("获取月度报表失败", e);
        }
    }

    /**
     * 获取年度报表
     *
     * @param year 年份
     * @return 年度报表数据
     */
    @Override
    public Map<String, Object> getAnnualReport(Integer year) {
        try {
            return problemMapper.getAnnualReport(year);
        } catch (Exception e) {
            log.error("获取年度报表失败", e);
            throw new RuntimeException("获取年度报表失败", e);
        }
    }

    /**
     * 获取自定义时间范围报表
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param metrics   指标列表
     * @return 自定义报表数据
     */
    @Override
    public Map<String, Object> getCustomRangeReport(Date startDate, Date endDate, List<String> metrics) {
        try {
            return problemMapper.getCustomRangeReport(startDate, endDate, metrics);
        } catch (Exception e) {
            log.error("获取自定义范围报表失败", e);
            throw new RuntimeException("获取自定义范围报表失败", e);
        }
    }

    /**
     * 获取实时题目状态监控
     *
     * @return 实时状态数据
     */
    @Override
    public Map<String, Object> getRealTimeProblemStatus() {
        try {
            return problemMapper.getRealTimeProblemStatus();
        } catch (Exception e) {
            log.error("获取实时题目状态监控失败", e);
            throw new RuntimeException("获取实时题目状态监控失败", e);
        }
    }

    /**
     * 获取实时提交监控
     *
     * @param timeWindow 时间窗口
     * @return 实时提交监控数据
     */
    @Override
    public Map<String, Object> getRealTimeSubmissionMonitoring(Integer timeWindow) {
        try {
            return problemMapper.getRealTimeSubmissionMonitoring(timeWindow);
        } catch (Exception e) {
            log.error("获取实时提交监控失败", e);
            throw new RuntimeException("获取实时提交监控失败", e);
        }
    }


    /**
     * 统一推荐方法
     * <p>
     * 此方法根据给定的推荐标准（RecommendationCriteria）返回题目推荐列表
     * 支持根据难度、题目类型、标签、创建者等多种条件进行灵活组合查询
     *
     * @param criteria 推荐标准，包含多种筛选条件
     * @return 符合条件的题目列表
     */
    @Override
    public List<ProblemDO> recommendProblems(RecommendationCriteria criteria) {
        try {
            LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

            // 只查询未删除的题目
            queryWrapper.eq(ProblemDO::getIsDeleted, false);

            // 按照创建时间降序排序
            queryWrapper.orderByDesc(ProblemDO::getCreatedAt);

            // 根据难度筛选
            if (criteria.getDifficulty() != null) {
                queryWrapper.eq(ProblemDO::getDifficulty, criteria.getDifficulty());
            }

            // 根据题目类型筛选
            if (criteria.getProblemType() != null && !criteria.getProblemType().isEmpty()) {
                queryWrapper.eq(ProblemDO::getProblemType, criteria.getProblemType());
            }

            // 根据标签筛选（假设标签存储在JSON字段中）
            if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
                StringBuilder tagCondition = new StringBuilder("(");
                for (int i = 0; i < criteria.getTags().size(); i++) {
                    if (i > 0) tagCondition.append(" AND ");
                    tagCondition.append("JSON_CONTAINS(tags, JSON_QUOTE('").append(criteria.getTags().get(i)).append("'))");
                }
                tagCondition.append(")");
                queryWrapper.apply(tagCondition.toString());
            }

            // 根据创建者ID筛选
            if (criteria.getCreatorId() != null) {
                queryWrapper.eq(ProblemDO::getCreatorId, criteria.getCreatorId());
            }

            // 根据可见性筛选
            if (criteria.getVisibility() != null) {
                queryWrapper.eq(ProblemDO::getVisibility, criteria.getVisibility());
            }

            // 根据状态筛选
            if (criteria.getStatus() != null) {
                queryWrapper.eq(ProblemDO::getStatus, criteria.getStatus());
            }

            // 执行查询
            return problemMapper.selectList(queryWrapper);

        } catch (Exception e) {
            log.error("推荐题目时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("推荐题目时发生错误", e);
        }
    }

    /**
     * 统一的推荐题目接口实现
     * 支持多种推荐算法：通过率推荐、相似性推荐、热门推荐、算法数据推荐
     *
     * @param criteria 推荐条件
     * @return 推荐题目列表
     */
    @Override
    public List<ProblemDO> getRecommendedProblems(RecommendationCriteria criteria) {
        try {
            log.info("Manager层获取推荐题目，推荐类型: {}, 条件: {}",
                    criteria.getType(), criteria);

            // 参数校验
            if (criteria == null) {
                throw new IllegalArgumentException("推荐条件不能为空");
            }

            // 调用Mapper层统一推荐接口
            List<ProblemDO> result = problemMapper.getRecommendedProblems(criteria);

            log.info("Manager层获取推荐题目成功，推荐类型: {}, 结果数量: {}",
                    criteria.getType(), result.size());

            return result;
        } catch (Exception e) {
            log.error("Manager层获取推荐题目失败，推荐类型: {}, 错误: {}",
                    criteria != null ? criteria.getType() : "unknown", e.getMessage(), e);
            throw new RuntimeException("获取推荐题目失败", e);
        }
    }

    /**
     * 统一的推荐算法数据接口实现
     * 返回包含推荐评分等详细信息的数据
     *
     * @param criteria 推荐条件
     * @return 推荐数据列表（包含评分信息）
     */
    @Override
    public List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria) {
        try {
            log.info("Manager层获取推荐题目数据（含评分），推荐类型: {}, 条件: {}",
                    criteria.getType(), criteria);

            // 参数校验
            if (criteria == null) {
                throw new IllegalArgumentException("推荐条件不能为空");
            }

            // 调用Mapper层统一推荐接口
            List<HashMap<String, Object>> result = problemMapper.getRecommendedProblemsWithScore(criteria);

            // 转换为标准Map接口
            List<Map<String, Object>> finalResult = result.stream()
                    .map(map -> (Map<String, Object>) map)
                    .collect(Collectors.toList());

            log.info("Manager层获取推荐题目数据（含评分）成功，推荐类型: {}, 结果数量: {}",
                    criteria.getType(), finalResult.size());

            return finalResult;
        } catch (Exception e) {
            log.error("Manager层获取推荐题目数据（含评分）失败，推荐类型: {}, 错误: {}",
                    criteria != null ? criteria.getType() : "unknown", e.getMessage(), e);
            throw new RuntimeException("获取推荐题目数据失败", e);
        }
    }

    /**
     * 获取统一统计信息（结构化响应）
     * 使用统一的接口替代多个冗余的统计方法，支持不同范围和过滤条件
     *
     * @param request 统一统计请求，包含范围、过滤条件等参数
     * @return 结构化的统计响应，包含元数据和版本信息
     */
    @Override
    public UnifiedStatisticsVTO getUnifiedStatistics(BatchProblemRequest.UnifiedStatisticsRequest request) {
        try {
            log.info("Manager层获取统一统计信息，范围: {}, 过滤条件: {}",
                    request.getScope(), request);

            // 参数校验
            if (request == null) {
                throw new IllegalArgumentException("统计请求参数不能为空");
            }
            if (request.getScope() == null) {
                throw new IllegalArgumentException("统计范围不能为空");
            }

            // 调用Mapper层获取原始统计数据
            Map<String, Object> rawData = problemMapper.getUnifiedStatisticsRaw(request);

            if (rawData == null) {
                rawData = new HashMap<>();
            }

            // 构建结构化响应
            UnifiedStatisticsVTO response = UnifiedStatisticsVTO.builder()
                    .scope(request.getScope())
                    .timestamp(LocalDateTime.now())
                    .version("1.0")
                    .data(rawData)
                    .metadata(buildMetadata(request, rawData))
                    .build();

            log.info("Manager层获取统一统计信息成功，范围: {}, 数据项数量: {}",
                    request.getScope(), rawData.size());

            return response;
        } catch (Exception e) {
            log.error("Manager层获取统一统计信息失败，范围: {}, 错误: {}",
                    request != null ? request.getScope() : "unknown", e.getMessage(), e);
            throw new RuntimeException("获取统一统计信息失败", e);
        }
    }

    /**
     * 获取统一统计信息（原始数据）
     * 直接返回Map格式的原始统计数据，适合需要简单数据格式的场景
     *
     * @param request 统一统计请求，包含范围、过滤条件等参数
     * @return 原始统计数据Map
     */
    @Override
    public Map<String, Object> getUnifiedStatisticsRaw(BatchProblemRequest.UnifiedStatisticsRequest request) {
        try {
            log.info("Manager层获取统一统计原始数据，范围: {}, 过滤条件: {}",
                    request.getScope(), request);

            // 参数校验
            if (request == null) {
                throw new IllegalArgumentException("统计请求参数不能为空");
            }
            if (request.getScope() == null) {
                throw new IllegalArgumentException("统计范围不能为空");
            }

            // 调用Mapper层获取原始统计数据
            Map<String, Object> rawData = problemMapper.getUnifiedStatisticsRaw(request);

            if (rawData == null) {
                rawData = new HashMap<>();
            }

            log.info("Manager层获取统一统计原始数据成功，范围: {}, 数据项数量: {}",
                    request.getScope(), rawData.size());

            return rawData;
        } catch (Exception e) {
            log.error("Manager层获取统一统计原始数据失败，范围: {}, 错误: {}",
                    request != null ? request.getScope() : "unknown", e.getMessage(), e);
            throw new RuntimeException("获取统一统计原始数据失败", e);
        }
    }

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

        // 设置执行时间
        metadata.setExecutionTime(System.currentTimeMillis());

        // 设置数据源
        metadata.setDataSource("problem-statistics");

        // 创建附加信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("scope", request.getScope().name());
        additionalInfo.put("hasDateFilter", request.getStartDate() != null || request.getEndDate() != null);
        additionalInfo.put("hasDifficultyFilter", request.getDifficulties() != null && !request.getDifficulties().isEmpty());
        additionalInfo.put("hasProblemTypeFilter", request.getProblemTypes() != null && !request.getProblemTypes().isEmpty());
        additionalInfo.put("hasStatusFilter", request.getStatuses() != null && !request.getStatuses().isEmpty());
        additionalInfo.put("dataItemCount", rawData.size());
        additionalInfo.put("cacheHint", "TTL_300"); // 建议缓存5分钟

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
     * 统一的排行榜接口
     * 根据不同的排行榜类型返回相应的排行榜数据
     *
     * @param criteria 排行榜条件
     * @return 排行榜数据列表
     */
    @Override
    public List<Map<String, Object>> getProblemRanking(RankingCriteria criteria) {
        try {
            log.info("获取排行榜数据，排行榜类型: {}, 限制数量: {}", criteria.getType(), criteria.getLimit());

            List<HashMap<String, Object>> result = problemMapper.getProblemRanking(criteria);
            log.info("成功获取排行榜数据，数量: {}", result.size());

            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取排行榜数据失败，排行榜类型: {}", criteria.getType(), e);
            throw new RuntimeException("获取排行榜数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 统一的排行榜接口（题目实体版本）
     * 返回ProblemDO实体列表，用于需要完整题目信息的场景
     *
     * @param criteria 排行榜条件
     * @return 题目实体列表
     */
    @Override
    public List<ProblemDO> getProblemRankingEntities(RankingCriteria criteria) {
        try {
            log.info("获取排行榜题目实体，排行榜类型: {}, 限制数量: {}", criteria.getType(), criteria.getLimit());

            List<ProblemDO> result = problemMapper.getProblemRankingEntities(criteria);
            log.info("成功获取排行榜题目实体，数量: {}", result.size());

            return result;
        } catch (Exception e) {
            log.error("获取排行榜题目实体失败，排行榜类型: {}", criteria.getType(), e);
            throw new RuntimeException("获取排行榜题目实体失败: " + e.getMessage(), e);
        }
    }

    /**
     * 统一的趋势分析接口（替代原有的4个冗余接口）
     *
     * @param criteria 趋势分析条件
     * @return 趋势数据列表
     */
    @Override
    public List<Map<String, Object>> getTrendAnalysis(TrendCriteria criteria) {
        try {
            List<HashMap<String, Object>> result = problemMapper.getTrendAnalysis(criteria);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取趋势分析失败", e);
            throw new RuntimeException("获取趋势分析失败", e);
        }
    }

    /**
     * 统一的分布统计信息接口
     * 根据不同的分布维度返回相应的统计数据
     *
     * @param criteria 分布统计条件
     * @return 分布统计数据列表
     */
    @Override
    public List<Map<String, Object>> getDistributionStatistics(DistributionCriteria criteria) {
        try {
            log.info("获取分布统计信息，维度: {}", criteria.getDimension());

            List<HashMap<String, Object>> result = problemMapper.getDistributionStatistics(criteria);

            log.info("成功获取分布统计信息，维度: {}, 数据量: {}", criteria.getDimension(), result.size());

            return result.stream()
                    .map(map -> (Map<String, Object>) map)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取分布统计信息失败，维度: {}, 错误: {}",
                    criteria.getDimension(), e.getMessage(), e);
            throw new RuntimeException("获取分布统计信息失败: " + e.getMessage(), e);
        }
    }
}
