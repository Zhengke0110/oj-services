package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.ProblemTagManager;
import fun.timu.oj.judge.manager.ProblemTagRelationManager;
import fun.timu.oj.judge.mapper.ProblemTagMapper;
import fun.timu.oj.judge.mapper.ProblemTagRelationMapper;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import fun.timu.oj.judge.model.criteria.ProblemTagRelationQueryCondition;
import fun.timu.oj.judge.model.criteria.RelationStatisticsReport;
import fun.timu.oj.judge.model.criteria.TagStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目标签关联管理器实现类
 * 基于MyBatis-Plus实现基础CRUD操作和业务逻辑
 *
 * @author zhengke
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemTagRelationManagerImpl implements ProblemTagRelationManager {
    private final ProblemTagRelationMapper problemTagRelationMapper;
    private final ProblemTagMapper tagMapper;

    /**
     * 根据题目ID列表批量查询标签列表
     *
     * @param problemIds 题目ID列表
     * @return 题目ID到标签列表的映射
     */
    @Override
    public Map<Long, List<ProblemTagDO>> getTagListByProblemIds(List<Long> problemIds) {
        if (CollectionUtils.isEmpty(problemIds)) {
            return new HashMap<>();
        }

        // 初始化结果Map，为每个problemId创建一个空列表
        Map<Long, List<ProblemTagDO>> resultMap = new HashMap<>();
        for (Long problemId : problemIds) {
            resultMap.put(problemId, new ArrayList<>());
        }

        // 查询题目-标签关系
        List<ProblemTagRelationDO> relations = findByProblemIds(problemIds);
        if (relations.isEmpty()) {
            return resultMap; // 返回所有problemId映射到空列表的Map
        }

        // 提取所有需要查询的标签ID
        List<Long> tagIds = relations.stream()
                .map(ProblemTagRelationDO::getTagId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询所有标签对象
        List<ProblemTagDO> allTags = tagMapper.selectBatchIds(tagIds);

        // 构建标签ID到标签对象的映射，便于快速查找
        Map<Long, ProblemTagDO> tagMap = allTags.stream()
                .collect(Collectors.toMap(ProblemTagDO::getId, tag -> tag, (existing, replacement) -> existing));

        // 按题目ID分组，填充结果映射
        for (ProblemTagRelationDO relation : relations) {
            Long problemId = relation.getProblemId();
            Long tagId = relation.getTagId();
            ProblemTagDO tag = tagMap.get(tagId);

            if (tag != null) {
                resultMap.get(problemId).add(tag);
            }
        }
        return resultMap;
    }

    /**
     * 根据id查询题目标签关联
     *
     * @param id 关联id
     * @return 题目标签关联
     */
    @Override
    public ProblemTagRelationDO findById(Long id) {
        return problemTagRelationMapper.selectById(id);
    }

    /**
     * 保存题目标签关联
     *
     * @param problemTagRelationDO 题目标签关联对象
     * @return 保存结果
     */
    @Override
    public int save(ProblemTagRelationDO problemTagRelationDO) {
        // 设置默认软删除状态
        if (problemTagRelationDO.getIsDeleted() == null) {
            problemTagRelationDO.setIsDeleted(0);
        }
        return problemTagRelationMapper.insert(problemTagRelationDO);
    }

    /**
     * 更新题目标签关联
     *
     * @param problemTagRelationDO 题目标签关联对象
     * @return 更新结果
     */
    @Override
    public int updateById(ProblemTagRelationDO problemTagRelationDO) {
        LambdaUpdateWrapper<ProblemTagRelationDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProblemTagRelationDO::getId, problemTagRelationDO.getId());

        // 只有当字段不为null时，才将其添加到更新条件中
        if (problemTagRelationDO.getProblemId() != null) {
            updateWrapper.set(ProblemTagRelationDO::getProblemId, problemTagRelationDO.getProblemId());
        }
        if (problemTagRelationDO.getTagId() != null) {
            updateWrapper.set(ProblemTagRelationDO::getTagId, problemTagRelationDO.getTagId());
        }
        if (problemTagRelationDO.getIsDeleted() != null) {
            updateWrapper.set(ProblemTagRelationDO::getIsDeleted, problemTagRelationDO.getIsDeleted());
        }

        // 如果没有字段需要更新，则直接返回0
        if (updateWrapper.getSqlSet() == null || updateWrapper.getSqlSet().isEmpty()) {
            return 0;
        }

        return problemTagRelationMapper.update(new ProblemTagRelationDO(), updateWrapper);
    }

    /**
     * 删除题目标签关联（软删除）
     *
     * @param id 关联id
     * @return 删除结果
     */
    @Override
    public int deleteById(Long id) {
        ProblemTagRelationDO problemTagRelationDO = new ProblemTagRelationDO();
        problemTagRelationDO.setId(id);
        problemTagRelationDO.setIsDeleted(1);
        return problemTagRelationMapper.updateById(problemTagRelationDO);
    }

    /**
     * 根据题目ID查询所有关联的标签
     *
     * @param problemId 题目ID
     * @return 标签关联列表
     */
    @Override
    public List<ProblemTagRelationDO> findByProblemId(Long problemId) {
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .orderByDesc(ProblemTagRelationDO::getCreatedAt);
        return problemTagRelationMapper.selectList(queryWrapper);
    }

    /**
     * 根据标签ID查询所有关联的题目
     *
     * @param tagId 标签ID
     * @return 题目关联列表
     */
    @Override
    public List<ProblemTagRelationDO> findByTagId(Long tagId) {
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .orderByDesc(ProblemTagRelationDO::getCreatedAt);
        return problemTagRelationMapper.selectList(queryWrapper);
    }

    /**
     * 根据题目ID和标签ID查询关联关系
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 关联关系（如果存在）
     */
    @Override
    public ProblemTagRelationDO findByProblemIdAndTagId(Long problemId, Long tagId) {
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0);
        return problemTagRelationMapper.selectOne(queryWrapper);
    }

    /**
     * 批量为题目添加标签关联
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功添加的关联数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddTagsToProblems(Long problemId, List<Long> tagIds) {
        if (problemId == null || tagIds == null || tagIds.isEmpty()) {
            return 0;
        }

        // 过滤掉已存在的关联，避免重复插入
        List<Long> validTagIds = tagIds.stream()
                .filter(tagId -> tagId != null && !existsRelation(problemId, tagId))
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        if (validTagIds.isEmpty()) {
            return 0;
        }

        // 构建批量插入的关联列表
        List<ProblemTagRelationDO> relations = validTagIds.stream()
                .map(tagId -> {
                    ProblemTagRelationDO relation = new ProblemTagRelationDO();
                    relation.setProblemId(problemId);
                    relation.setTagId(tagId);
                    relation.setIsDeleted(0);
                    return relation;
                })
                .collect(java.util.stream.Collectors.toList());

        // 使用批量插入
        try {
            return batchInsertRelations(relations);
        } catch (Exception e) {
            throw new RuntimeException("Error batch inserting problem tags: " + e.getMessage(), e);
        }
    }

    /**
     * 批量为标签添加题目关联
     *
     * @param tagId      标签ID
     * @param problemIds 题目ID列表
     * @return 成功添加的关联数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddProblemsToTag(Long tagId, List<Long> problemIds) {
        if (tagId == null || problemIds == null || problemIds.isEmpty()) {
            return 0;
        }

        // 过滤掉已存在的关联，避免重复插入
        List<Long> validProblemIds = problemIds.stream()
                .filter(problemId -> problemId != null && !existsRelation(problemId, tagId))
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        if (validProblemIds.isEmpty()) {
            return 0;
        }

        // 构建批量插入的关联列表
        List<ProblemTagRelationDO> relations = validProblemIds.stream()
                .map(problemId -> {
                    ProblemTagRelationDO relation = new ProblemTagRelationDO();
                    relation.setProblemId(problemId);
                    relation.setTagId(tagId);
                    relation.setIsDeleted(0);
                    return relation;
                })
                .collect(java.util.stream.Collectors.toList());

        // 使用批量插入
        try {
            return batchInsertRelations(relations);
        } catch (Exception e) {
            throw new RuntimeException("Error batch inserting tag problems: " + e.getMessage(), e);
        }
    }

    /**
     * 批量删除题目的标签关联
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功删除的关联数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRemoveTagsFromProblem(Long problemId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }

        LambdaUpdateWrapper<ProblemTagRelationDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .in(ProblemTagRelationDO::getTagId, tagIds)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .set(ProblemTagRelationDO::getIsDeleted, 1);

        return problemTagRelationMapper.update(new ProblemTagRelationDO(), updateWrapper);
    }

    /**
     * 批量删除标签的题目关联
     *
     * @param tagId      标签ID
     * @param problemIds 题目ID列表
     * @return 成功删除的关联数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRemoveProblemsFromTag(Long tagId, List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return 0;
        }

        LambdaUpdateWrapper<ProblemTagRelationDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProblemTagRelationDO::getTagId, tagId)
                .in(ProblemTagRelationDO::getProblemId, problemIds)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .set(ProblemTagRelationDO::getIsDeleted, 1);

        return problemTagRelationMapper.update(new ProblemTagRelationDO(), updateWrapper);
    }

    /**
     * 删除题目的所有标签关联
     *
     * @param problemId 题目ID
     * @return 删除的关联数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeAllTagsFromProblem(Long problemId) {
        LambdaUpdateWrapper<ProblemTagRelationDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .set(ProblemTagRelationDO::getIsDeleted, 1);

        return problemTagRelationMapper.update(new ProblemTagRelationDO(), updateWrapper);
    }

    /**
     * 删除标签的所有题目关联
     *
     * @param tagId 标签ID
     * @return 删除的关联数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeAllProblemsFromTag(Long tagId) {
        LambdaUpdateWrapper<ProblemTagRelationDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .set(ProblemTagRelationDO::getIsDeleted, 1);

        return problemTagRelationMapper.update(new ProblemTagRelationDO(), updateWrapper);
    }

    /**
     * 检查题目和标签是否存在关联关系
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return true如果存在关联，false如果不存在
     */
    @Override
    public boolean existsRelation(Long problemId, Long tagId) {
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0);

        return problemTagRelationMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 统计题目关联的标签数量
     *
     * @param problemId 题目ID
     * @return 关联的标签数量
     */
    @Override
    public int countTagsByProblemId(Long problemId) {
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0);

        return Math.toIntExact(problemTagRelationMapper.selectCount(queryWrapper));
    }

    /**
     * 统计标签关联的题目数量
     *
     * @param tagId 标签ID
     * @return 关联的题目数量
     */
    @Override
    public int countProblemsByTagId(Long tagId) {
        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0);

        return Math.toIntExact(problemTagRelationMapper.selectCount(queryWrapper));
    }

    /**
     * 查询没有任何标签的题目ID列表
     *
     * @return 没有标签的题目ID列表
     */
    @Override
    public List<Long> findProblemsWithoutTags() {
        return problemTagRelationMapper.findProblemsWithoutTags();
    }

    /**
     * 查询没有任何题目的标签ID列表
     *
     * @return 没有题目的标签ID列表
     */
    @Override
    public List<Long> findTagsWithoutProblems() {
        return problemTagRelationMapper.findTagsWithoutProblems();
    }

    /**
     * 根据题目ID列表批量查询标签关联
     *
     * @param problemIds 题目ID列表
     * @return 标签关联列表
     */
    @Override
    public List<ProblemTagRelationDO> findByProblemIds(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return List.of();
        }
        return problemTagRelationMapper.findByProblemIds(problemIds);
    }

    /**
     * 根据标签ID列表批量查询题目关联
     *
     * @param tagIds 标签ID列表
     * @return 题目关联列表
     */
    @Override
    public List<ProblemTagRelationDO> findByTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return problemTagRelationMapper.findByTagIds(tagIds);
    }

    /**
     * 批量插入题目标签关联
     *
     * @param relations 关联关系列表
     * @return 插入的行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertRelations(List<ProblemTagRelationDO> relations) {
        if (relations == null || relations.isEmpty()) {
            return 0;
        }

        // 设置默认值
        for (ProblemTagRelationDO relation : relations) {
            if (relation.getIsDeleted() == null) {
                relation.setIsDeleted(0);
            }
        }

        return problemTagRelationMapper.batchInsertRelations(relations);
    }

    /**
     * 替换题目的所有标签关联（先删除所有现有关联，再添加新关联）
     *
     * @param problemId 题目ID
     * @param tagIds    新的标签ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceAllTagsForProblem(Long problemId, List<Long> tagIds) {
        try {
            // 1. 删除现有所有关联
            removeAllTagsFromProblem(problemId);

            // 2. 添加新关联
            if (tagIds != null && !tagIds.isEmpty()) {
                batchAddTagsToProblems(problemId, tagIds);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to replace all tags for problem " + problemId + ": " + e.getMessage(), e);
        }
    }

    /**
     * 替换标签的所有题目关联（先删除所有现有关联，再添加新关联）
     *
     * @param tagId      标签ID
     * @param problemIds 新的题目ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceAllProblemsForTag(Long tagId, List<Long> problemIds) {
        try {
            // 1. 删除现有所有关联
            removeAllProblemsFromTag(tagId);

            // 2. 添加新关联
            if (problemIds != null && !problemIds.isEmpty()) {
                batchAddProblemsToTag(tagId, problemIds);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to replace all problems for tag " + tagId + ": " + e.getMessage(), e);
        }
    }

    /**
     * 统计每个题目的标签数量
     *
     * @param problemIds 题目ID列表
     * @return 题目ID和标签数量的映射
     */
    @Override
    public Map<Long, Long> countTagsByProblemIds(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return java.util.Map.of();
        }

        List<HashMap<String, Object>> results = problemTagRelationMapper.countTagsByProblemIds(problemIds);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        map -> ((Number) map.get("problemId")).longValue(),
                        map -> ((Number) map.get("tagCount")).longValue()
                ));
    }

    /**
     * 统计每个标签的题目数量
     *
     * @param tagIds 标签ID列表
     * @return 标签ID和题目数量的映射
     */
    @Override
    public Map<Long, Long> countProblemsByTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return java.util.Map.of();
        }

        List<HashMap<String, Object>> results = problemTagRelationMapper.countProblemsByTagIds(tagIds);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        map -> ((Number) map.get("tagId")).longValue(),
                        map -> ((Number) map.get("problemCount")).longValue()
                ));
    }

    // ==================== 便捷工具方法实现 ====================

    /**
     * 添加单个标签到题目
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 是否添加成功
     */
    @Override
    public boolean addTagToProblem(Long problemId, Long tagId) {
        if (problemId == null || tagId == null) {
            return false;
        }

        // 检查关联是否已存在
        if (existsRelation(problemId, tagId)) {
            return true; // 已存在，视为成功
        }

        ProblemTagRelationDO relation = new ProblemTagRelationDO();
        relation.setProblemId(problemId);
        relation.setTagId(tagId);
        relation.setIsDeleted(0);

        return problemTagRelationMapper.insert(relation) > 0;
    }

    /**
     * 从题目中移除单个标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 是否移除成功
     */
    @Override
    public boolean removeTagFromProblem(Long problemId, Long tagId) {
        if (problemId == null || tagId == null) {
            return false;
        }

        LambdaUpdateWrapper<ProblemTagRelationDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .set(ProblemTagRelationDO::getIsDeleted, 1);

        return problemTagRelationMapper.update(new ProblemTagRelationDO(), updateWrapper) > 0;
    }

    /**
     * 获取题目的所有标签ID列表
     *
     * @param problemId 题目ID
     * @return 标签ID列表
     */
    @Override
    public List<Long> getTagIdsByProblemId(Long problemId) {
        if (problemId == null) {
            return List.of();
        }

        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .select(ProblemTagRelationDO::getTagId);

        return problemTagRelationMapper.selectList(queryWrapper)
                .stream()
                .map(ProblemTagRelationDO::getTagId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取标签的所有题目ID列表
     *
     * @param tagId 标签ID
     * @return 题目ID列表
     */
    @Override
    public List<Long> getProblemIdsByTagId(Long tagId) {
        if (tagId == null) {
            return List.of();
        }

        LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, 0)
                .select(ProblemTagRelationDO::getProblemId);

        return problemTagRelationMapper.selectList(queryWrapper)
                .stream()
                .map(ProblemTagRelationDO::getProblemId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 检查题目是否有任何标签
     *
     * @param problemId 题目ID
     * @return 是否有标签
     */
    @Override
    public boolean hasAnyTags(Long problemId) {
        return countTagsByProblemId(problemId) > 0;
    }

    /**
     * 检查标签是否有任何题目
     *
     * @param tagId 标签ID
     * @return 是否有题目
     */
    @Override
    public boolean hasAnyProblems(Long tagId) {
        return countProblemsByTagId(tagId) > 0;
    }

    // ==================== 扩展功能实现 ====================

    /**
     * 批量保存问题标签关系（使用批量插入SQL提高性能）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveOptimized(List<ProblemTagRelationDO> relations) {
        if (CollectionUtils.isEmpty(relations)) {
            return 0;
        }

        // 设置创建时间
        LocalDateTime now = LocalDateTime.now();
        relations.forEach(relation -> {
            if (relation.getIsDeleted() == null) {
                relation.setIsDeleted(relation.getIsDeleted());
            }
        });

        return problemTagRelationMapper.batchInsertOptimized(relations);
    }

    /**
     * 批量检查问题标签关系是否存在
     */
    @Override
    public List<Long> findExistingTagIds(Long problemId, List<Long> tagIds) {
        if (problemId == null || CollectionUtils.isEmpty(tagIds)) {
            return new ArrayList<>();
        }

        return problemTagRelationMapper.findExistingTagIds(problemId, tagIds);
    }

    /**
     * 为问题设置标签（先删除旧的，再添加新的）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setProblemTags(Long problemId, List<Long> tagIds) {
        if (problemId == null) {
            return 0;
        }

        // 先删除所有现有关联
        int deletedCount = removeAllTagsFromProblem(problemId);

        // 如果标签列表为空，只删除不添加
        if (CollectionUtils.isEmpty(tagIds)) {
            return deletedCount;
        }

        // 创建新的关联关系
        List<ProblemTagRelationDO> relations = tagIds.stream()
                .map(tagId -> {
                    ProblemTagRelationDO relation = new ProblemTagRelationDO();
                    relation.setProblemId(problemId);
                    relation.setTagId(tagId);
                    relation.setIsDeleted(0);
                    return relation;
                })
                .collect(Collectors.toList());

        int insertedCount = batchSaveOptimized(relations);
        return deletedCount + insertedCount;
    }

    /**
     * 为标签关联问题（先删除旧的，再添加新的）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setTagProblems(Long tagId, List<Long> problemIds) {
        if (tagId == null) {
            return 0;
        }


        // 先删除所有现有关联
        int deletedCount = removeAllProblemsFromTag(tagId);

        // 如果题目列表为空，只删除不添加
        if (CollectionUtils.isEmpty(problemIds)) {
            return deletedCount;
        }

        // 创建新的关联关系
        List<ProblemTagRelationDO> relations = problemIds.stream()
                .map(problemId -> {
                    ProblemTagRelationDO relation = new ProblemTagRelationDO();
                    relation.setProblemId(problemId);
                    relation.setTagId(tagId);
                    relation.setIsDeleted(0);
                    return relation;
                })
                .collect(Collectors.toList());

        int insertedCount = batchSaveOptimized(relations);
        return deletedCount + insertedCount;
    }

    /**
     * 分页查询题目的标签关联
     */
    @Override
    public IPage<ProblemTagRelationDO> findByProblemIdWithPage(Long problemId, int page, int size) {
        if (problemId == null || page < 1 || size < 1) {
            return new Page<>();
        }

        Page<ProblemTagRelationDO> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ProblemTagRelationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProblemTagRelationDO::getProblemId, problemId)
                .eq(ProblemTagRelationDO::getIsDeleted, false)
                .orderByDesc(ProblemTagRelationDO::getCreatedAt);

        Page<ProblemTagRelationDO> result = problemTagRelationMapper.selectPage(pageObj, wrapper);
        return result;
    }

    /**
     * 分页查询标签的题目关联
     */
    @Override
    public IPage<ProblemTagRelationDO> findByTagIdWithPage(Long tagId, int page, int size) {
        if (tagId == null || page < 1 || size < 1) {
            return new Page<>();
        }

        Page<ProblemTagRelationDO> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ProblemTagRelationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProblemTagRelationDO::getTagId, tagId)
                .eq(ProblemTagRelationDO::getIsDeleted, false)
                .orderByDesc(ProblemTagRelationDO::getCreatedAt);

        Page<ProblemTagRelationDO> result = problemTagRelationMapper.selectPage(pageObj, wrapper);
        return result;
    }

    /**
     * 根据多个条件查询关联关系
     */
    @Override
    public List<ProblemTagRelationDO> findByConditions(ProblemTagRelationQueryCondition queryCondition) {
        if (queryCondition == null) {
            return new ArrayList<>();
        }

        return problemTagRelationMapper.findByConditions(queryCondition);
    }

    /**
     * 查询指定时间范围内创建的关联关系
     */
    @Override
    public List<ProblemTagRelationDO> findByCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<ProblemTagRelationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ProblemTagRelationDO::getCreatedAt, startTime, endTime)
                .eq(ProblemTagRelationDO::getIsDeleted, false)
                .orderByDesc(ProblemTagRelationDO::getCreatedAt);

        return problemTagRelationMapper.selectList(wrapper);
    }

    /**
     * 获取热门标签统计（按关联题目数量排序）
     */
    @Override
    public List<TagStatistics> getPopularTags(int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }

        return problemTagRelationMapper.getPopularTags(limit);
    }

    /**
     * 获取题目标签分布统计
     */
    @Override
    public Map<Integer, Long> getTagDistributionStats() {
        List<HashMap<String, Object>> results = problemTagRelationMapper.getTagDistributionStats();
        return results.stream()
                .collect(Collectors.toMap(
                        map -> ((Number) map.get("tagCount")).intValue(),
                        map -> ((Number) map.get("problemCount")).longValue()
                ));
    }

    /**
     * 获取最近活跃的关联关系
     */
    @Override
    public List<ProblemTagRelationDO> getRecentActiveRelations(int days, int limit) {
        if (days <= 0 || limit <= 0) {
            return new ArrayList<>();
        }

        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return problemTagRelationMapper.getRecentActiveRelations(startTime, limit);
    }

    /**
     * 批量修复关联关系的创建时间（如果为空）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fixMissingCreateTime() {

        LambdaUpdateWrapper<ProblemTagRelationDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(ProblemTagRelationDO::getCreatedAt, LocalDateTime.now())
                .isNull(ProblemTagRelationDO::getCreatedAt);

        int count = problemTagRelationMapper.update(null, wrapper);
        return count;
    }

    /**
     * 获取关联关系统计报告
     */
    @Override
    public RelationStatisticsReport getStatisticsReport() {
        RelationStatisticsReport report = new RelationStatisticsReport();

        // 总关联数
        LambdaQueryWrapper<ProblemTagRelationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProblemTagRelationDO::getIsDeleted, false);
        report.setTotalRelations(problemTagRelationMapper.selectCount(wrapper));

        // 无标签题目数和无题目标签数
        report.setProblemsWithoutTags(findProblemsWithoutTags().size());
        report.setTagsWithoutProblems(findTagsWithoutProblems().size());

        // 其他统计数据通过Mapper获取
        Map<String, Object> stats = problemTagRelationMapper.getBasicStatistics();
        if (stats != null) {
            report.setTotalProblems(((Number) stats.getOrDefault("totalProblems", 0)).longValue());
            report.setTotalTags(((Number) stats.getOrDefault("totalTags", 0)).longValue());
            report.setAverageTagsPerProblem(((Number) stats.getOrDefault("avgTagsPerProblem", 0.0)).doubleValue());
            report.setAverageProblemsPerTag(((Number) stats.getOrDefault("avgProblemsPerTag", 0.0)).doubleValue());
        }

        return report;
    }

    /**
     * 查询相似题目（基于共同标签）
     */
    @Override
    public List<Long> findSimilarProblems(Long problemId, int minCommonTags, int limit) {
        if (problemId == null || minCommonTags <= 0 || limit <= 0) {
            return new ArrayList<>();
        }

        return problemTagRelationMapper.findSimilarProblems(problemId, minCommonTags, limit);
    }

    /**
     * 查询标签的相关标签（经常一起出现的标签）
     */
    @Override
    public List<Long> findRelatedTags(Long tagId, int limit) {
        if (tagId == null || limit <= 0) {
            return new ArrayList<>();
        }

        return problemTagRelationMapper.findRelatedTags(tagId, limit);
    }

    /**
     * 批量查询题目的标签名称
     */
    @Override
    public Map<Long, List<String>> getTagNamesByProblemIds(List<Long> problemIds) {
        if (CollectionUtils.isEmpty(problemIds)) {
            return new HashMap<>();
        }

        List<HashMap<String, Object>> results = problemTagRelationMapper.getTagNamesByProblemIds(problemIds);
        Map<Long, List<String>> resultMap = new HashMap<>();

        for (Map<String, Object> result : results) {
            Long problemId = ((Number) result.get("problemId")).longValue();
            String tagName = (String) result.get("tagName");

            resultMap.computeIfAbsent(problemId, k -> new ArrayList<>()).add(tagName);
        }

        return resultMap;
    }

    // ==================== 数据一致性检查方法实现 ====================

    /**
     * 清理孤立的关联记录（题目或标签已不存在）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanOrphanedRelations() {
        log.info("开始清理孤立的关联记录");
        int count = problemTagRelationMapper.cleanOrphanedRelations();
        log.info("清理孤立关联记录数: {}", count);
        return count;
    }

    /**
     * 修复软删除状态不一致的记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fixInconsistentDeleteStatus() {
        log.info("开始修复软删除状态不一致的记录");
        int count = problemTagRelationMapper.fixInconsistentDeleteStatus();
        log.info("修复不一致删除状态的记录数: {}", count);
        return count;
    }

    /**
     * 获取重复的关联记录
     */
    @Override
    public List<ProblemTagRelationDO> findDuplicateRelations() {
        return problemTagRelationMapper.findDuplicateRelations();
    }

    /**
     * 合并重复的关联记录，保留最新的一条
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int mergeDuplicateRelations() {
        log.info("开始合并重复的关联记录");
        int count = problemTagRelationMapper.mergeDuplicateRelations();
        log.info("合并重复关联记录数: {}", count);
        return count;
    }


}
