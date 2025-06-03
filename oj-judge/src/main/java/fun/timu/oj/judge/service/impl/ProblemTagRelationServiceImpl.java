package fun.timu.oj.judge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.oj.judge.manager.ProblemTagRelationManager;
import fun.timu.oj.judge.mapper.ProblemTagRelationMapper;
import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import fun.timu.oj.judge.service.ProblemTagRelationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhengke
 * @description 针对表【problem_tag_relation(题目标签关联表)】的数据库操作Service实现
 * @createDate 2025-06-03 10:56:18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemTagRelationServiceImpl extends ServiceImpl<ProblemTagRelationMapper, ProblemTagRelationDO>
        implements ProblemTagRelationService {

    private final ProblemTagRelationManager problemTagRelationManager;

    // ==================== 业务逻辑方法实现 ====================

    /**
     * 为题目添加标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 操作结果
     */
    @Override
    public boolean addTagToProblem(Long problemId, Long tagId) {
        try {
            if (problemId == null || tagId == null) {
                log.warn("Invalid parameters: problemId={}, tagId={}", problemId, tagId);
                return false;
            }

            // 检查是否已存在关联
            if (problemTagRelationManager.existsRelation(problemId, tagId)) {
                log.info("Relation already exists: problemId={}, tagId={}", problemId, tagId);
                return true;
            }

            boolean result = problemTagRelationManager.addTagToProblem(problemId, tagId);

            if (result) {
                log.info("Successfully added tag to problem: problemId={}, tagId={}", problemId, tagId);
            } else {
                log.warn("Failed to add tag to problem: problemId={}, tagId={}", problemId, tagId);
            }

            return result;
        } catch (Exception e) {
            log.error("Error adding tag to problem: problemId={}, tagId={}, error={}",
                    problemId, tagId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从题目移除标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 操作结果
     */
    @Override
    public boolean removeTagFromProblem(Long problemId, Long tagId) {
        try {
            if (problemId == null || tagId == null) {
                log.warn("Invalid parameters: problemId={}, tagId={}", problemId, tagId);
                return false;
            }

            boolean result = problemTagRelationManager.removeTagFromProblem(problemId, tagId);

            if (result) {
                log.info("Successfully removed tag from problem: problemId={}, tagId={}", problemId, tagId);
            } else {
                log.warn("Failed to remove tag from problem or relation not found: problemId={}, tagId={}",
                        problemId, tagId);
            }

            return result;
        } catch (Exception e) {
            log.error("Error removing tag from problem: problemId={}, tagId={}, error={}",
                    problemId, tagId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量为题目添加标签
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功添加的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddTagsToProblem(Long problemId, List<Long> tagIds) {
        try {
            if (problemId == null || tagIds == null || tagIds.isEmpty()) {
                log.warn("Invalid parameters: problemId={}, tagIds={}", problemId, tagIds);
                return 0;
            }

            // 过滤掉null值
            List<Long> validTagIds = tagIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (validTagIds.isEmpty()) {
                log.warn("No valid tag IDs provided for problemId={}", problemId);
                return 0;
            }

            int result = problemTagRelationManager.batchAddTagsToProblems(problemId, validTagIds);

            log.info("Batch added tags to problem: problemId={}, requestedCount={}, addedCount={}",
                    problemId, validTagIds.size(), result);

            return result;
        } catch (Exception e) {
            log.error("Error batch adding tags to problem: problemId={}, tagIds={}, error={}",
                    problemId, tagIds, e.getMessage(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

    /**
     * 批量从题目移除标签
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功移除的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRemoveTagsFromProblem(Long problemId, List<Long> tagIds) {
        try {
            if (problemId == null || tagIds == null || tagIds.isEmpty()) {
                log.warn("Invalid parameters: problemId={}, tagIds={}", problemId, tagIds);
                return 0;
            }

            // 过滤掉null值
            List<Long> validTagIds = tagIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (validTagIds.isEmpty()) {
                log.warn("No valid tag IDs provided for problemId={}", problemId);
                return 0;
            }

            int result = problemTagRelationManager.batchRemoveTagsFromProblem(problemId, validTagIds);

            log.info("Batch removed tags from problem: problemId={}, requestedCount={}, removedCount={}",
                    problemId, validTagIds.size(), result);

            return result;
        } catch (Exception e) {
            log.error("Error batch removing tags from problem: problemId={}, tagIds={}, error={}",
                    problemId, tagIds, e.getMessage(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

    /**
     * 替换题目的所有标签
     *
     * @param problemId 题目ID
     * @param tagIds    新的标签ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceAllTagsForProblem(Long problemId, List<Long> tagIds) {
        try {
            if (problemId == null) {
                log.warn("Invalid problemId: {}", problemId);
                return false;
            }

            // 处理空标签列表的情况
            List<Long> validTagIds = tagIds == null ? List.of() :
                    tagIds.stream()
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toList());

            boolean result = problemTagRelationManager.replaceAllTagsForProblem(problemId, validTagIds);

            if (result) {
                log.info("Successfully replaced all tags for problem: problemId={}, newTagCount={}",
                        problemId, validTagIds.size());
            } else {
                log.warn("Failed to replace tags for problem: problemId={}", problemId);
            }

            return result;
        } catch (Exception e) {
            log.error("Error replacing tags for problem: problemId={}, tagIds={}, error={}",
                    problemId, tagIds, e.getMessage(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

    /**
     * 获取题目的所有标签ID
     *
     * @param problemId 题目ID
     * @return 标签ID列表
     */
    @Override
    public List<Long> getTagIdsByProblemId(Long problemId) {
        try {
            if (problemId == null) {
                log.warn("Invalid problemId: {}", problemId);
                return List.of();
            }

            List<Long> tagIds = problemTagRelationManager.getTagIdsByProblemId(problemId);

            log.debug("Retrieved tag IDs for problem: problemId={}, tagCount={}",
                    problemId, tagIds.size());

            return tagIds;
        } catch (Exception e) {
            log.error("Error retrieving tag IDs for problem: problemId={}, error={}",
                    problemId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取标签的所有题目ID
     *
     * @param tagId 标签ID
     * @return 题目ID列表
     */
    @Override
    public List<Long> getProblemIdsByTagId(Long tagId) {
        try {
            if (tagId == null) {
                log.warn("Invalid tagId: {}", tagId);
                return List.of();
            }

            List<Long> problemIds = problemTagRelationManager.getProblemIdsByTagId(tagId);

            log.debug("Retrieved problem IDs for tag: tagId={}, problemCount={}",
                    tagId, problemIds.size());

            return problemIds;
        } catch (Exception e) {
            log.error("Error retrieving problem IDs for tag: tagId={}, error={}",
                    tagId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 检查题目是否有指定标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 是否存在关联
     */
    @Override
    public boolean hasTagRelation(Long problemId, Long tagId) {
        try {
            if (problemId == null || tagId == null) {
                log.warn("Invalid parameters: problemId={}, tagId={}", problemId, tagId);
                return false;
            }

            boolean exists = problemTagRelationManager.existsRelation(problemId, tagId);

            log.debug("Checked tag relation: problemId={}, tagId={}, exists={}",
                    problemId, tagId, exists);

            return exists;
        } catch (Exception e) {
            log.error("Error checking tag relation: problemId={}, tagId={}, error={}",
                    problemId, tagId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查题目是否有任何标签
     *
     * @param problemId 题目ID
     * @return 是否有标签
     */
    @Override
    public boolean hasAnyTags(Long problemId) {
        try {
            if (problemId == null) {
                log.warn("Invalid problemId: {}", problemId);
                return false;
            }

            boolean hasTags = problemTagRelationManager.hasAnyTags(problemId);

            log.debug("Checked if problem has any tags: problemId={}, hasTags={}",
                    problemId, hasTags);

            return hasTags;
        } catch (Exception e) {
            log.error("Error checking if problem has any tags: problemId={}, error={}",
                    problemId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 统计题目的标签数量
     *
     * @param problemId 题目ID
     * @return 标签数量
     */
    @Override
    public long countTagsByProblemId(Long problemId) {
        try {
            if (problemId == null) {
                log.warn("Invalid problemId: {}", problemId);
                return 0;
            }

            long count = problemTagRelationManager.countTagsByProblemId(problemId);

            log.debug("Counted tags for problem: problemId={}, tagCount={}",
                    problemId, count);

            return count;
        } catch (Exception e) {
            log.error("Error counting tags for problem: problemId={}, error={}",
                    problemId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计标签的题目数量
     *
     * @param tagId 标签ID
     * @return 题目数量
     */
    @Override
    public long countProblemsByTagId(Long tagId) {
        try {
            if (tagId == null) {
                log.warn("Invalid tagId: {}", tagId);
                return 0;
            }

            long count = problemTagRelationManager.countProblemsByTagId(tagId);

            log.debug("Counted problems for tag: tagId={}, problemCount={}",
                    tagId, count);

            return count;
        } catch (Exception e) {
            log.error("Error counting problems for tag: tagId={}, error={}",
                    tagId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 批量统计题目的标签数量
     *
     * @param problemIds 题目ID列表
     * @return 题目ID -> 标签数量的映射
     */
    @Override
    public Map<Long, Long> batchCountTagsByProblemIds(List<Long> problemIds) {
        try {
            if (problemIds == null || problemIds.isEmpty()) {
                log.warn("Invalid problemIds: {}", problemIds);
                return Map.of();
            }

            // 过滤掉null值
            List<Long> validProblemIds = problemIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (validProblemIds.isEmpty()) {
                log.warn("No valid problem IDs provided");
                return Map.of();
            }

            Map<Long, Long> countMap = problemTagRelationManager.countTagsByProblemIds(validProblemIds);

            log.debug("Batch counted tags for problems: requestedCount={}, resultCount={}",
                    validProblemIds.size(), countMap.size());

            return countMap;
        } catch (Exception e) {
            log.error("Error batch counting tags for problems: problemIds={}, error={}",
                    problemIds, e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 批量统计标签的题目数量
     *
     * @param tagIds 标签ID列表
     * @return 标签ID -> 题目数量的映射
     */
    @Override
    public Map<Long, Long> batchCountProblemsByTagIds(List<Long> tagIds) {
        try {
            if (tagIds == null || tagIds.isEmpty()) {
                log.warn("Invalid tagIds: {}", tagIds);
                return Map.of();
            }

            // 过滤掉null值
            List<Long> validTagIds = tagIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (validTagIds.isEmpty()) {
                log.warn("No valid tag IDs provided");
                return Map.of();
            }

            Map<Long, Long> countMap = problemTagRelationManager.countProblemsByTagIds(validTagIds);

            log.debug("Batch counted problems for tags: requestedCount={}, resultCount={}",
                    validTagIds.size(), countMap.size());

            return countMap;
        } catch (Exception e) {
            log.error("Error batch counting problems for tags: tagIds={}, error={}",
                    tagIds, e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 查找没有任何标签的题目
     *
     * @return 没有标签的题目ID列表
     */
    @Override
    public List<Long> findProblemsWithoutTags() {
        try {
            List<Long> problemIds = problemTagRelationManager.findProblemsWithoutTags();

            log.info("Found problems without tags: count={}", problemIds.size());

            return problemIds;
        } catch (Exception e) {
            log.error("Error finding problems without tags: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 查找没有任何题目的标签
     *
     * @return 没有题目的标签ID列表
     */
    @Override
    public List<Long> findTagsWithoutProblems() {
        try {
            List<Long> tagIds = problemTagRelationManager.findTagsWithoutProblems();

            log.info("Found tags without problems: count={}", tagIds.size());

            return tagIds;
        } catch (Exception e) {
            log.error("Error finding tags without problems: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 清理孤立的关联记录
     *
     * @return 清理的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanOrphanedRelations() {
        try {
            int cleanedCount = problemTagRelationManager.cleanOrphanedRelations();

            log.info("Cleaned orphaned relations: count={}", cleanedCount);

            return cleanedCount;
        } catch (Exception e) {
            log.error("Error cleaning orphaned relations: error={}", e.getMessage(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

    /**
     * 修复数据一致性问题
     *
     * @return 修复的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fixDataConsistency() {
        try {
            int fixedCount = 0;

            // 修复软删除状态不一致的记录
            int fixedDeleteStatus = problemTagRelationManager.fixInconsistentDeleteStatus();
            fixedCount += fixedDeleteStatus;

            // 合并重复的关联记录
            int mergedDuplicates = problemTagRelationManager.mergeDuplicateRelations();
            fixedCount += mergedDuplicates;

            log.info("Fixed data consistency issues: deletedStatusFixed={}, duplicatesMerged={}, totalFixed={}",
                    fixedDeleteStatus, mergedDuplicates, fixedCount);

            return fixedCount;
        } catch (Exception e) {
            log.error("Error fixing data consistency: error={}", e.getMessage(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

}




