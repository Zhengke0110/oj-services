package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.model.DTO.TagUsageStatisticsDTO;
import fun.timu.oj.judge.model.DTO.CategoryAggregateStatisticsDTO;

import java.util.List;

public interface ProblemTagManager {
    /**
     * 根据id查询标签
     *
     * @param id 标签id
     * @return 标签
     */
    ProblemTagDO findById(Long id);

    /**
     * 查询所有启用的标签
     *
     * @return
     */
    List<ProblemTagDO> findAllActive();

    /**
     * 保存标签
     *
     * @param problemTagDO 标签对象
     * @return 保存结果
     */
    int save(ProblemTagDO problemTagDO);

    /**
     * 更新标签
     *
     * @param problemTagDO 标签对象
     * @return 更新结果
     */
    int updateById(ProblemTagDO problemTagDO);

    /**
     * 删除标签
     *
     * @param id 标签id
     * @return 删除结果
     */
    int deleteById(Long id);

    /**
     * 分页查询问题标签列表
     *
     * @param page     页码，从1开始
     * @param size     每页大小
     * @param keyword  搜索关键词，可为空，用于模糊搜索标签名或英文名
     * @param category 标签分类，可为空
     * @param status   标签状态，可为空
     * @param tagColor 标签颜色，可为空
     * @return 返回分页结果
     */
    IPage<ProblemTagDO> findTagListWithPage(int page, int size, String keyword, String category, Integer status, String tagColor);

    /**
     * 获取标签使用统计信息
     *
     * @param category 标签分类（可选）
     * @return 统计信息
     */
    List<TagUsageStatisticsDTO> getTagUsageStatistics(String category);

    /**
     * 获取所有分类的标签使用聚合统计信息
     *
     * @return 每个分类的标签总数和使用情况统计
     */
    List<CategoryAggregateStatisticsDTO> getCategoryAggregateStatistics();

    /**
     * 根据使用次数范围查询标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @param category      标签分类（可选）
     * @return 标签列表
     */
    List<ProblemTagDO> findByUsageCountRange(Long minUsageCount, Long maxUsageCount, String category);

    /**
     * 查询热门标签
     *
     * @param limit    限制数量
     * @param category 标签分类（可选）
     * @return 热门标签列表
     */
    List<ProblemTagDO> findPopularTags(int limit, String category);

    /**
     * 增加标签使用次数
     *
     * @param tagId 标签id
     * @return 增加结果
     */
    int incrementUsageCount(Long tagId);

    /**
     * 减少标签使用次数
     *
     * @param tagId 标签id
     * @return 减少结果
     */
    int decrementUsageCount(Long tagId);

    /**
     * 批量增加标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param increment 增加的数量
     * @return 受影响的行数
     */
    int batchIncrementUsageCount(List<Long> tagIds, int increment);

    /**
     * 批量减少标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param decrement 减少的数量
     * @return 受影响的行数
     */
    int batchDecrementUsageCount(List<Long> tagIds, int decrement);


    /**
     * 批量更新标签状态
     *
     * @param tagIds 标签ID列表
     * @param status 新状态
     * @return 受影响的行数
     */
    int batchUpdateStatus(List<Long> tagIds, Integer status);

    /**
     * 根据ID列表批量查询标签
     *
     * @param tagIds 标签ID列表
     * @return 标签列表
     */
    List<ProblemTagDO> batchFindByIds(List<Long> tagIds);
}
