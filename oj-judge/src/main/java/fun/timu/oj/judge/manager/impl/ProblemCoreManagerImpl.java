package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.manager.ProblemCoreManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 题目核心管理器实现类
 * 负责题目的基础CRUD操作和核心业务逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemCoreManagerImpl implements ProblemCoreManager {

    private final ProblemMapper problemMapper;

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    @Override
    public ProblemDO getById(Long id) {
        return problemMapper.selectById(id);
    }

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
     * @return 分页结果
     */
    @Override
    public IPage<ProblemDO> findTagListWithPage(int pageNum, int pageSize, String problemType, Integer difficulty, Integer status, Integer visibility, List<String> supportedLanguages, Boolean hasInput, Double MinAcceptanceRate, Double MaxAcceptanceRate) {
        // 创建分页对象
        Page<ProblemDO> page = new Page<>(pageNum, pageSize);

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

        // 支持的编程语言筛选
        if (supportedLanguages != null && !supportedLanguages.isEmpty()) {
            StringBuilder jsonCondition = new StringBuilder("(");
            for (int i = 0; i < supportedLanguages.size(); i++) {
                if (i > 0) jsonCondition.append(" OR ");
                jsonCondition.append("JSON_SEARCH(supported_languages, 'one', '").append(supportedLanguages.get(i)).append("') IS NOT NULL");
            }
            jsonCondition.append(")");
            queryWrapper.apply(jsonCondition.toString());
        }

        // 是否有输入筛选
        if (hasInput != null) {
            queryWrapper.eq(ProblemDO::getHasInput, hasInput);
        }

        // 通过率筛选
        if (MinAcceptanceRate != null || MaxAcceptanceRate != null) {
            boolean needAcceptanceRateFilter = false;

            if (MinAcceptanceRate != null && MinAcceptanceRate > 0.0) {
                needAcceptanceRateFilter = true;
            }

            if (MaxAcceptanceRate != null && MaxAcceptanceRate < 1.0) {
                needAcceptanceRateFilter = true;
            }

            if (needAcceptanceRateFilter) {
                queryWrapper.gt(ProblemDO::getSubmissionCount, 0);

                if (MinAcceptanceRate != null && MinAcceptanceRate > 0.0) {
                    queryWrapper.apply("accepted_count >= submission_count * {0}", MinAcceptanceRate);
                }

                if (MaxAcceptanceRate != null && MaxAcceptanceRate < 1.0) {
                    queryWrapper.apply("accepted_count <= submission_count * {0}", MaxAcceptanceRate);
                }
            }
        }

        return problemMapper.selectPage(page, queryWrapper);
    }

    /**
     * 根据创建者id查询题目列表
     *
     * @param creatorId 创建者id
     * @return 题目列表
     */
    @Override
    public List<ProblemDO> findByCreatorId(Long creatorId) {
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemDO::getCreatorId, creatorId);
        return problemMapper.selectList(queryWrapper);
    }

    /**
     * 保存题目
     *
     * @param problem 题目信息
     * @return 保存结果
     */
    @Override
    public int save(ProblemDO problem) {
        return problemMapper.insert(problem);
    }

    /**
     * 更新题目
     *
     * @param problem 题目信息
     * @return 更新结果
     */
    @Override
    public int updateById(ProblemDO problem) {
        return problemMapper.updateById(problem);
    }

    /**
     * 删除题目
     *
     * @param id 题目ID
     * @return 删除结果
     */
    @Override
    public int deleteById(Long id) {
        return problemMapper.deleteById(id);
    }

    /**
     * 检查指定标题的题目是否已存在
     *
     * @param title 题目标题
     * @return 如果存在返回true，否则返回false
     */
    @Override
    public boolean existsByTitle(String title) {
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemDO::getTitle, title);
        return problemMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 更新题目提交统计
     *
     * @param problemId  题目ID
     * @param loginUser  登录用户
     * @param isAccepted 是否通过
     * @return 更新的记录数
     */
    @Override
    public int updateSubmissionStats(Long problemId, LoginUser loginUser, boolean isAccepted) {
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
    }


    /**
     * 根据创建者查询题目数量
     *
     * @param creatorId 创建者ID
     * @return 题目数量
     */
    @Override
    public Long countByCreator(Long creatorId) {
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemDO::getCreatorId, creatorId);
        Integer row = problemMapper.selectCount(queryWrapper);
        return row.longValue();
    }

    /**
     * 查询最近创建的题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param limit    限制返回的题目总数（可为 null，表示无上限）
     * @return 分页结果
     */
    @Override
    public IPage<ProblemDO> selectRecentProblems(int pageNum, int pageSize, Integer limit) {
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
    }

    /**
     * 根据支持的编程语言查询题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param language 编程语言
     * @return 分页结果
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
     * 查询题目的通过率
     *
     * @param problemId 题目ID
     * @return 通过率（小数形式，如0.6表示60%）
     */
    @Override
    public Double getAcceptanceRate(Long problemId) {
        ProblemDO problem = problemMapper.selectById(problemId);
        if (problem == null || problem.getSubmissionCount() == 0) {
            return 0.0;
        }
        return (double) problem.getAcceptedCount() / problem.getSubmissionCount() * 100;
    }


    /**
     * 批量获取题目的基本信息
     *
     * @param problemIds 题目ID列表
     * @return 题目信息列表
     */
    @Override
    public List<ProblemDO> selectBasicInfoByIds(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProblemDO::getId, problemIds).select(ProblemDO::getId, ProblemDO::getTitle, ProblemDO::getDifficulty, ProblemDO::getSubmissionCount, ProblemDO::getSubmissionCount);
        return problemMapper.selectList(queryWrapper);
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

        // 创建分页对象
        Page<ProblemDO> page = new Page<>(pageNum, pageSize);

        // 创建查询条件构造器
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 设置查询条件：提交数为0且未删除的题目
        queryWrapper.eq(ProblemDO::getSubmissionCount, 0).eq(ProblemDO::getIsDeleted, false).orderByDesc(ProblemDO::getCreatedAt); // 按创建时间降序排Ï列

        // 执行分页查询
        IPage<ProblemDO> problemPage = problemMapper.selectPage(page, queryWrapper);

        return problemPage;
    }
}