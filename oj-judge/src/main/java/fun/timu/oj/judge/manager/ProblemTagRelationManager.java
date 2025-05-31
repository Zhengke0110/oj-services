package fun.timu.oj.judge.manager;

import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;

import java.util.List;

public interface ProblemTagRelationManager {
    
    /**
     * 根据问题ID查找标签ID列表
     */
    List<Long> findTagIdsByProblemId(Long problemId);

    /**
     * 根据标签ID查找问题ID列表
     */
    List<Long> findProblemIdsByTagId(Long tagId);

    /**
     * 根据问题ID和标签ID查找关系
     */
    ProblemTagRelationDO findByProblemIdAndTagId(Long problemId, Long tagId);

    /**
     * 保存问题标签关系
     */
    int save(ProblemTagRelationDO relationDO);

    /**
     * 根据问题ID和标签ID删除关系
     */
    int deleteByProblemIdAndTagId(Long problemId, Long tagId);

    /**
     * 根据问题ID删除所有相关标签关系
     */
    int deleteByProblemId(Long problemId);

    /**
     * 根据标签ID删除所有相关问题关系
     */
    int deleteByTagId(Long tagId);

    /**
     * 批量保存问题标签关系
     */
    int batchSave(List<ProblemTagRelationDO> relations);

    // ========== 新增方法 ==========

    /**
     * 批量查找问题对应的标签ID
     */
    List<Long> findTagIdsByProblemIds(List<Long> problemIds);

    /**
     * 批量查找标签对应的问题ID
     */
    List<Long> findProblemIdsByTagIds(List<Long> tagIds);

    /**
     * 批量删除指定问题的所有标签关系
     */
    int batchDeleteByProblemIds(List<Long> problemIds);

    /**
     * 批量删除指定标签的所有问题关系
     */
    int batchDeleteByTagIds(List<Long> tagIds);

    /**
     * 统计问题的标签数量
     */
    int countTagsByProblemId(Long problemId);

    /**
     * 统计标签的问题数量
     */
    int countProblemsByTagId(Long tagId);

    /**
     * 检查问题标签关系是否存在
     */
    boolean existsByProblemIdAndTagId(Long problemId, Long tagId);

    /**
     * 批量检查问题标签关系是否存在
     */
    List<Long> findExistingTagIds(Long problemId, List<Long> tagIds);

    /**
     * 批量保存问题标签关系（使用批量插入SQL提高性能）
     */
    int batchSaveOptimized(List<ProblemTagRelationDO> relations);

    /**
     * 为问题设置标签（先删除旧的，再添加新的）
     */
    int setProblemTags(Long problemId, List<Long> tagIds);

    /**
     * 为标签关联问题（先删除旧的，再添加新的）
     */
    int setTagProblems(Long tagId, List<Long> problemIds);
}
