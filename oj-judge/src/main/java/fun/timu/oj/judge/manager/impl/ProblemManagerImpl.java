package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;

import java.util.List;

public class ProblemManagerImpl implements ProblemManager {
    private final ProblemMapper problemMapper;

    public ProblemManagerImpl(ProblemMapper problemMapper) {
        this.problemMapper = problemMapper;
    }

    /**
     * 根据问题ID查找问题详情
     * <p>
     * 此方法通过MyBatis-Plus提供的方法，从数据库中根据ID查询并返回问题详情
     * 使用@Override注解表明此方法重写了父类或接口的方法
     *
     * @param id 问题的唯一标识符，用于数据库查询
     * @return 返回查询到的问题对象，如果没有找到则返回null
     */
    @Override
    public ProblemDO findById(Long id) {
        return problemMapper.selectById(id);
    }


    /**
     * 根据条件分页查询问题列表
     *
     * @param current     当前页码
     * @param size        每页大小
     * @param problemType 问题类型，如果为null，则不按问题类型过滤
     * @param difficulty  难度，如果为null，则不按难度过滤
     * @param status      状态，如果为null，则不按状态过滤
     * @return 返回分页的问题列表
     */
    @Override
    public Page<ProblemDO> findPageList(int current, int size, String problemType, Integer difficulty, Integer status) {
        // 初始化分页对象
        Page<ProblemDO> page = new Page<>(current, size);
        // 创建查询条件包装器
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 默认查询未删除的记录
        queryWrapper.eq(ProblemDO::getIsDeleted, false);

        // 按条件过滤
        if (problemType != null) {
            queryWrapper.eq(ProblemDO::getProblemType, problemType);
        }
        if (difficulty != null) {
            queryWrapper.eq(ProblemDO::getDifficulty, difficulty);
        }
        if (status != null) {
            queryWrapper.eq(ProblemDO::getStatus, status);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(ProblemDO::getCreatedAt);

        // 执行分页查询并返回结果
        return problemMapper.selectPage(page, queryWrapper);
    }


    /**
     * 根据创建者ID查询问题列表
     * <p>
     * 此方法构造了一个Lambda查询包装器，以查询与给定创建者ID相关且未被删除的问题
     * 它确保了查询结果是按创建时间降序排列的
     *
     * @param creatorId 创建者的ID，用于查询问题
     * @return 返回一个ProblemDO对象列表，包含符合条件的问题
     */
    @Override
    public List<ProblemDO> findByCreatorId(Long creatorId) {
        // 创建Lambda查询包装器实例
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：创建者ID匹配且问题未被删除，并指定结果排序方式
        queryWrapper.eq(ProblemDO::getCreatorId, creatorId).eq(ProblemDO::getIsDeleted, false).orderByDesc(ProblemDO::getCreatedAt);
        // 执行查询并返回结果列表
        return problemMapper.selectList(queryWrapper);
    }


    /**
     * 根据标题关键词搜索问题
     * <p>
     * 此方法用于在数据库中搜索与给定关键词在标题或英文标题中匹配的问题它只返回未删除且状态为已启用的问题
     *
     * @param keyword 要搜索的关键词
     * @return 匹配的问题列表
     */
    @Override
    public List<ProblemDO> searchByTitle(String keyword) {
        // 创建Lambda查询包装器以用于构建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件：标题或英文标题中包含关键词，且未删除、状态为已启用的问题，按创建时间降序排序
        queryWrapper.like(ProblemDO::getTitle, keyword).or().like(ProblemDO::getTitleEn, keyword)
                .eq(ProblemDO::getIsDeleted, false).eq(ProblemDO::getStatus, 1) // 只搜索已启用的题目
                .orderByDesc(ProblemDO::getCreatedAt);

        // 执行查询并返回结果列表
        return problemMapper.selectList(queryWrapper);
    }


    /**
     * 保存问题信息到数据库中
     * <p>
     * 此方法负责将一个问题对象（ProblemDO）插入到数据库中它主要用于问题信息的持久化
     *
     * @param problemDO 要插入的问题对象包含问题的详细信息
     * @return 插入操作影响的行数，通常为1表示成功，0表示失败
     */
    @Override
    public int save(ProblemDO problemDO) {
        return problemMapper.insert(problemDO);
    }


    /**
     * 根据ID更新问题信息
     *
     * @param problemDO 包含更新信息的问题数据对象
     * @return 更新操作影响的行数
     */
    @Override
    public int updateById(ProblemDO problemDO) {
        return problemMapper.updateById(problemDO);
    }


    /**
     * 根据ID删除问题
     * 逻辑删除问题记录，通过设置is_deleted标志为1来标记该记录为已删除
     *
     * @param id 问题的唯一标识符
     * @return 更新操作影响的行数，1表示删除成功，0表示删除失败或记录不存在
     */
    @Override
    public int deleteById(Long id) {
        // 创建一个ProblemDO对象，用于封装要更新的数据
        ProblemDO problemDO = new ProblemDO();
        // 设置问题的ID
        problemDO.setId(id);
        // 设置问题的删除标志为1，表示已删除
        problemDO.setIsDeleted(1);
        // 调用problemMapper的updateById方法，根据ID更新问题的删除状态
        return problemMapper.updateById(problemDO);
    }


    /**
     * 更新题目提交统计信息
     *
     * @param problemId  题目ID
     * @param isAccepted 提交是否被接受
     * @return 更新操作的影响行数
     */
    @Override
    public int updateSubmissionStats(Long problemId, boolean isAccepted) {
        // 根据题目ID查询题目信息
        ProblemDO problemDO = problemMapper.selectById(problemId);
        // 如果题目信息存在，则更新提交计数和接受计数
        if (problemDO != null) {
            // 增加提交计数
            problemDO.setSubmissionCount(problemDO.getSubmissionCount() + 1);
            // 如果提交被接受，则增加接受计数
            if (isAccepted) {
                problemDO.setAcceptedCount(problemDO.getAcceptedCount() + 1);
            }
            // 更新题目的提交和接受计数
            return problemMapper.updateById(problemDO);
        }
        // 如果题目信息不存在，返回0表示未进行更新操作
        return 0;
    }

}
