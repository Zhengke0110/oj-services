package fun.timu.oj.judge.controller;

import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.exception.BizException;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.service.ProblemTagRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题目标签关联控制器
 * 提供题目标签关联的REST API接口
 *
 * @author zhengke
 */
@Slf4j
@RestController
@RequestMapping("/api/judge/relation")
@RequiredArgsConstructor
public class ProblemTagRelationController {
    private final ProblemTagRelationService problemTagRelationService;

    // ==================== 标签关联操作接口 ====================

    /**
     * 为题目添加标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 操作结果
     */
    @PostMapping("/problem/{problemId}/tag/{tagId}")
    public JsonData addTagToProblem(
            @PathVariable Long problemId,
            @PathVariable Long tagId) {
        try {
            boolean result = problemTagRelationService.addTagToProblem(problemId, tagId);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("Error adding tag to problem: problemId={}, tagId={}, error={}",
                    problemId, tagId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 从题目移除标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 操作结果
     */
    @DeleteMapping("/problem/{problemId}/tag/{tagId}")
    public JsonData removeTagFromProblem(
            @PathVariable Long problemId,
            @PathVariable Long tagId) {
        try {
            boolean result = problemTagRelationService.removeTagFromProblem(problemId, tagId);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("Error removing tag from problem: problemId={}, tagId={}, error={}",
                    problemId, tagId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 批量为题目添加标签
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功添加的数量
     */
    @PostMapping("/problem/{problemId}/tags/batch")
    public JsonData batchAddTagsToProblem(
            @PathVariable Long problemId,
            @RequestBody List<Long> tagIds) {
        try {
            int result = problemTagRelationService.batchAddTagsToProblem(problemId, tagIds);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("Error batch adding tags to problem: problemId={}, tagIds={}, error={}",
                    problemId, tagIds, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 批量从题目移除标签
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功移除的数量
     */
    @DeleteMapping("/problem/{problemId}/tags/batch")
    public JsonData batchRemoveTagsFromProblem(
            @PathVariable Long problemId,
            @RequestBody List<Long> tagIds) {
        try {
            int result = problemTagRelationService.batchRemoveTagsFromProblem(problemId, tagIds);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("Error batch removing tags from problem: problemId={}, tagIds={}, error={}",
                    problemId, tagIds, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 替换题目的所有标签
     *
     * @param problemId 题目ID
     * @param tagIds    新的标签ID列表
     * @return 操作结果
     */
    @PutMapping("/problem/{problemId}/tags")
    public JsonData replaceAllTagsForProblem(
            @PathVariable Long problemId,
            @RequestBody List<Long> tagIds) {
        try {
            boolean result = problemTagRelationService.replaceAllTagsForProblem(problemId, tagIds);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("Error replacing tags for problem: problemId={}, tagIds={}, error={}",
                    problemId, tagIds, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    // ==================== 查询接口 ====================

    /**
     * 获取题目的所有标签ID
     *
     * @param problemId 题目ID
     * @return 标签ID列表
     */
    @GetMapping("/problem/{problemId}/tags")
    public JsonData getTagIdsByProblemId(@PathVariable Long problemId) {
        try {
            List<Long> tagIds = problemTagRelationService.getTagIdsByProblemId(problemId);
            return JsonData.buildSuccess(tagIds);
        } catch (Exception e) {
            log.error("Error getting tag IDs for problem: problemId={}, error={}",
                    problemId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 获取标签的所有题目ID
     *
     * @param tagId 标签ID
     * @return 题目ID列表
     */
    @GetMapping("/tag/{tagId}/problems")
    public JsonData getProblemIdsByTagId(@PathVariable Long tagId) {
        try {
            List<Long> problemIds = problemTagRelationService.getProblemIdsByTagId(tagId);
            return JsonData.buildSuccess(problemIds);
        } catch (Exception e) {
            log.error("Error getting problem IDs for tag: tagId={}, error={}",
                    tagId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 检查题目是否有指定标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 是否存在关联
     */
    @GetMapping("/problem/{problemId}/tag/{tagId}/exists")
    public JsonData hasTagRelation(
            @PathVariable Long problemId,
            @PathVariable Long tagId) {
        try {
            boolean exists = problemTagRelationService.hasTagRelation(problemId, tagId);
            return JsonData.buildSuccess(exists);
        } catch (Exception e) {
            log.error("Error checking tag relation: problemId={}, tagId={}, error={}",
                    problemId, tagId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 检查题目是否有任何标签
     *
     * @param problemId 题目ID
     * @return 是否有标签
     */
    @GetMapping("/problem/{problemId}/has-tags")
    public JsonData hasAnyTags(@PathVariable Long problemId) {
        try {
            boolean hasTags = problemTagRelationService.hasAnyTags(problemId);
            return JsonData.buildSuccess(hasTags);
        } catch (Exception e) {
            log.error("Error checking if problem has any tags: problemId={}, error={}",
                    problemId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    // ==================== 统计接口 ====================

    /**
     * 统计题目的标签数量
     *
     * @param problemId 题目ID
     * @return 标签数量
     */
    @GetMapping("/problem/{problemId}/tag-count")
    public JsonData countTagsByProblemId(@PathVariable Long problemId) {
        try {
            long count = problemTagRelationService.countTagsByProblemId(problemId);
            return JsonData.buildSuccess(count);
        } catch (Exception e) {
            log.error("Error counting tags for problem: problemId={}, error={}",
                    problemId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 统计标签的题目数量
     *
     * @param tagId 标签ID
     * @return 题目数量
     */
    @GetMapping("/tag/{tagId}/problem-count")
    public JsonData countProblemsByTagId(@PathVariable Long tagId) {
        try {
            long count = problemTagRelationService.countProblemsByTagId(tagId);
            return JsonData.buildSuccess(count);
        } catch (Exception e) {
            log.error("Error counting problems for tag: tagId={}, error={}",
                    tagId, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 批量统计题目的标签数量
     *
     * @param problemIds 题目ID列表
     * @return 题目ID -> 标签数量的映射
     */
    @PostMapping("/problems/tag-counts")
    public JsonData batchCountTagsByProblemIds(@RequestBody List<Long> problemIds) {
        try {
            Map<Long, Long> countMap = problemTagRelationService.batchCountTagsByProblemIds(problemIds);
            return JsonData.buildSuccess(countMap);
        } catch (Exception e) {
            log.error("Error batch counting tags for problems: problemIds={}, error={}",
                    problemIds, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 批量统计标签的题目数量
     *
     * @param tagIds 标签ID列表
     * @return 标签ID -> 题目数量的映射
     */
    @PostMapping("/tags/problem-counts")
    public JsonData batchCountProblemsByTagIds(@RequestBody List<Long> tagIds) {
        try {
            Map<Long, Long> countMap = problemTagRelationService.batchCountProblemsByTagIds(tagIds);
            return JsonData.buildSuccess(countMap);
        } catch (Exception e) {
            log.error("Error batch counting problems for tags: tagIds={}, error={}",
                    tagIds, e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    // ==================== 数据维护接口 ====================

    /**
     * 查找没有任何标签的题目
     *
     * @return 没有标签的题目ID列表
     */
    @GetMapping("/problems/without-tags")
    public JsonData findProblemsWithoutTags() {
        try {
            List<Long> problemIds = problemTagRelationService.findProblemsWithoutTags();
            return JsonData.buildSuccess(problemIds);
        } catch (Exception e) {
            log.error("Error finding problems without tags: error={}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 查找没有任何题目的标签
     *
     * @return 没有题目的标签ID列表
     */
    @GetMapping("/tags/without-problems")
    public JsonData findTagsWithoutProblems() {
        try {
            List<Long> tagIds = problemTagRelationService.findTagsWithoutProblems();
            return JsonData.buildSuccess(tagIds);
        } catch (Exception e) {
            log.error("Error finding tags without problems: error={}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 清理孤立的关联记录
     *
     * @return 清理的记录数量
     */
    @PostMapping("/maintenance/clean-orphaned")
    public JsonData cleanOrphanedRelations() {
        try {
            int cleanedCount = problemTagRelationService.cleanOrphanedRelations();
            return JsonData.buildSuccess(cleanedCount);
        } catch (Exception e) {
            log.error("Error cleaning orphaned relations: error={}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 修复数据一致性问题
     *
     * @return 修复的记录数量
     */
    @PostMapping("/maintenance/fix-consistency")
    public JsonData fixDataConsistency() {
        try {
            int fixedCount = problemTagRelationService.fixDataConsistency();
            return JsonData.buildSuccess(fixedCount);
        } catch (Exception e) {
            log.error("Error fixing data consistency: error={}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }
}
