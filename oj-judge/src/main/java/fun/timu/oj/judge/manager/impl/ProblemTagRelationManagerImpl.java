package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.timu.oj.judge.manager.ProblemTagRelationManager;
import fun.timu.oj.judge.mapper.ProblemTagRelationMapper;
import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProblemTagRelationManagerImpl implements ProblemTagRelationManager {
    private final ProblemTagRelationMapper problemTagRelationMapper;

    public ProblemTagRelationManagerImpl(ProblemTagRelationMapper problemTagRelationMapper) {
        this.problemTagRelationMapper = problemTagRelationMapper;
    }

    /**
     * 根据问题ID查找标签ID列表
     *
     * @param problemId 问题的唯一标识符
     * @return 包含标签ID的列表
     */
    @Override
    public List<Long> findTagIdsByProblemId(Long problemId) {
        // 创建一个Lambda查询包装器，用于构建查询条件和选择返回的列
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：问题ID等于参数problemId且未删除
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getIsDeleted, false)
                .select(ProblemTagRelationDO::getTagId);

        // 执行查询，获取符合条件的问题-标签关系列表
        List<ProblemTagRelationDO> relations = problemTagRelationMapper.selectList(queryWrapper);
        // 使用Java Stream API从关系列表中提取标签ID，并收集到一个新的列表中返回
        return relations.stream()
                .map(ProblemTagRelationDO::getTagId)
                .collect(Collectors.toList());
    }


    /**
     * 根据标签ID查找关联的问题ID列表
     *
     * @param tagId 标签ID，用于查询与标签关联的问题
     * @return 返回一个Long类型的列表，包含与指定标签ID关联的所有问题ID
     */
    @Override
    public List<Long> findProblemIdsByTagId(Long tagId) {
        // 创建LambdaQueryWrapper对象，用于构建查询条件
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：根据标签ID和是否删除字段进行筛选，只选择问题ID字段
        queryWrapper.eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, false)
                .select(ProblemTagRelationDO::getProblemId);

        // 执行查询，获取与标签ID关联的问题标签关系列表
        List<ProblemTagRelationDO> relations = problemTagRelationMapper.selectList(queryWrapper);
        // 使用Stream API处理查询结果，提取问题ID并收集到列表中
        return relations.stream()
                .map(ProblemTagRelationDO::getProblemId)
                .collect(Collectors.toList());
    }


    /**
     * 根据问题ID和标签ID查找问题标签关系
     *
     * @param problemId 问题ID
     * @param tagId     标签ID
     * @return 如果找到对应的问题标签关系且未被删除，则返回该关系对象，否则返回null
     */
    @Override
    public ProblemTagRelationDO findByProblemIdAndTagId(Long problemId, Long tagId) {
        // 创建查询条件构造器
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：问题ID等于参数problemId、标签ID等于参数tagId且未被删除
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, false);
        // 执行查询并返回结果
        return problemTagRelationMapper.selectOne(queryWrapper);
    }


    /**
     * 保存问题与标签的关联关系
     * <p>
     * 此方法负责将问题与标签之间的关联关系持久化到数据库中通过调用problemTagRelationMapper的insert方法实现
     *
     * @param relationDO 问题与标签关联关系的数据对象，包含需要保存的关联信息
     * @return 返回影响的行数，通常为1表示成功插入，否则表示插入失败
     */
    @Override
    public int save(ProblemTagRelationDO relationDO) {
        return problemTagRelationMapper.insert(relationDO);
    }


    /**
     * 根据问题ID和标签ID删除问题标签关系
     * <p>
     * 该方法通过设置问题标签关系的is_deleted标志为1来实现逻辑删除
     * 使用LambdaQueryWrapper构建查询条件，以匹配指定的问题ID和标签ID
     *
     * @param problemId 问题ID，用于定位问题标签关系
     * @param tagId     标签ID，用于定位问题标签关系
     * @return 返回受影响的行数，表示删除操作影响的记录数
     */
    @Override
    public int deleteByProblemIdAndTagId(Long problemId, Long tagId) {
        // 构建查询条件，用于定位要删除的问题标签关系
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getTagId, tagId);

        // 创建一个更新对象，设置is_deleted标志为1，表示已删除
        ProblemTagRelationDO updateObj = new ProblemTagRelationDO();
        updateObj.setIsDeleted(1);

        // 执行更新操作，返回受影响的行数
        return problemTagRelationMapper.update(updateObj, queryWrapper);
    }


    /**
     * 根据问题ID删除问题标签关系
     * <p>
     * 该方法通过设置问题标签关系的is_deleted标志为1来实现逻辑删除
     * 使用LambdaQueryWrapper构建查询条件，确保只删除与给定问题ID匹配的记录
     *
     * @param problemId 问题的唯一标识符
     * @return 返回受影响的行数，表示删除操作影响的记录数量
     */
    @Override
    public int deleteByProblemId(Long problemId) {
        // 构建查询条件，选择问题ID匹配的记录
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId);

        // 创建一个待更新的对象，并设置其is_deleted字段为1，表示已删除
        ProblemTagRelationDO updateObj = new ProblemTagRelationDO();
        updateObj.setIsDeleted(1);

        // 执行更新操作，返回受影响的行数
        return problemTagRelationMapper.update(updateObj, queryWrapper);
    }


    /**
     * 根据标签ID删除问题与标签的关系
     * <p>
     * 该方法通过设置问题与标签关系的is_deleted标志为1，来实现逻辑删除
     * 使用LambdaQueryWrapper构建查询条件，以匹配具有指定标签ID的关系
     *
     * @param tagId 标签ID，用于标识要删除的关系
     * @return 返回受影响的行数，表示删除操作影响了多少条数据
     */
    @Override
    public int deleteByTagId(Long tagId) {
        // 构建查询条件，选择具有指定标签ID的问题与标签关系
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getTagId, tagId);

        // 创建一个用于更新的对象，并设置is_deleted标志为1，表示已删除
        ProblemTagRelationDO updateObj = new ProblemTagRelationDO();
        updateObj.setIsDeleted(1);

        // 执行更新操作，返回受影响的行数
        return problemTagRelationMapper.update(updateObj, queryWrapper);
    }


    /**
     * 批量保存问题标签关系
     * <p>
     * 通过遍历问题标签关系列表，逐一插入每个关系到数据库中，并统计插入成功的数量
     * 此方法主要用于在批量操作中高效地保存多个问题标签关系，例如在导入数据或批量创建问题时
     *
     * @param relations 问题标签关系列表，包含多个ProblemTagRelationDO对象，表示待保存的问题标签关系
     * @return 插入成功的记录数量，表示成功保存的问题标签关系数目
     */
    @Override
    public int batchSave(List<ProblemTagRelationDO> relations) {
        int count = 0;
        for (ProblemTagRelationDO relation : relations) {
            count += problemTagRelationMapper.insert(relation);
        }
        return count;
    }

}
