package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import fun.timu.oj.judge.model.criteria.ProblemTagRelationQueryCondition;
import fun.timu.oj.judge.model.criteria.RelationStatisticsReport;
import fun.timu.oj.judge.model.criteria.TagStatistics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 题目标签关联管理器接口
 * 提供题目标签关联表的基础CRUD操作和业务逻辑
 *
 * @author zhengke
 */
public interface ProblemTagRelationManager {

    /**
     * 根据id查询题目标签关联
     *
     * @param id 关联id
     * @return 题目标签关联
     */
    ProblemTagRelationDO findById(Long id);

    /**
     * 保存题目标签关联
     *
     * @param problemTagRelationDO 题目标签关联对象
     * @return 保存结果
     */
    int save(ProblemTagRelationDO problemTagRelationDO);

    /**
     * 更新题目标签关联
     *
     * @param problemTagRelationDO 题目标签关联对象
     * @return 更新结果
     */
    int updateById(ProblemTagRelationDO problemTagRelationDO);

    /**
     * 删除题目标签关联（软删除）
     *
     * @param id 关联id
     * @return 删除结果
     */
    int deleteById(Long id);

    /**
     * 根据题目ID查询所有关联的标签
     *
     * @param problemId 题目ID
     * @return 标签关联列表
     */
    List<ProblemTagRelationDO> findByProblemId(Long problemId);

    /**
     * 根据标签ID查询所有关联的题目
     *
     * @param tagId 标签ID
     * @return 题目关联列表
     */
    List<ProblemTagRelationDO> findByTagId(Long tagId);

    /**
     * 根据题目ID和标签ID查询关联关系
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 关联关系（如果存在）
     */
    ProblemTagRelationDO findByProblemIdAndTagId(Long problemId, Long tagId);

    /**
     * 批量为题目添加标签关联
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功添加的关联数量
     */
    int batchAddTagsToProblems(Long problemId, List<Long> tagIds);

    /**
     * 批量为标签添加题目关联
     *
     * @param tagId      标签ID
     * @param problemIds 题目ID列表
     * @return 成功添加的关联数量
     */
    int batchAddProblemsToTag(Long tagId, List<Long> problemIds);

    /**
     * 批量删除题目的标签关联
     *
     * @param problemId 题目ID
     * @param tagIds    标签ID列表
     * @return 成功删除的关联数量
     */
    int batchRemoveTagsFromProblem(Long problemId, List<Long> tagIds);

    /**
     * 批量删除标签的题目关联
     *
     * @param tagId      标签ID
     * @param problemIds 题目ID列表
     * @return 成功删除的关联数量
     */
    int batchRemoveProblemsFromTag(Long tagId, List<Long> problemIds);

    /**
     * 删除题目的所有标签关联
     *
     * @param problemId 题目ID
     * @return 删除的关联数量
     */
    int removeAllTagsFromProblem(Long problemId);

    /**
     * 删除标签的所有题目关联
     *
     * @param tagId 标签ID
     * @return 删除的关联数量
     */
    int removeAllProblemsFromTag(Long tagId);

    /**
     * 检查题目和标签是否存在关联关系
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return true如果存在关联，false如果不存在
     */
    boolean existsRelation(Long problemId, Long tagId);

    /**
     * 统计题目关联的标签数量
     *
     * @param problemId 题目ID
     * @return 关联的标签数量
     */
    int countTagsByProblemId(Long problemId);

    /**
     * 统计标签关联的题目数量
     *
     * @param tagId 标签ID
     * @return 关联的题目数量
     */
    int countProblemsByTagId(Long tagId);

    /**
     * 查询没有任何标签的题目ID列表
     *
     * @return 没有标签的题目ID列表
     */
    List<Long> findProblemsWithoutTags();

    /**
     * 查询没有任何题目的标签ID列表
     *
     * @return 没有题目的标签ID列表
     */
    List<Long> findTagsWithoutProblems();

