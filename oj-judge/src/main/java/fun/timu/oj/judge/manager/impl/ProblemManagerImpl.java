package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import fun.timu.oj.judge.model.criteria.DistributionCriteria;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import fun.timu.oj.judge.model.criteria.TrendCriteria;
import fun.timu.oj.judge.model.request.UnifiedStatisticsRequest;
import fun.timu.oj.judge.model.response.UnifiedStatisticsResponse;
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

    /**
     * 根据id查询题目
     *
     * @param id 题目id
     * @return 题目
     */
    @Override
    public ProblemDO getById(Long id) {
        return problemMapper.selectById(id);
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
        // 创建分页对象
        Page<ProblemDO> page = new Page<>(current, size);

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 未删除的记录
        queryWrapper.eq(ProblemDO::getIsDeleted, false);

        // 按创建时间降序排序，最新创建的题目排在前面
        queryWrapper.orderByDesc(ProblemDO::getCreatedAt);
        // 按题目类型筛选
        if (problemType != null && !problemType.isEmpty()) {
            queryWrapper.eq(ProblemDO::getProblemType, problemType);
        }

        // 按难度筛选
        if (difficulty != null) {
            queryWrapper.eq(ProblemDO::getDifficulty, difficulty);
        }

        // 按状态筛选
        if (status != null) {
            queryWrapper.eq(ProblemDO::getStatus, status);
        }

        // 按可见性筛选
        if (visibility != null) {
            queryWrapper.eq(ProblemDO::getVisibility, visibility);
        }

        // 按是否需要输入筛选
        if (hasInput != null) {
            queryWrapper.eq(ProblemDO::getHasInput, hasInput);
        }

        // 通过率筛选 (通过率 = accepted_count / submission_count)
        if (MinAcceptanceRate != null || MaxAcceptanceRate != null) {
            // 判断是否需要真正的通过率筛选
            boolean needAcceptanceRateFilter = false;

            if (MinAcceptanceRate != null && MinAcceptanceRate > 0.0) {
                needAcceptanceRateFilter = true;
            }

            if (MaxAcceptanceRate != null && MaxAcceptanceRate < 1.0) {
                needAcceptanceRateFilter = true;
            }

            // 只有在真正需要通过率筛选时才添加submission_count > 0的条件
            if (needAcceptanceRateFilter) {
                // 防止除以零，确保至少有一次提交
                queryWrapper.gt(ProblemDO::getSubmissionCount, 0);

                if (MinAcceptanceRate != null && MinAcceptanceRate > 0.0) {
                    // acceptedCount >= submissionCount * MinAcceptanceRate
                    queryWrapper.apply("accepted_count >= submission_count * {0}", MinAcceptanceRate);
                }

                if (MaxAcceptanceRate != null && MaxAcceptanceRate < 1.0) {
                    // acceptedCount <= submissionCount * MaxAcceptanceRate
                    queryWrapper.apply("accepted_count <= submission_count * {0}", MaxAcceptanceRate);
                }
            }
        }

        // 支持的编程语言筛选 (因为是JSON字段，MyBatis-Plus不能直接处理)
        // 需要使用JSON查询，这里使用原生SQL
        if (supportedLanguages != null && !supportedLanguages.isEmpty()) {
            // 构造JSON查询条件，始终用括号包围
            StringBuilder jsonCondition = new StringBuilder("(");
            for (int i = 0; i < supportedLanguages.size(); i++) {
                if (i > 0) jsonCondition.append(" AND ");
                // 使用 JSON_SEARCH 函数查找数组中的字符串元素
                jsonCondition.append("JSON_SEARCH(supported_languages, 'one', '").append(supportedLanguages.get(i)).append("') IS NOT NULL");
            }
            jsonCondition.append(")");
            queryWrapper.apply(jsonCondition.toString());
        }

        // 执行查询
        return problemMapper.selectPage(page, queryWrapper);
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
        // 使用MyBatis-Plus的LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemDO::getCreatorId, creatorId);
        // 只查询未删除的题目
        queryWrapper.eq(ProblemDO::getIsDeleted, false);
        // 按创建时间降序排序
        queryWrapper.orderByDesc(ProblemDO::getCreatedAt);

        return problemMapper.selectList(queryWrapper);
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
        return problemMapper.insert(problemDO);
    }

    /**
     * 根据ID更新题目信息
     *
     * @param problemDO 包含要更新的题目信息的对象
     * @return 更新操作的结果，返回影响的行数
     */
    @Override
    public int updateById(ProblemDO problemDO) {
        // 创建UpdateWrapper对象，用于构建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 指定更新的记录ID
        updateWrapper.eq(ProblemDO::getId, problemDO.getId());

        // 只有当字段不为null时，才将其添加到更新条件中
        if (problemDO.getTitle() != null) {
            updateWrapper.set(ProblemDO::getTitle, problemDO.getTitle());
        }
        if (problemDO.getTitleEn() != null) {
            updateWrapper.set(ProblemDO::getTitleEn, problemDO.getTitleEn());
        }
        if (problemDO.getDescription() != null) {
            updateWrapper.set(ProblemDO::getDescription, problemDO.getDescription());
        }
        if (problemDO.getDescriptionEn() != null) {
            updateWrapper.set(ProblemDO::getDescriptionEn, problemDO.getDescriptionEn());
        }
        if (problemDO.getProblemType() != null) {
            updateWrapper.set(ProblemDO::getProblemType, problemDO.getProblemType());
        }
        if (problemDO.getDifficulty() != null) {
            updateWrapper.set(ProblemDO::getDifficulty, problemDO.getDifficulty());
        }
        if (problemDO.getTimeLimit() != null) {
            updateWrapper.set(ProblemDO::getTimeLimit, problemDO.getTimeLimit());
        }
        if (problemDO.getMemoryLimit() != null) {
            updateWrapper.set(ProblemDO::getMemoryLimit, problemDO.getMemoryLimit());
        }
        if (problemDO.getSupportedLanguages() != null) {
            updateWrapper.set(ProblemDO::getSupportedLanguages, problemDO.getSupportedLanguages());
        }
        if (problemDO.getSolutionTemplates() != null) {
            updateWrapper.set(ProblemDO::getSolutionTemplates, problemDO.getSolutionTemplates());
        }
        if (problemDO.getInputDescription() != null) {
            updateWrapper.set(ProblemDO::getInputDescription, problemDO.getInputDescription());
        }
        if (problemDO.getOutputDescription() != null) {
            updateWrapper.set(ProblemDO::getOutputDescription, problemDO.getOutputDescription());
        }
        if (problemDO.getHasInput() != null) {
            updateWrapper.set(ProblemDO::getHasInput, problemDO.getHasInput());
        }
        if (problemDO.getInputFormat() != null) {
            updateWrapper.set(ProblemDO::getInputFormat, problemDO.getInputFormat());
        }
        if (problemDO.getExamples() != null) {
            updateWrapper.set(ProblemDO::getExamples, problemDO.getExamples());
        }
        if (problemDO.getStatus() != null) {
            updateWrapper.set(ProblemDO::getStatus, problemDO.getStatus());
        }
        if (problemDO.getVisibility() != null) {
            updateWrapper.set(ProblemDO::getVisibility, problemDO.getVisibility());
        }
        if (problemDO.getHints() != null) {
            updateWrapper.set(ProblemDO::getHints, problemDO.getHints());
        }
        if (problemDO.getConstraints() != null) {
            updateWrapper.set(ProblemDO::getConstraints, problemDO.getConstraints());
        }
        if (problemDO.getNotes() != null) {
            updateWrapper.set(ProblemDO::getNotes, problemDO.getNotes());
        }
        if (problemDO.getMetadata() != null) {
            updateWrapper.set(ProblemDO::getMetadata, problemDO.getMetadata());
        }

        // 如果没有字段需要更新，则直接返回0
        if (updateWrapper.getSqlSet() == null || updateWrapper.getSqlSet().isEmpty()) {
            return 0;
        }
        return problemMapper.update(new ProblemDO(), updateWrapper);
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
        ProblemDO problemDO = new ProblemDO();
        problemDO.setId(id);
        problemDO.setIsDeleted(1);
        // 执行更新操作
        return problemMapper.updateById(problemDO);
    }

    /**
     * 检查指定标题的题目是否已存在
     *
     * @param title 题目标题
     * @return 如果存在未删除的同名题目返回true，否则返回false
     */
    @Override
    public boolean existsByTitle(String title) {
        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        // 查找与输入标题匹配的记录
        queryWrapper.eq(ProblemDO::getTitle, title);
        // 只检查未删除的题目
        queryWrapper.eq(ProblemDO::getIsDeleted, false);
        // 如果count > 0，表示存在同名题目
        return problemMapper.selectCount(queryWrapper) > 0;
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
        try {
            // 1. 根据题目ID查询题目信息
            ProblemDO problemDO = problemMapper.selectById(problemId);

            // 2. 检查题目是否存在
            if (problemDO == null) throw new RuntimeException("题目不存在，ID:" + problemId);

            // 3. 检查题目状态和删除标志
            if (problemDO.getStatus() != 1 || problemDO.getIsDeleted() == 1)
                throw new RuntimeException("题目状态不正确，ID:" + problemId + "，状态:" + problemDO.getStatus() + "，删除标志:" + problemDO.getIsDeleted());

            // 4. 检查题目可见性
            if (problemDO.getVisibility() == 0) {
                if (loginUser == null) throw new RuntimeException("用户未登录，无法更新私人题目的提交统计");
                if (!loginUser.getAccountNo().equals(problemDO.getCreatorId())) {
                    throw new RuntimeException("用户无权限更新该题目的提交统计, 用户ID: " + loginUser.getAccountNo() + "创建者ID: " + problemDO.getCreatorId());
                }
            }

            // 5. 如果题目信息存在，则更新提交计数和接受计数
            // 增加提交计数
            problemDO.setSubmissionCount(problemDO.getSubmissionCount() + 1);
            // 如果提交被接受，则增加接受计数
            if (isAccepted) {
                problemDO.setAcceptedCount(problemDO.getAcceptedCount() + 1);
            }
            // 更新题目的提交和接受计数
            return problemMapper.updateById(problemDO);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        // 如果limit为null或小于等于0，设置默认值为10
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        // 如果minAcceptanceRate为null，设置默认值为0.0
        if (minAcceptanceRate == null) {
            minAcceptanceRate = 0.0;
        }
        // 如果maxAcceptanceRate为null，设置默认值为1.0
        if (maxAcceptanceRate == null) {
            maxAcceptanceRate = 1.0;
        }
        // 调用problemMapper的方法来选择推荐的问题
        return problemMapper.selectRecommendedProblems(minAcceptanceRate, maxAcceptanceRate, difficulty, limit);
    }

    /**
     * 获取题目统计信息
     * 该方法返回按题目类型和难度分组的统计数据，包括总题目数量、活跃题目数量、
     * 总提交次数、总通过次数以及平均通过率等信息
     *
     * @return 统计信息列表
     * @deprecated 使用 {@link #getUnifiedStatistics(UnifiedStatisticsRequest)} 替代，请参照统一接口迁移指南
     */
    @Override
    @Deprecated
    public List<ProblemStatisticsDTO> getProblemStatistics() {
        try {
            log.info("开始获取题目统计信息");
            List<Object> rawStatistics = problemMapper.getProblemStatistics();

            if (rawStatistics == null) {
                return Collections.emptyList();
            }
            return rawStatistics.stream().map(item -> ProblemStatisticsDTO.fromMap((Map<String, Object>) item)).collect(Collectors.toList());
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
        // 参数校验
        if (problemIds == null || problemIds.isEmpty() || status == null) {
            throw new RuntimeException("批量更新题目状态失败:参数无效，题目ID列表为空或状态值为null");
        }

        // 创建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 只更新未删除的题目
        updateWrapper.eq(ProblemDO::getIsDeleted, 0);
        // 限定要更新的ID列表
        updateWrapper.in(ProblemDO::getId, problemIds);
        // 设置要更新的字段
        updateWrapper.set(ProblemDO::getStatus, status);

        // 执行批量更新并返回影响的行数
        return problemMapper.update(null, updateWrapper);
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
        // 检查用户ID是否无效如果无效，抛出异常
        if (creatorId == null || creatorId <= 0) {
            throw new RuntimeException("用户ID无效");
        }

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemDO::getCreatorId, creatorId);
        queryWrapper.eq(ProblemDO::getIsDeleted, 0);

        // 执行查询并返回结果
        return Long.valueOf(problemMapper.selectCount(queryWrapper));
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
        try {
            // 创建查询条件
            LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
            // 只查询未删除的题目
            queryWrapper.eq(ProblemDO::getIsDeleted, false);
            // 只查询状态为1（激活）的题目
            queryWrapper.eq(ProblemDO::getStatus, 1);
            // 只查询可见性为1（公开）的题目
            queryWrapper.eq(ProblemDO::getVisibility, 1);
            // 按创建时间降序排序
            queryWrapper.orderByDesc(ProblemDO::getCreatedAt);
            // TODO 如果需要，可以添加其他查询条件，如题目类型、难度等

            // 根据是否有limit参数决定查询方式
            if (limit != null && limit > 0) {
                // 使用单页查询方式，直接获取前limit条记录
                Page<ProblemDO> singlePage = new Page<>(1, limit);
                return problemMapper.selectPage(singlePage, queryWrapper);
            } else {
                // 使用常规分页方式
                Page<ProblemDO> page = new Page<>(pageNum, pageSize);
                return problemMapper.selectPage(page, queryWrapper);
            }
        } catch (Exception e) {
            log.error("获取最近创建的题目失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取最近创建的题目失败", e);
        }
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
        // 创建分页对象
        Page<ProblemDO> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询未删除的题目
        queryWrapper.eq(ProblemDO::getIsDeleted, false);

        // 只查询状态为激活的题目
        queryWrapper.eq(ProblemDO::getStatus, 1);

        // 只查询可见性为公开的题目
        queryWrapper.eq(ProblemDO::getVisibility, 1);

        // 如果语言参数有效，添加JSON查询条件
        if (language != null && !language.trim().isEmpty()) {
            // 使用apply方法应用原生SQL条件，通过JSON_CONTAINS函数查询
            // 使用参数化查询防止SQL注入
            queryWrapper.apply("JSON_CONTAINS(supported_languages, JSON_QUOTE({0}))", language);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(ProblemDO::getCreatedAt);

        // 执行查询并返回分页结果
        return problemMapper.selectPage(page, queryWrapper);
    }

    /**
     * 批量软删除题目
     *
     * @param problemIds 题目ID列表，用于指定要删除的题目
     * @return 返回成功删除的题目数量
     */
    @Override
    public int batchSoftDelete(List<Long> problemIds) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty()) {
            return 0;
        }

        // 创建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 只更新未删除的题目
        updateWrapper.eq(ProblemDO::getIsDeleted, 0);
        // 限定要更新的ID列表
        updateWrapper.in(ProblemDO::getId, problemIds);
        // 设置要更新的字段
        updateWrapper.set(ProblemDO::getIsDeleted, 1);

        // 执行批量更新并返回影响的行数
        return problemMapper.update(null, updateWrapper);
    }

    /**
     * 批量恢复删除的题目
     *
     * @param problemIds 需要恢复的题目ID列表
     * @return 影响的行数，表示成功恢复的题目数量
     */
    @Override
    public int batchRestore(List<Long> problemIds) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty()) {
            return 0;
        }

        // 创建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 只更新已删除的题目
        updateWrapper.eq(ProblemDO::getIsDeleted, 1);
        // 限定要更新的ID列表
        updateWrapper.in(ProblemDO::getId, problemIds);
        // 设置要更新的字段
        updateWrapper.set(ProblemDO::getIsDeleted, 0);

        // 执行批量更新并返回影响的行数
        return problemMapper.update(null, updateWrapper);
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
        // 参数校验
        if (problemId == null || problemId <= 0) {
            throw new RuntimeException("题目ID无效");
        }

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemDO::getId, problemId);
        queryWrapper.eq(ProblemDO::getIsDeleted, 0);
        // 只查询需要的字段，提高效率
        queryWrapper.select(ProblemDO::getSubmissionCount, ProblemDO::getAcceptedCount);

        // 查询题目
        ProblemDO problem = problemMapper.selectOne(queryWrapper);

        // 如果题目不存在，返回0
        if (problem == null) {
            return 0.0;
        }

        // 如果提交次数为0，返回0，避免除以零错误
        if (problem.getSubmissionCount() == 0) {
            return 0.0;
        }

        // 计算通过率并四舍五入到小数点后4位
        double acceptanceRate = (double) problem.getAcceptedCount() / problem.getSubmissionCount();
        return Math.round(acceptanceRate * 10000) / 10000.0;
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
        // 参数校验
        if (problemIds == null || problemIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询指定的字段
        queryWrapper.select(ProblemDO::getId, ProblemDO::getTitle, ProblemDO::getTitleEn, ProblemDO::getDifficulty, ProblemDO::getStatus, ProblemDO::getProblemType, ProblemDO::getSubmissionCount, ProblemDO::getAcceptedCount, ProblemDO::getCreatedAt, ProblemDO::getUpdatedAt);
        // 按ID列表筛选
        queryWrapper.in(ProblemDO::getId, problemIds);
        // 只查询未删除的题目
        queryWrapper.eq(ProblemDO::getIsDeleted, 0);

        // 执行查询
        List<ProblemDO> problems = problemMapper.selectList(queryWrapper);

        // 如果没有找到任何题目，直接返回空列表
        if (problems.isEmpty()) {
            return problems;
        }

        // 使用Map存储题目，以便按照传入的ID列表顺序进行排序
        Map<Long, ProblemDO> problemMap = new HashMap<>();
        for (ProblemDO problem : problems) {
            problemMap.put(problem.getId(), problem);
        }

        // 按照传入的ID列表顺序排序
        List<ProblemDO> orderedProblems = new ArrayList<>();
        for (Long id : problemIds) {
            ProblemDO problem = problemMap.get(id);
            if (problem != null) {
                orderedProblems.add(problem);
            }
        }
        return orderedProblems;
    }

    /**
     * 获取题目详细统计信息
     * 该方法通过调用Mapper层获取包含题目各维度的统计数据
     *
     * @return 包含详细统计数据的HashMap
     * @deprecated 使用 {@link #getUnifiedStatistics(UnifiedStatisticsRequest)} 替代，请使用DETAILED范围
     */
    @Override
    @Deprecated
    public ProblemDetailStatisticsDTO getProblemDetailStatistics() {
        HashMap<String, Object> statistics = problemMapper.getProblemDetailStatistics();
        if (statistics == null) {
            throw new RuntimeException("获取题目详细统计信息失败");
        }
        return ProblemDetailStatisticsDTO.fromMap(statistics);
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
        // 创建分页对象
        Page<ProblemDO> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 添加基本条件：未删除的记录
        queryWrapper.eq(ProblemDO::getIsDeleted, 0);

        // 添加可选条件
        if (status != null) {
            queryWrapper.eq(ProblemDO::getStatus, status);
        }
        if (startDate != null) {
            queryWrapper.ge(ProblemDO::getCreatedAt, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(ProblemDO::getCreatedAt, endDate);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(ProblemDO::getCreatedAt);

        // 执行分页查询
        return problemMapper.selectPage(page, queryWrapper);
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
            // 参数校验
            if (problemId == null || problemId <= 0) {
                return Collections.emptyList();
            }

            // 设置默认限制
            if (limit == null || limit <= 0) {
                limit = 10; // 默认返回10条记录
            }

            // 调用Mapper层方法获取相似题目
            List<ProblemDO> problemList = problemMapper.findSimilarProblems(problemId, difficulty, problemType, limit);

            return problemList;
        } catch (Exception e) {
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
    public int batchUpdateVisibility(List<Long> problemIds, Integer visibility) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty() || visibility == null) {
            throw new IllegalArgumentException("参数错误");
        }

        // 使用LambdaUpdateWrapper构建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ProblemDO::getId, problemIds).set(ProblemDO::getVisibility, visibility);

        // 执行更新操作
        int updatedCount = problemMapper.update(null, updateWrapper);
        if (updatedCount <= 0) throw new RuntimeException("更新失败");
        return updatedCount;
    }

    /**
     * 批量更新题目的时间和内存限制
     *
     * @param problemIds  需要更新的题目ID列表
     * @param timeLimit   新的时间限制值（毫秒）
     * @param memoryLimit 新的内存限制值（MB）
     * @return 成功更新的记录数
     */
    public int batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty()) {
            throw new IllegalArgumentException("参数错误");
        }

        if (timeLimit == null && memoryLimit == null) {
            throw new IllegalArgumentException("参数错误");
        }

        // 创建更新构造器
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 设置更新条件：ID在指定列表中且未删除
        updateWrapper.in(ProblemDO::getId, problemIds).eq(ProblemDO::getIsDeleted, 0);

        // 设置更新字段：如果参数不为空，则更新对应字段
        if (timeLimit != null) {
            updateWrapper.set(ProblemDO::getTimeLimit, timeLimit);
        }

        if (memoryLimit != null) {
            updateWrapper.set(ProblemDO::getMemoryLimit, memoryLimit);
        }

        // 执行更新操作
        int updatedRows = problemMapper.update(null, updateWrapper);
        if (updatedRows <= 0) throw new RuntimeException("更新失败,请检查题目ID是否存在或未被删除");
        return updatedRows;

    }

    /**
     * 重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 需要重置统计数据的题目ID列表
     * @return 更新成功的记录数
     */
    public int resetProblemStats(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            throw new IllegalArgumentException("参数错误");
        }

        // 使用MyBatis-Plus的LambdaUpdateWrapper构建更新操作
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ProblemDO::getId, problemIds).eq(ProblemDO::getIsDeleted, 0)  // 只重置未删除的题目
                .set(ProblemDO::getSubmissionCount, 0).set(ProblemDO::getAcceptedCount, 0);

        return problemMapper.update(null, updateWrapper);
    }

    /**
     * 查询长时间未更新的题目
     *
     * @param lastUpdateBefore 上次更新时间早于此日期的题目将被视为陈旧题目
     * @param pageNum          页码（从1开始）
     * @param pageSize         每页大小
     * @return 分页结果，包含符合条件的题目列表
     */
    public IPage<ProblemDO> selectStaleProblems(Date lastUpdateBefore, int pageNum, int pageSize) {
        log.info("ProblemManager--->查询长时间未更新的题目, 更新时间早于: {}, 页码: {}, 每页大小: {}", lastUpdateBefore, pageNum, pageSize);

        // 创建分页对象
        Page<ProblemDO> page = new Page<>(pageNum, pageSize);

        // 创建查询条件构造器
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 设置查询条件：更新时间早于指定日期，且未被删除
        queryWrapper.lt(ProblemDO::getUpdatedAt, lastUpdateBefore).eq(ProblemDO::getIsDeleted, 0).eq(ProblemDO::getStatus, 1); // 只查询已发布的题目

        // 按更新时间升序排序，最早更新的排在前面
        queryWrapper.orderByAsc(ProblemDO::getUpdatedAt);

        // 执行查询并返回分页结果
        return problemMapper.selectPage(page, queryWrapper);
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
        try {
            log.info("ProblemManager--->查询零提交题目，页码: {}, 每页大小: {}", pageNum, pageSize);

            // 创建分页对象
            Page<ProblemDO> page = new Page<>(pageNum, pageSize);

            // 创建查询条件构造器
            LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

            // 设置查询条件：提交数为0且未删除的题目
            queryWrapper.eq(ProblemDO::getSubmissionCount, 0).eq(ProblemDO::getIsDeleted, false).orderByDesc(ProblemDO::getCreatedAt); // 按创建时间降序排Ï列

            // 执行分页查询
            IPage<ProblemDO> problemPage = problemMapper.selectPage(page, queryWrapper);

            log.info("查询零提交题目成功，总数: {}", problemPage.getTotal());
            return problemPage;
        } catch (Exception e) {
            log.error("ProblemManager--->查询零提交题目失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询零提交题目失败", e);
        }
    }

    /**
     * 获取题目总体统计信息（增强版）
     *
     * @return 统计信息
     * @deprecated 使用 {@link #getUnifiedStatistics(UnifiedStatisticsRequest)} 替代，请使用OVERALL范围
     */
    @Override
    @Deprecated
    public Map<String, Object> getOverallStatistics() {
        try {
            return problemMapper.getOverallStatistics();
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
            List<HashMap<String, Object>> result = problemMapper.getStatisticsByDifficulty();
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
            List<HashMap<String, Object>> result = problemMapper.getStatisticsByType();
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
            List<HashMap<String, Object>> result = problemMapper.getStatisticsByLanguage();
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
            List<HashMap<String, Object>> result = problemMapper.getStatisticsByStatus();
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
     * @deprecated 请使用 {@link #getTrendAnalysis(TrendCriteria)} 替代
     */
    @Deprecated
    @Override
    public List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity) {
        try {
            List<HashMap<String, Object>> result = problemMapper.getProblemCreationTrend(startDate, endDate, granularity);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
     * @deprecated 请使用 {@link #getTrendAnalysis(TrendCriteria)} 替代
     */
    @Deprecated
    @Override
    public List<Map<String, Object>> getSubmissionTrendAnalysis(Date startDate, Date endDate, String granularity) {
        try {
            List<HashMap<String, Object>> result = problemMapper.getSubmissionTrendAnalysis(startDate, endDate, granularity);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
     * @deprecated 请使用 {@link #getTrendAnalysis(TrendCriteria)} 替代
     */
    @Deprecated
    @Override
    public List<Map<String, Object>> getAcceptanceRateTrend(Date startDate, Date endDate, String granularity) {
        try {
            List<HashMap<String, Object>> result = problemMapper.getAcceptanceRateTrend(startDate, endDate, granularity);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
            List<HashMap<String, Object>> result = problemMapper.getPopularProblemsRanking(limit, timeRange);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
            List<HashMap<String, Object>> result = problemMapper.getHardestProblemsRanking(limit, minSubmissions);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
            List<HashMap<String, Object>> result = problemMapper.getCreatorContributionRanking(limit, timeRange);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
            List<HashMap<String, Object>> result = problemMapper.getQualityProblemsRanking(limit);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
     * @deprecated 使用 {@link #getUnifiedStatistics(UnifiedStatisticsRequest)} 替代，请使用DASHBOARD范围
     */
    @Override
    @Deprecated
    public Map<String, Object> getDashboardStatistics() {
        try {
            return problemMapper.getDashboardStatistics();
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
            List<HashMap<String, Object>> result = problemMapper.getRecommendationData(difficulty, problemType, limit);
            return result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
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
     * 批量重置题目统计
     *
     * @param problemIds 题目ID列表
     * @return 重置记录数
     */
    @Override
    public int batchResetStats(List<Long> problemIds) {
        return 0;
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

    // ===== 新增：统一推荐接口实现 =====

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
    public UnifiedStatisticsResponse getUnifiedStatistics(UnifiedStatisticsRequest request) {
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
            UnifiedStatisticsResponse response = UnifiedStatisticsResponse.builder()
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
    public Map<String, Object> getUnifiedStatisticsRaw(UnifiedStatisticsRequest request) {
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

    private UnifiedStatisticsResponse.StatisticsMetadata buildMetadata(UnifiedStatisticsRequest request, Map<String, Object> rawData) {
        // 创建 StatisticsMetadata 对象
        UnifiedStatisticsResponse.StatisticsMetadata metadata = new UnifiedStatisticsResponse.StatisticsMetadata();

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

    // ===== 新增：统一排行榜接口实现 =====

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
