package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemManagerImpl implements ProblemManager {
    private final ProblemMapper problemMapper;

    /**
     * 根据id查询问题
     *
     * @param id 问题id
     * @return 问题
     */
    @Override
    public ProblemDO getById(Long id) {
        return problemMapper.selectById(id);
    }


    /**
     * 根据多个筛选条件分页查询问题列表
     *
     * @param current            当前页码
     * @param size               每页大小
     * @param problemType        问题类型
     * @param difficulty         难度
     * @param status             状态
     * @param supportedLanguages 支持的编程语言列表
     * @param hasInput           是否有输入
     * @param MinAcceptanceRate  最小通过率
     * @param MaxAcceptanceRate  最大通过率
     * @return 分页的问题列表
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
     * 根据创建者ID查询问题列表
     * <p>
     * 此方法旨在通过创建者ID筛选出未删除的问题，并按照创建时间降序排列
     * 选择使用LambdaQueryWrapper是为了提高查询条件编写的可读性和维护性
     *
     * @param creatorId 创建者ID，用于筛选问题的创建者
     * @return 返回由创建者创建的、未删除的问题列表如果creatorId为null，则返回空列表
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
     * 保存问题信息到数据库中
     * <p>
     * 此方法负责将一个问题数据对象（ProblemDO）插入到数据库中它主要用于问题的创建或更新操作
     * 通过调用problemMapper的insert方法来实现数据的插入功能
     *
     * @param problemDO 问题数据对象，包含需要保存的问题信息
     * @return 插入操作的结果，通常是一个表示受影响行数的整数
     */
    @Override
    public int save(ProblemDO problemDO) {
        return problemMapper.insert(problemDO);
    }

    /**
     * 根据ID更新问题信息
     *
     * @param problemDO 包含要更新的问题信息的对象
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
     * 根据ID删除问题
     * 实际上，这个方法通过将问题标记为已删除来实现软删除它并不真正从数据库中删除记录
     * 软删除是一种常见的做法，可以保持数据的完整性，避免真正删除后无法恢复
     *
     * @param id 问题的唯一标识符
     *           这个参数用于标识数据库中的特定问题记录
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

}