    /**
     * 根据题目ID列表批量查询标签关联
     *
     * @param problemIds 题目ID列表
     * @return 标签关联列表
     */
    List<ProblemTagRelationDO> findByProblemIds(List<Long> problemIds);

    /**
     * 根据标签ID列表批量查询题目关联
     *
     * @param tagIds 标签ID列表
     * @return 题目关联列表
     */
    List<ProblemTagRelationDO> findByTagIds(List<Long> tagIds);

    /**
     * 批量插入题目标签关联
     *
     * @param relations 关联关系列表
     * @return 插入的行数
     */
    int batchInsertRelations(List<ProblemTagRelationDO> relations);

    /**
     * 替换题目的所有标签关联（先删除所有现有关联，再添加新关联）
     *
     * @param problemId 题目ID
     * @param tagIds    新的标签ID列表
     * @return 操作结果
     */
    boolean replaceAllTagsForProblem(Long problemId, List<Long> tagIds);

    /**
     * 替换标签的所有题目关联（先删除所有现有关联，再添加新关联）
     *
     * @param tagId      标签ID
     * @param problemIds 新的题目ID列表
     * @return 操作结果
     */
    boolean replaceAllProblemsForTag(Long tagId, List<Long> problemIds);

    /**
     * 统计每个题目的标签数量
     *
     * @param problemIds 题目ID列表
     * @return 题目ID和标签数量的映射
     */
    Map<Long, Long> countTagsByProblemIds(List<Long> problemIds);

    /**
     * 统计每个标签的题目数量
     *
     * @param tagIds 标签ID列表
     * @return 标签ID和题目数量的映射
     */
    Map<Long, Long> countProblemsByTagIds(List<Long> tagIds);

    // ==================== 便捷工具方法 ====================

    /**
     * 添加单个标签到题目
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 是否添加成功
     */
    boolean addTagToProblem(Long problemId, Long tagId);

    /**
     * 从题目中移除单个标签
     *
     * @param problemId 题目ID
     * @param tagId     标签ID
     * @return 是否移除成功
     */
    boolean removeTagFromProblem(Long problemId, Long tagId);

    /**
     * 获取题目的所有标签ID列表
     *
     * @param problemId 题目ID
     * @return 标签ID列表
     */
    List<Long> getTagIdsByProblemId(Long problemId);

    /**
     * 获取标签的所有题目ID列表
     *
     * @param tagId 标签ID
     * @return 题目ID列表
     */
    List<Long> getProblemIdsByTagId(Long tagId);

    /**
     * 检查题目是否有任何标签
     *
     * @param problemId 题目ID
     * @return 是否有标签
     */
    boolean hasAnyTags(Long problemId);

    /**
     * 检查标签是否有任何题目
     *
     * @param tagId 标签ID
     * @return 是否有题目
     */
    boolean hasAnyProblems(Long tagId);

    // ==================== 数据一致性检查方法 ====================

    /**
     * 清理孤立的关联记录（题目或标签已不存在）
     * 注意：此方法需要配合题目表和标签表的数据进行清理
     *
     * @return 清理的记录数量
     */
    int cleanOrphanedRelations();

    /**
     * 修复软删除状态不一致的记录
     *
     * @return 修复的记录数量
     */
    int fixInconsistentDeleteStatus();

    /**
     * 获取重复的关联记录（同一题目-标签对有多条记录）
     *
     * @return 重复的关联记录列表
     */
    List<ProblemTagRelationDO> findDuplicateRelations();

    /**
     * 合并重复的关联记录，保留最新的一条
     *
     * @return 合并的记录数量
     */
    int mergeDuplicateRelations();

    // ==================== 性能优化功能扩展 ====================

    /**
     * 批量保存问题标签关系（使用批量插入SQL提高性能）
     * 相比普通批量保存，该方法针对大量数据插入进行了优化
     *
     * @param relations 关联关系列表
     * @return 插入的行数
     */
    int batchSaveOptimized(List<ProblemTagRelationDO> relations);

