package fun.timu.oj.judge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import fun.timu.oj.judge.model.VO.ProblemTagVO;

import java.util.List;
import java.util.Map;

/**
 * @author zhengke
 * @description 针对表【problem_tag_relation(题目标签关联表)】的数据库操作Service
 * @createDate 2025-06-03 10:56:18
 */
public interface ProblemTagRelationService extends IService<ProblemTagRelationDO> {

    // ==================== 业务逻辑方法 ====================

    /**
     * 为题目添加标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 操作结果
     */
    boolean addTagToProblem(Long problemId, Long tagId);

    /**
     * 从题目移除标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 操作结果
     */
    boolean removeTagFromProblem(Long problemId, Long tagId);

    /**
     * 批量为题目添加标签
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功添加的数量
     */
    int batchAddTagsToProblem(Long problemId, List<Long> tagIds);

    /**
     * 批量从题目移除标签
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功移除的数量
     */
    int batchRemoveTagsFromProblem(Long problemId, List<Long> tagIds);

    /**
     * 替换题目的所有标签
     *
     * @param problemId 题目ID
     * @param tagIds    新的标签ID列表
     * @return 操作结果
     */
    boolean replaceAllTagsForProblem(Long problemId, List<Long> tagIds);

    /**
     * 获取题目的所有标签ID
     *
     * @param problemId 题目ID
     * @return 标签ID列表
     */
    @Deprecated
    List<Long> getTagIdsByProblemId(Long problemId);

    /**
     * 获取标签的所有题目ID
     *
     * @param tagId 标签ID
     * @return 题目ID列表
     */
    List<Long> getProblemIdsByTagId(Long tagId);

    /**
     * 检查题目是否有指定标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 是否存在关联
     */
    boolean hasTagRelation(Long problemId, Long tagId);

    /**
     * 检查题目是否有任何标签
     *
     * @param problemId 题目ID
     * @return 是否有标签
     */
    boolean hasAnyTags(Long problemId);

    /**
     * 统计题目的标签数量
     *
     * @param problemId 题目ID
     * @return 标签数量
     */
    long countTagsByProblemId(Long problemId);

    /**
     * 统计标签的题目数量
     *
     * @param tagId 标签ID
     * @return 题目数量
     */
    long countProblemsByTagId(Long tagId);

    /**
     * 批量统计题目的标签数量
     *
     * @param problemIds 题目ID列表
     * @return 题目ID -> 标签数量的映射
     */
    Map<Long, Long> batchCountTagsByProblemIds(List<Long> problemIds);

    /**
     * 批量统计标签的题目数量
     *
     * @param tagIds 标签ID列表
     * @return 标签ID -> 题目数量的映射
     */
    Map<Long, Long> batchCountProblemsByTagIds(List<Long> tagIds);

    /**
     * 查找没有任何标签的题目
     *
     * @return 没有标签的题目ID列表
     */
    List<Long> findProblemsWithoutTags();

    /**
     * 查找没有任何题目的标签
     *
     * @return 没有题目的标签ID列表
     */
    List<Long> findTagsWithoutProblems();

    /**
     * 清理孤立的关联记录
     *
     * @return 清理的记录数量
     */
    int cleanOrphanedRelations();

    /**
     * 修复数据一致性问题
     *
     * @return 修复的记录数量
     */
    int fixDataConsistency();

}
