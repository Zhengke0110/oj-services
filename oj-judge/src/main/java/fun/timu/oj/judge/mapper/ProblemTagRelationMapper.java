package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.timu.oj.judge.model.criteria.ProblemTagRelationQueryCondition;
import fun.timu.oj.judge.model.criteria.TagStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProblemTagRelationMapper extends BaseMapper<ProblemTagRelationDO> {

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
     * 批量插入题目标签关联
     *
     * @param relations 关联关系列表
     * @return 插入的行数
     */
    int batchInsertRelations(@Param("relations") List<ProblemTagRelationDO> relations);

    /**
     * 根据题目ID列表查询所有相关的标签关联
     *
     * @param problemIds 题目ID列表
     * @return 标签关联列表
     */
    List<ProblemTagRelationDO> findByProblemIds(@Param("problemIds") List<Long> problemIds);

    /**
     * 根据标签ID列表查询所有相关的题目关联
     *
     * @param tagIds 标签ID列表
     * @return 题目关联列表
     */
    List<ProblemTagRelationDO> findByTagIds(@Param("tagIds") List<Long> tagIds);

    /**
     * 统计每个题目的标签数量
     *
     * @param problemIds 题目ID列表
     * @return 题目ID和标签数量的映射
     */
    List<HashMap<String, Object>> countTagsByProblemIds(@Param("problemIds") List<Long> problemIds);

    /**
     * 统计每个标签的题目数量
     *
     * @param tagIds 标签ID列表
     * @return 标签ID和题目数量的映射
     */
    List<HashMap<String, Object>> countProblemsByTagIds(@Param("tagIds") List<Long> tagIds);

    // ==================== 扩展功能方法 ====================

    /**
     * 批量保存问题标签关系（优化版本）
     *
     * @param relations 关联关系列表
     * @return 插入的行数
     */
    int batchInsertOptimized(@Param("relations") List<ProblemTagRelationDO> relations);

    /**
     * 批量检查问题标签关系是否存在
     *
     * @param problemId 问题ID
     * @param tagIds    标签ID列表
     * @return 已存在的标签ID列表
     */
    List<Long> findExistingTagIds(@Param("problemId") Long problemId, @Param("tagIds") List<Long> tagIds);

    /**
     * 根据多个条件查询关联关系
     *
     * @param queryCondition 查询条件对象
     * @return 符合条件的关联列表
     */
    List<ProblemTagRelationDO> findByConditions(@Param("condition") ProblemTagRelationQueryCondition queryCondition);

    /**
     * 获取热门标签统计（按关联题目数量排序）
     *
     * @param limit 返回数量限制
     * @return 标签统计列表
     */
    List<TagStatistics> getPopularTags(@Param("limit") int limit);

    /**
     * 获取题目标签分布统计
     *
     * @return 标签数量分布统计
     */
    List<HashMap<String, Object>> getTagDistributionStats();

    /**
     * 获取最近活跃的关联关系
     *
     * @param startTime 开始时间
     * @param limit     返回数量限制
     * @return 最近活跃的关联关系列表
     */
    List<ProblemTagRelationDO> getRecentActiveRelations(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 获取基础统计数据
     *
     * @return 统计数据映射
     */
    HashMap<String, Object> getBasicStatistics();

    /**
     * 查询相似题目（基于共同标签）
     *
     * @param problemId     题目ID
     * @param minCommonTags 最少共同标签数
     * @param limit         返回数量限制
     * @return 相似题目ID列表
     */
    List<Long> findSimilarProblems(@Param("problemId") Long problemId, @Param("minCommonTags") int minCommonTags, @Param("limit") int limit);

    /**
     * 查询标签的相关标签（经常一起出现的标签）
     *
     * @param tagId 标签ID
     * @param limit 返回数量限制
     * @return 相关标签ID列表
     */
    List<Long> findRelatedTags(@Param("tagId") Long tagId, @Param("limit") int limit);

    /**
     * 批量查询题目的标签名称
     *
     * @param problemIds 题目ID列表
     * @return 题目和标签名称的映射列表
     */
    List<HashMap<String, Object>> getTagNamesByProblemIds(@Param("problemIds") List<Long> problemIds);

    /**
     * 查找重复的关联记录
     *
     * @return 重复的关联记录列表
     */
    List<ProblemTagRelationDO> findDuplicateRelations();

    /**
     * 合并重复的关联记录
     *
     * @return 合并的记录数量
     */
    int mergeDuplicateRelations();

    /**
     * 清理孤立的关联记录
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
}




