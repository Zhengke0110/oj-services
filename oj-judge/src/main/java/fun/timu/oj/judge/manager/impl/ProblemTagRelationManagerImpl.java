package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import fun.timu.oj.judge.manager.ProblemTagRelationManager;
import fun.timu.oj.judge.mapper.ProblemTagRelationMapper;
import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 设置创建时间
        if (problemTagRelationDO.getCreatedAt() == null) {
            problemTagRelationDO.setCreatedAt(new Date());
        }
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
        Date now = new Date();
        List<ProblemTagRelationDO> relations = validTagIds.stream()
                .map(tagId -> {
                    ProblemTagRelationDO relation = new ProblemTagRelationDO();
                    relation.setProblemId(problemId);
                    relation.setTagId(tagId);
                    relation.setIsDeleted(0);
                    relation.setCreatedAt(now);
                    return relation;
                })
                .collect(java.util.stream.Collectors.toList());

        // 使用批量插入
        try {
            return batchInsertRelations(relations);
        } catch (Exception e) {
            log.error("Failed to batch add tags {} to problem {}: {}", validTagIds, problemId, e.getMessage(), e);
            return 0;
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
        Date now = new Date();
        List<ProblemTagRelationDO> relations = validProblemIds.stream()
                .map(problemId -> {
                    ProblemTagRelationDO relation = new ProblemTagRelationDO();
                    relation.setProblemId(problemId);
                    relation.setTagId(tagId);
                    relation.setIsDeleted(0);
                    relation.setCreatedAt(now);
                    return relation;
                })
                .collect(java.util.stream.Collectors.toList());

        // 使用批量插入
        try {
            return batchInsertRelations(relations);
        } catch (Exception e) {
            log.error("Failed to batch add problems {} to tag {}: {}", validProblemIds, tagId, e.getMessage(), e);
            return 0;
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
        Date now = new Date();
        for (ProblemTagRelationDO relation : relations) {
            if (relation.getCreatedAt() == null) {
                relation.setCreatedAt(now);
            }
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
            log.error("Failed to replace all tags for problem {}: {}", problemId, e.getMessage(), e);
            return false;
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
            log.error("Failed to replace all problems for tag {}: {}", tagId, e.getMessage(), e);
            return false;
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
        relation.setCreatedAt(new Date());

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

    // ==================== 数据一致性检查方法实现 ====================

    /**
     * 清理孤立的关联记录（题目或标签已不存在）
     * 注意：此方法需要配合题目表和标签表的数据进行清理
     *
     * @return 清理的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanOrphanedRelations() {
        try {
            // 这里需要根据实际的题目表和标签表来清理孤立记录
            // 由于我们无法访问这些表，先返回0，具体实现需要业务层提供
            log.info("Cleaning orphaned relations - this method needs to be implemented with actual problem and tag table access");
            return 0;
        } catch (Exception e) {
            log.error("Failed to clean orphaned relations: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 修复软删除状态不一致的记录
     *
     * @return 修复的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fixInconsistentDeleteStatus() {
        try {
            // 查找可能存在的不一致状态并修复
            // 这里可以添加具体的修复逻辑
            log.info("Fixing inconsistent delete status - implementation depends on specific business rules");
            return 0;
        } catch (Exception e) {
            log.error("Failed to fix inconsistent delete status: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取重复的关联记录（同一题目-标签对有多条记录）
     *
     * @return 重复的关联记录列表
     */
    @Override
    public List<ProblemTagRelationDO> findDuplicateRelations() {
        try {
            // 查找重复记录的逻辑可以通过SQL实现
            // 这里提供一个基本的查询逻辑
            LambdaQueryWrapper<ProblemTagRelationDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProblemTagRelationDO::getIsDeleted, 0)
                    .orderByAsc(ProblemTagRelationDO::getProblemId)
                    .orderByAsc(ProblemTagRelationDO::getTagId)
                    .orderByDesc(ProblemTagRelationDO::getCreatedAt);

            List<ProblemTagRelationDO> allRelations = problemTagRelationMapper.selectList(queryWrapper);
            List<ProblemTagRelationDO> duplicates = new java.util.ArrayList<>();

            // 检查重复
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (ProblemTagRelationDO relation : allRelations) {
                String key = relation.getProblemId() + ":" + relation.getTagId();
                if (!seen.add(key)) {
                    duplicates.add(relation);
                }
            }

            return duplicates;
        } catch (Exception e) {
            log.error("Failed to find duplicate relations: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 合并重复的关联记录，保留最新的一条
     *
     * @return 合并的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int mergeDuplicateRelations() {
        try {
            List<ProblemTagRelationDO> duplicates = findDuplicateRelations();
            if (duplicates.isEmpty()) {
                return 0;
            }

            int mergedCount = 0;
            // 按problem_id和tag_id分组，保留最新的记录，删除其他的
            java.util.Map<String, List<ProblemTagRelationDO>> groupedRelations = duplicates.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            relation -> relation.getProblemId() + ":" + relation.getTagId()
                    ));

            for (List<ProblemTagRelationDO> group : groupedRelations.values()) {
                if (group.size() > 1) {
                    // 按创建时间排序，保留最新的
                    group.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

                    // 删除除第一个以外的所有记录
                    for (int i = 1; i < group.size(); i++) {
                        if (deleteById(group.get(i).getId()) > 0) {
                            mergedCount++;
                        }
                    }
                }
            }

            return mergedCount;
        } catch (Exception e) {
            log.error("Failed to merge duplicate relations: {}", e.getMessage(), e);
            return 0;
        }
    }
}