    /**
     * 批量检查问题标签关系是否存在
     * 用于在批量操作前检查已存在的关联，避免重复插入
     *
     * @param problemId 问题ID
     * @param tagIds    标签ID列表
     * @return 已存在的标签ID列表
     */
    List<Long> findExistingTagIds(Long problemId, List<Long> tagIds);

    /**
     * 为问题设置标签（先删除旧的，再添加新的）
     * 这是一个事务性操作，确保数据一致性
     *
     * @param problemId 题目ID
     * @param tagIds    新的标签ID列表
     * @return 操作影响的行数
     */
    int setProblemTags(Long problemId, List<Long> tagIds);

    /**
     * 为标签关联问题（先删除旧的，再添加新的）
     * 这是一个事务性操作，确保数据一致性
     *
     * @param tagId      标签ID
     * @param problemIds 新的问题ID列表
     * @return 操作影响的行数
     */
    int setTagProblems(Long tagId, List<Long> problemIds);

    // ==================== 分页查询功能 ====================

    /**
     * 分页查询题目的标签关联
     *
     * @param problemId 题目ID
     * @param page      页码（从1开始）
     * @param size      每页大小
     * @return 分页结果
     */
    IPage<ProblemTagRelationDO> findByProblemIdWithPage(Long problemId, int page, int size);

    /**
     * 分页查询标签的题目关联
     *
     * @param tagId 标签ID
     * @param page  页码（从1开始）
     * @param size  每页大小
     * @return 分页结果
     */
    IPage<ProblemTagRelationDO> findByTagIdWithPage(Long tagId, int page, int size);

    // ==================== 条件查询功能 ====================

    /**
     * 根据多个条件查询关联关系
     *
     * @param queryCondition 查询条件对象
     * @return 符合条件的关联列表
     */
    List<ProblemTagRelationDO> findByConditions(ProblemTagRelationQueryCondition queryCondition);

    /**
     * 查询指定时间范围内创建的关联关系
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 关联关系列表
     */
    List<ProblemTagRelationDO> findByCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 统计分析功能 ====================

    /**
     * 获取热门标签统计（按关联题目数量排序）
     *
     * @param limit 返回数量限制
     * @return 标签统计列表（按数量降序）
     */
    List<TagStatistics> getPopularTags(int limit);

    /**
     * 获取题目标签分布统计
     *
     * @return 标签数量分布统计
     */
    Map<Integer, Long> getTagDistributionStats();

    /**
     * 获取最近活跃的关联关系
     *
     * @param days  最近天数
     * @param limit 返回数量限制
     * @return 最近活跃的关联关系列表
     */
    List<ProblemTagRelationDO> getRecentActiveRelations(int days, int limit);

    // ==================== 数据维护功能 ====================

    /**
     * 批量修复关联关系的创建时间（如果为空）
     *
     * @return 修复的关联关系数量
     */
    int fixMissingCreateTime();

    /**
     * 获取关联关系统计报告
     *
     * @return 统计报告对象
     */
    RelationStatisticsReport getStatisticsReport();

    // ==================== 便捷查询方法 ====================

    /**
     * 查询相似题目（基于共同标签）
     *
     * @param problemId     题目ID
     * @param minCommonTags 最少共同标签数
     * @param limit         返回数量限制
     * @return 相似题目ID列表
     */
    List<Long> findSimilarProblems(Long problemId, int minCommonTags, int limit);

    /**
     * 查询标签的相关标签（经常一起出现的标签）
     *
     * @param tagId 标签ID
     * @param limit 返回数量限制
     * @return 相关标签ID列表
     */
    List<Long> findRelatedTags(Long tagId, int limit);

    /**
     * 批量查询题目的标签名称
     *
     * @param problemIds 题目ID列表
     * @return 题目ID到标签名称列表的映射
     */
    Map<Long, List<String>> getTagNamesByProblemIds(List<Long> problemIds);
}
