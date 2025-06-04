package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.controller.request.BatchProblemRequest;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.manager.Problem.ProblemCoreManager;
import fun.timu.oj.judge.manager.Problem.ProblemBatchManager;
import fun.timu.oj.judge.manager.Problem.ProblemRecommendationManager;
import fun.timu.oj.judge.manager.Problem.ProblemStatisticsManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import fun.timu.oj.judge.model.Enums.*;
import fun.timu.oj.judge.model.criteria.*;
import fun.timu.oj.judge.model.VTO.UnifiedStatisticsVTO;
import fun.timu.oj.judge.utils.StatisticsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemManagerImpl implements ProblemManager {
    private final ProblemMapper problemMapper;
    private final ProblemCoreManager problemCoreManager;
    private final ProblemBatchManager problemBatchManager;
    private final ProblemRecommendationManager problemRecommendationManager;
    private final ProblemStatisticsManager problemStatisticsManager;

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
        return problemRecommendationManager.selectHotProblems(problemType, difficulty, limit);
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
        RecommendationCriteria criteria = RecommendationCriteria.builder().type(RecommendationType.ACCEPTANCE_RATE).difficulty(difficulty).minAcceptanceRate(minAcceptanceRate).maxAcceptanceRate(maxAcceptanceRate).limit(limit).build();
        return getRecommendedProblems(criteria);
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
            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.BASIC)
                    .build();

            List<HashMap<String, Object>> rawDataList = getUnifiedStatisticsRaw(request);

            // 转换数据格式以保持向后兼容
            if (rawDataList == null || rawDataList.isEmpty()) {
                return Collections.emptyList();
            }

            // 正确处理List类型，每个元素都转换为DTO
            return rawDataList.stream()
                    .map(data -> ProblemStatisticsDTO.fromMap(data))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取题目统计信息失败: " + e.getMessage(), e);
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
     * @return 包含详细统计数据的ProblemDetailStatisticsDTO
     */
    /**
     * 获取题目详细统计信息
     * 该方法通过调用Mapper层获取包含题目各维度的统计数据
     *
     * @return 包含详细统计数据的ProblemDetailStatisticsDTO列表
     */
    @Override
    public ProblemDetailStatisticsDTO getProblemDetailStatistics() {
        try {
            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.DETAILED)
                    .build();

            List<HashMap<String, Object>> rawDataList = getUnifiedStatisticsRaw(request);
            if (rawDataList == null || rawDataList.isEmpty()) {
                return null;
            }
            HashMap<String, Object> data = rawDataList.get(0);

            // 将每条原始数据转换为DTO对象
            return ProblemDetailStatisticsDTO.fromMap(data);
        } catch (Exception e) {
            throw new RuntimeException("获取题目详细统计信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 安全地从Map中获取Long值
     *
     * @param data 数据Map
     * @param key  键名
     * @return Long值，如果不存在或无法转换则返回0
     */
    private long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
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
        // 参数校验
        if (problemId == null || problemId <= 0) {
            return Collections.emptyList();
        }

        // 设置默认限制
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10条记录
        }

        // TODO 这里还需要添加: tags的逻辑 应该由Service层查询出结果传递下来或直接调用Manager层的查询tags逻辑
        // 使用统一推荐接口替代旧的实现
        RecommendationCriteria criteria = RecommendationCriteria.builder().type(RecommendationType.SIMILARITY).baseProblemId(problemId).difficulty(difficulty).problemType(problemType).limit(limit).build();


        return getRecommendedProblems(criteria);
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
            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.OVERALL)
                    .build();

            List<HashMap<String, Object>> rawDataList = getUnifiedStatisticsRaw(request);

            // 如果返回列表为空，返回空的Map
            if (rawDataList == null || rawDataList.isEmpty()) {
                return new HashMap<>();
            }

            // 对于总体统计，我们需要处理可能的多条记录
            if (rawDataList.size() == 1) {
                // 只有一条记录，直接返回
                return new HashMap<>(rawDataList.get(0));
            } else {
                // 多条记录，需要合并统计数据
                return mergeOverallStatistics(rawDataList);
            }
        } catch (Exception e) {
            throw new RuntimeException("获取总体统计信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 合并多条总体统计数据
     *
     * @param rawDataList 原始数据列表
     * @return 合并后的统计数据
     */
    private Map<String, Object> mergeOverallStatistics(List<HashMap<String, Object>> rawDataList) {
        Map<String, Object> mergedData = new HashMap<>();

        // 初始化累计值
        long totalProblems = 0;
        long totalSubmissions = 0;
        long totalUsers = 0;
        long totalAccepted = 0;

        // 累加各项统计数据
        for (HashMap<String, Object> data : rawDataList) {
            totalProblems += getLongValue(data, "totalProblems");
            totalSubmissions += getLongValue(data, "totalSubmissions");
            totalUsers += getLongValue(data, "totalUsers");
            totalAccepted += getLongValue(data, "totalAccepted");
        }

        // 计算总体通过率
        double overallAcceptanceRate = totalSubmissions > 0 ? (double) totalAccepted / totalSubmissions : 0.0;

        // 构建合并后的数据
        mergedData.put("totalProblems", totalProblems);
        mergedData.put("totalSubmissions", totalSubmissions);
        mergedData.put("totalUsers", totalUsers);
        mergedData.put("totalAccepted", totalAccepted);
        mergedData.put("overallAcceptanceRate", overallAcceptanceRate);

        return mergedData;
    }

    /**
     * 按难度获取统计信息
     *
     * @return 难度统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByDifficulty() {
        // 使用统一分布统计接口获取基础数据
        DistributionCriteria criteria = DistributionCriteria.builder().dimension(DistributionDimension.DIFFICULTY).build();
        List<Map<String, Object>> resultList = getDistributionStatistics(criteria);

        // 遍历结果，添加难度文本描述
        for (Map<String, Object> item : resultList) {
            Integer difficulty = (Integer) item.get("dimension_value");
            // 添加难度文本描述
            item.put("difficulty_label", ProblemDifficultyEnum.getDescriptionByCode(difficulty));
        }

        return resultList;
    }

    /**
     * 按类型获取统计信息
     *
     * @return 类型统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByType() {
        // TODO 需要将返回值转化成实体类
        // 使用统一分布统计接口获取基础数据
        DistributionCriteria criteria = DistributionCriteria.builder().dimension(DistributionDimension.TYPE).build();
        List<Map<String, Object>> resultList = getDistributionStatistics(criteria);

        // 为每条记录添加类型描述
        return resultList.stream()
                .map(StatisticsUtils::addTypeDescription)
                .collect(Collectors.toList());
    }

    /**
     * 按语言获取统计信息
     *
     * @return 语言统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByLanguage() {
        // 使用统一分布统计接口获取基础数据
        DistributionCriteria criteria = DistributionCriteria.builder().dimension(DistributionDimension.LANGUAGE).build();
        List<Map<String, Object>> rawDataList = getDistributionStatistics(criteria);

        // 创建每种语言的统计映射
        Map<String, Map<String, Object>> languageStatsMap = new HashMap<>();

        // 遍历原始数据，解析语言组合并累加统计数据
        for (Map<String, Object> item : rawDataList) {
            String languagesJson = (String) item.get("dimension_value");
            List<String> languages = StatisticsUtils.parseLanguageArray(languagesJson);

            // 提取当前记录的统计数据
            long activeProblems = ((Number) item.get("active_problems")).longValue();
            long totalProblems = ((Number) item.get("total_problems")).longValue();
            long totalSubmissions = ((Number) item.get("total_submissions")).longValue();
            long totalAccepted = ((Number) item.get("total_accepted")).longValue();

            // 为每种语言更新统计数据
            for (String language : languages) {
                Map<String, Object> languageStats = languageStatsMap.computeIfAbsent(language,
                        StatisticsUtils::initLanguageStats);

                // 累加统计数据
                StatisticsUtils.updateLanguageStats(languageStats, activeProblems, totalProblems,
                        totalSubmissions, totalAccepted);
            }
        }

        // 计算每种语言的百分比和通过率
        StatisticsUtils.calculateLanguagePercentages(languageStatsMap);

        // 将Map转换为列表并添加语言描述
        return languageStatsMap.values().stream()
                .map(StatisticsUtils::addLanguageDescription)
                .collect(Collectors.toList());
    }


    /**
     * 按状态获取统计信息
     *
     * @return 状态统计列表
     */
    @Override
    public List<Map<String, Object>> getStatisticsByStatus() {
        // TODO 需要将返回值转化成实体类
        // 使用统一分布统计接口替代旧的实现
        DistributionCriteria criteria = DistributionCriteria.builder().dimension(DistributionDimension.STATUS).build();
        return getDistributionStatistics(criteria);
    }

    /**
     * 获取题目创建趋势
     * TODO 需要将返回值转化成实体类
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 创建趋势数据
     */
    @Override
    public List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity) {
        TrendCriteria criteria = TrendCriteria.builder().type(TrendType.PROBLEM_CREATION).startTime(startDate).endTime(endDate).timeGranularity(granularity).build();
        List<Map<String, Object>> analysis = getTrendAnalysis(criteria);
        // 使用工具类处理每条记录，增强数据
        List<Map<String, Object>> enhancedResult = analysis.stream()
                .map(StatisticsUtils::enhanceProblemTrendData)
                .collect(Collectors.toList());
        return enhancedResult;
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
        // 使用统一接口替代旧的实现
        TrendCriteria criteria = TrendCriteria.builder().type(TrendType.ACCEPTANCE_RATE_TREND).startTime(startDate).endTime(endDate).timeGranularity(granularity).build();
        // TODO 需要将返回值转化成实体类
        return getTrendAnalysis(criteria);
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
        // 使用统一排行榜接口替代旧的实现
        RankingCriteria criteria = RankingCriteria.builder().type(RankingType.POPULARITY).limit(limit != null ? limit : 10).timeRange(timeRange).build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
    }

    /**
     * 获取高质量题目排行榜
     *
     * @param limit 限制数量
     * @return 高质量题目排行榜
     */
    @Override
    public List<Map<String, Object>> getHighQualityProblemsRanking(Integer limit) {
        // 使用统一排行榜接口
        RankingCriteria criteria = RankingCriteria.builder()
                .type(RankingType.QUALITY)
                .limit(limit != null ? limit : 10)
                .build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
    }

    /**
     * 获取最难题目排行榜
     *
     * @param limit 限制数量
     * @return 最难题目排行榜
     */
    @Override
    public List<Map<String, Object>> getHardestProblemsRanking(Integer limit) {

        // 使用统一排行榜接口替代旧的实现
        RankingCriteria criteria = RankingCriteria.builder().type(RankingType.HARDEST).limit(limit != null ? limit : 10).build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
    }

    /**
     * 获取简单题目排行榜
     *
     * @param limit 限制数量
     * @return 最易题目排行榜
     */
    @Override
    public List<Map<String, Object>> getEasiestProblemsRanking(Integer limit) {

        // 使用统一排行榜接口替代旧的实现
        RankingCriteria criteria = RankingCriteria.builder().type(RankingType.EASIEST).limit(limit != null ? limit : 10).build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
    }

    /**
     * 获取最多提交题目排行榜
     *
     * @param limit 限制数量
     * @return 最易题目排行榜
     */
    @Override
    public List<Map<String, Object>> getMaxSubmissionProblemsRanking(Integer limit, Integer timeRange) {
        // 使用统一排行榜接口替代旧的实现
        RankingCriteria criteria = RankingCriteria.builder().type(RankingType.MAX_SUBMISSION).limit(limit != null ? limit : 10).timeRange(timeRange).build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
    }

    /**
     * 获取零提交题目排行榜
     *
     * @param limit 限制数量
     * @return 最易题目排行榜
     */
    @Override
    public List<Map<String, Object>> getZeroSubmissionProblemsRanking(Integer limit, Integer timeRange) {
        // 使用统一排行榜接口替代旧的实现
        RankingCriteria criteria = RankingCriteria.builder().type(RankingType.ZERO_SUBMISSION).limit(limit != null ? limit : 10).timeRange(timeRange).build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
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

        // 使用统一排行榜接口替代旧的实现
        RankingCriteria criteria = RankingCriteria.builder().type(RankingType.CREATOR_CONTRIBUTION).limit(limit != null ? limit : 10).timeRange(timeRange).build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
    }

    /**
     * 获取题目质量排行榜
     *
     * @param limit 限制数量
     * @return 质量排行榜
     */
    @Override
    public List<Map<String, Object>> getQualityProblemsRanking(Integer limit) {

        // 使用统一排行榜接口替代旧的实现
        RankingCriteria criteria = RankingCriteria.builder().type(RankingType.QUALITY).limit(limit != null ? limit : 10).build();
        // TODO 需要将返回值转化成实体类
        return getProblemRanking(criteria);
    }

    /**
     * 获取难度-类型分布矩阵
     *
     * @return 分布矩阵数据
     */
    @Override
    public List<Map<String, Object>> getDifficultyTypeDistribution() {
        return problemStatisticsManager.getDifficultyTypeDistribution();
    }

    /**
     * 获取通过率分布统计
     *
     * @param bucketSize 区间大小
     * @return 通过率分布数据
     */
    @Override
    public List<Map<String, Object>> getAcceptanceRateDistribution(Double bucketSize) {
        return problemStatisticsManager.getAcceptanceRateDistribution(bucketSize);

    }

    /**
     * 获取提交量分布统计
     *
     * @return 提交量分布数据
     */
    @Override
    public List<Map<String, Object>> getSubmissionCountDistribution() {
        return problemStatisticsManager.getSubmissionCountDistribution();
    }

    /**
     * 获取题目综合健康度报告
     *
     * @return 健康度报告
     */
    @Override
    public Map<String, Object> getProblemHealthReport() {
        return problemStatisticsManager.getProblemHealthReport();
    }

    /**
     * 获取平台数据大屏统计
     *
     * @return 大屏统计数据
     */
    @Override
    public Map<String, Object> getDashboardStatistics() {
        try {
            // 使用统一接口替代旧的实现
            BatchProblemRequest.UnifiedStatisticsRequest request = BatchProblemRequest.UnifiedStatisticsRequest.builder()
                    .scope(StatisticsScope.DASHBOARD)
                    .build();

            List<HashMap<String, Object>> rawDataList = getUnifiedStatisticsRaw(request);

            // 如果返回列表为空，返回空的Map
            if (rawDataList == null || rawDataList.isEmpty()) {
                return new HashMap<>();
            }

            // 对于仪表盘统计，我们需要处理可能的多条记录
            if (rawDataList.size() == 1) {
                // 只有一条记录，直接返回
                return new HashMap<>(rawDataList.get(0));
            } else {
                // 多条记录，需要合并统计数据
                return mergeDashboardStatistics(rawDataList);
            }
        } catch (Exception e) {
            throw new RuntimeException("获取仪表盘统计信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 合并多条仪表盘统计数据
     *
     * @param rawDataList 原始数据列表
     * @return 合并后的统计数据
     */
    private Map<String, Object> mergeDashboardStatistics(List<HashMap<String, Object>> rawDataList) {
        Map<String, Object> mergedData = new HashMap<>();

        // 初始化累计值
        long totalProblems = 0;
        long totalSubmissions = 0;
        long totalUsers = 0;
        long dailySubmissions = 0;
        long weeklySubmissions = 0;
        long monthlySubmissions = 0;

        // 累加各项统计数据
        for (HashMap<String, Object> data : rawDataList) {
            totalProblems += getLongValue(data, "totalProblems");
            totalSubmissions += getLongValue(data, "totalSubmissions");
            totalUsers += getLongValue(data, "totalUsers");
            dailySubmissions += getLongValue(data, "dailySubmissions");
            weeklySubmissions += getLongValue(data, "weeklySubmissions");
            monthlySubmissions += getLongValue(data, "monthlySubmissions");
        }

        // 构建合并后的数据
        mergedData.put("totalProblems", totalProblems);
        mergedData.put("totalSubmissions", totalSubmissions);
        mergedData.put("totalUsers", totalUsers);
        mergedData.put("dailySubmissions", dailySubmissions);
        mergedData.put("weeklySubmissions", weeklySubmissions);
        mergedData.put("monthlySubmissions", monthlySubmissions);

        return mergedData;
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
        // 使用统一推荐接口替代旧的实现
        RecommendationCriteria criteria = RecommendationCriteria.builder().type(RecommendationType.ALGORITHM_DATA).difficulty(difficulty).problemType(problemType).limit(limit != null ? limit : 10).build();
        return getRecommendedProblemsWithScore(criteria);
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
        // 使用统一推荐接口替代旧的实现
        RecommendationCriteria criteria = RecommendationCriteria.builder().type(RecommendationType.SIMILARITY).baseProblemId(problemId).limit(limit != null ? limit : 5).build();
        return getRecommendedProblemsWithScore(criteria);
    }

    /**
     * 获取平台增长指标
     *
     * @param timeRange 时间范围
     * @return 增长指标数据
     */
    @Override
    public Map<String, Object> getPlatformGrowthMetrics(Integer timeRange) {
        return problemStatisticsManager.getPlatformGrowthMetrics(timeRange);
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
        return problemStatisticsManager.getMonthlyReport(year, month);
    }

    /**
     * 获取年度报表
     *
     * @param year 年份
     * @return 年度报表数据
     */
    @Override
    public Map<String, Object> getAnnualReport(Integer year) {
        return problemStatisticsManager.getAnnualReport(year);
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
        return problemStatisticsManager.getCustomRangeReport(startDate, endDate, metrics);
    }

    /**
     * 获取实时题目状态监控
     *
     * @return 实时状态数据
     */
    @Override
    public Map<String, Object> getRealTimeProblemStatus() {
        return problemStatisticsManager.getRealTimeProblemStatus();
    }

    /**
     * 获取实时提交监控
     *
     * @param timeWindow 时间窗口
     * @return 实时提交监控数据
     */
    @Override
    public Map<String, Object> getRealTimeSubmissionMonitoring(Integer timeWindow) {
        return problemStatisticsManager.getRealTimeSubmissionMonitoring(timeWindow);
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
        return problemRecommendationManager.recommendProblems(criteria);
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
        return problemRecommendationManager.getRecommendedProblems(criteria);
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
        return problemRecommendationManager.getRecommendedProblemsWithScore(criteria);
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
        return problemStatisticsManager.getUnifiedStatistics(request);
    }

    /**
     * 获取统一统计信息（原始数据）
     * 直接返回Map格式的原始统计数据，适合需要简单数据格式的场景
     *
     * @param request 统一统计请求，包含范围、过滤条件等参数
     * @return 原始统计数据Map
     */
    @Override
    public List<HashMap<String, Object>> getUnifiedStatisticsRaw(BatchProblemRequest.UnifiedStatisticsRequest request) {
        return problemStatisticsManager.getUnifiedStatisticsRaw(request);
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
        return problemStatisticsManager.getProblemRanking(criteria);
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
        return problemStatisticsManager.getTrendAnalysis(criteria);
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
        return problemStatisticsManager.getDistributionStatistics(criteria);
    }
}
