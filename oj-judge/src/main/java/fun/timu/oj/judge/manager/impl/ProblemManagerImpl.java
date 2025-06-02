package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
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
        UpdateWrapper<ProblemDO> updateWrapper = new UpdateWrapper<>();
        // 指定更新的记录ID
        updateWrapper.eq("id", problemDO.getId());

        // 只有当字段不为null时，才将其添加到更新条件中
        if (problemDO.getTitle() != null) {
            updateWrapper.set("title", problemDO.getTitle());
        }
        if (problemDO.getTitleEn() != null) {
            updateWrapper.set("title_en", problemDO.getTitleEn());
        }
        if (problemDO.getDescription() != null) {
            updateWrapper.set("description", problemDO.getDescription());
        }
        if (problemDO.getDescriptionEn() != null) {
            updateWrapper.set("description_en", problemDO.getDescriptionEn());
        }
        if (problemDO.getProblemType() != null) {
            updateWrapper.set("problem_type", problemDO.getProblemType());
        }
        if (problemDO.getDifficulty() != null) {
            updateWrapper.set("difficulty", problemDO.getDifficulty());
        }
        if (problemDO.getTimeLimit() != null) {
            updateWrapper.set("time_limit", problemDO.getTimeLimit());
        }
        if (problemDO.getMemoryLimit() != null) {
            updateWrapper.set("memory_limit", problemDO.getMemoryLimit());
        }
        if (problemDO.getSupportedLanguages() != null) {
            updateWrapper.set("supported_languages", problemDO.getSupportedLanguages());
        }
        if (problemDO.getSolutionTemplates() != null) {
            updateWrapper.set("solution_templates", problemDO.getSolutionTemplates());
        }
        if (problemDO.getInputDescription() != null) {
            updateWrapper.set("input_description", problemDO.getInputDescription());
        }
        if (problemDO.getOutputDescription() != null) {
            updateWrapper.set("output_description", problemDO.getOutputDescription());
        }
        if (problemDO.getHasInput() != null) {
            updateWrapper.set("has_input", problemDO.getHasInput());
        }
        if (problemDO.getInputFormat() != null) {
            updateWrapper.set("input_format", problemDO.getInputFormat());
        }
        if (problemDO.getExamples() != null) {
            updateWrapper.set("examples", problemDO.getExamples());
        }
        if (problemDO.getStatus() != null) {
            updateWrapper.set("status", problemDO.getStatus());
        }
        if (problemDO.getVisibility() != null) {
            updateWrapper.set("visibility", problemDO.getVisibility());
        }
        if (problemDO.getHints() != null) {
            updateWrapper.set("hints", problemDO.getHints());
        }
        if (problemDO.getConstraints() != null) {
            updateWrapper.set("constraints", problemDO.getConstraints());
        }
        if (problemDO.getNotes() != null) {
            updateWrapper.set("notes", problemDO.getNotes());
        }
        if (problemDO.getMetadata() != null) {
            updateWrapper.set("metadata", problemDO.getMetadata());
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
     */
    @Override
    public List<ProblemStatisticsDTO> getProblemStatistics() {
        try {
            log.info("开始获取题目统计信息");
            List<Object> rawStatistics = problemMapper.getProblemStatistics();

            if (rawStatistics == null) {
                return Collections.emptyList();
            }

            return rawStatistics.stream().filter(Objects::nonNull).map(item -> {
                // 假设 item 是 Map<String, Object> 类型
                if (!(item instanceof Map)) {
                    log.warn("发现非 Map 类型的元素：{}", item.getClass());
                    throw new RuntimeException("非 Map 类型的元素");
                }
                @SuppressWarnings("unchecked") Map<String, Object> data = (Map<String, Object>) item;

                ProblemStatisticsDTO dto = new ProblemStatisticsDTO();

                // 根据SQL查询结果字段映射到DTO属性
                dto.setProblemType((String) data.get("problem_type"));
                dto.setDifficulty((Integer) data.get("difficulty"));
                dto.setTotalCount(((Number) data.get("total_count")).intValue());
                dto.setActiveCount(((Number) data.get("active_count")).intValue());
                dto.setTotalSubmissions(((Number) data.get("total_submissions")).intValue());
                dto.setTotalAccepted(((Number) data.get("total_accepted")).intValue());
                dto.setAvgAcceptanceRate(((Number) data.get("avg_acceptance_rate")).doubleValue());

                return dto;
            }).filter(Objects::nonNull).collect(Collectors.toList());
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
        UpdateWrapper<ProblemDO> updateWrapper = new UpdateWrapper<>();
        // 只更新未删除的题目
        updateWrapper.eq("is_deleted", 0);
        // 限定要更新的ID列表
        updateWrapper.in("id", problemIds);
        // 设置要更新的字段
        updateWrapper.set("status", status);

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
        UpdateWrapper<ProblemDO> updateWrapper = new UpdateWrapper<>();
        // 只更新未删除的题目
        updateWrapper.eq("is_deleted", 0);
        // 限定要更新的ID列表
        updateWrapper.in("id", problemIds);
        // 设置要更新的字段
        updateWrapper.set("is_deleted", 1);

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
        UpdateWrapper<ProblemDO> updateWrapper = new UpdateWrapper<>();
        // 只更新已删除的题目
        updateWrapper.eq("is_deleted", 1);
        // 限定要更新的ID列表
        updateWrapper.in("id", problemIds);
        // 设置要更新的字段
        updateWrapper.set("is_deleted", 0);
        // 使用数据库函数更新时间戳
        updateWrapper.setSql("updated_at = NOW()");

        // 执行批量更新并返回影响的行数
        return problemMapper.update(null, updateWrapper);
    }

    /**
     * 获取指定题目的通过率
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
        queryWrapper.select(
                ProblemDO::getId,
                ProblemDO::getTitle,
                ProblemDO::getTitleEn,
                ProblemDO::getDifficulty,
                ProblemDO::getStatus,
                ProblemDO::getProblemType,
                ProblemDO::getSubmissionCount,
                ProblemDO::getAcceptedCount,
                ProblemDO::getCreatedAt,
                ProblemDO::getUpdatedAt
        );
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
}
