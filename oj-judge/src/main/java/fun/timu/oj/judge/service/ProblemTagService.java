package fun.timu.oj.judge.service;

import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.model.Enums.TagCategoryEnum;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VTO.TagUsageStatisticsVTO;
import fun.timu.oj.judge.model.VTO.CategoryAggregateStatisticsVTO;

import java.util.List;
import java.util.Map;


/**
 * 问题标签服务接口
 */
public interface ProblemTagService {
    /**
     * 创建标签
     */
    JsonData createTag(ProblemTagCreateRequest request);

    /**
     * 更新标签
     */
    JsonData updateTag(ProblemTagUpdateRequest request);

    /**
     * 删除标签
     */
    JsonData deleteTag(Long id);

    /**
     * 根据ID获取标签
     */
    JsonData getTagById(Long id);

    /**
     * 分页查询标签
     */
    JsonData listTags(int current, int size, String tagName, Boolean isEnabled, String tagColor);

    /**
     * 获取所有启用的标签
     */
    JsonData getAllEnabledTags();

    /**
     * 获取标签使用统计
     */
    JsonData getTagUsageStatistics(TagCategoryEnum category);

    /**
     * 获取所有分类的标签使用聚合统计信息
     *
     * @return 每个分类的标签总数和使用情况统计
     */
    JsonData getCategoryAggregateStatistics();

    /**
     * 根据使用次数范围查询标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @param category      标签分类（可选）
     * @return 标签列表
     */
    JsonData findByUsageCountRange(Long minUsageCount, Long maxUsageCount, TagCategoryEnum category);

    /**
     * 查询热门标签
     *
     * @param limit    限制数量
     * @param category 标签分类（可选）
     * @return 热门标签列表
     */
    JsonData findPopularTags(int limit, TagCategoryEnum category);

    /**
     * 增加标签使用次数
     *
     * @param tagId 标签ID
     * @return 操作是否成功
     */
    boolean incrementUsageCount(Long tagId);

    /**
     * 减少标签使用次数
     *
     * @param tagId 标签ID
     * @return 操作是否成功
     */
    boolean decrementUsageCount(Long tagId);

    /**
     * 批量增加标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param increment 增加的次数
     * @return 受影响的记录数
     */
    boolean batchIncrementUsageCount(List<Long> tagIds, int increment);

    /**
     * 批量减少标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param decrement 减少的次数
     * @return 受影响的记录数
     */
    boolean batchDecrementUsageCount(List<Long> tagIds, int decrement);

    /**
     * 批量更新标签状态
     *
     * @param tagIds 标签ID列表
     * @param status 新的状态值
     * @return 受影响的记录数
     */
    boolean batchUpdateStatus(List<Long> tagIds, Integer status);

    /**
     * 批量更新标签使用次数
     *
     * @param tagIds 标签ID列表
     * @param value  更新值（正数为增加，负数为减少）
     * @param type   操作类型（"increment"或"decrement"）
     * @return 受影响的记录数
     */
    boolean batchUpdateUsageCount(List<Long> tagIds, int value, String type);

    /**
     * 根据问题ID获取标签列表
     *
     * @param problemId 问题ID
     * @return 标签列表
     */
    List<ProblemTagVO> getTagListByProblemId(Long problemId);

    /**
     * 根据问题ID列表获取标签列表
     *
     * @param problemIds 问题ID列表
     * @return 问题ID到标签列表的映射
     */
    Map<Long, List<ProblemTagVO>> getTagListByProblemIds(List<Long> problemIds);

}
