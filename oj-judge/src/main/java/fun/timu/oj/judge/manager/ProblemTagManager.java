package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.judge.model.DO.ProblemTagDO;

import java.util.List;

public interface ProblemTagManager {
    /**
     * 根据id查询标签
     *
     * @param id 标签id
     * @return 标签
     */
    public ProblemTagDO findById(Long id);

    /**
     * 查询所有启用的标签
     *
     * @return
     */
    public List<ProblemTagDO> findAllActive();

    /**
     * 根据标签类别查询标签
     *
     * @param category 标签类别
     * @return
     */
    public List<ProblemTagDO> findByCategory(String category);

    /**
     * 根据标签名查询标签
     *
     * @param tagName 标签名
     * @return
     */
    public ProblemTagDO findByTagName(String tagName);

    /**
     * 搜索标签
     *
     * @param keyword 关键字
     * @return
     */
    public List<ProblemTagDO> searchTags(String keyword);

    /**
     * 保存标签
     *
     * @param problemTagDO 标签对象
     * @return 保存结果
     */
    public int save(ProblemTagDO problemTagDO);

    /**
     * 更新标签
     *
     * @param problemTagDO 标签对象
     * @return 更新结果
     */
    public int updateById(ProblemTagDO problemTagDO);

    /**
     * 删除标签
     *
     * @param id 标签id
     * @return 删除结果
     */
    public int deleteById(Long id);

    /**
     * 分页查询标签列表
     *
     * @param page     页码
     * @param size     每页大小
     * @param keyword  关键词搜索（可选）
     * @param category 标签分类（可选）
     * @param status   状态筛选（可选）
     * @return 标签列表
     */
    public List<ProblemTagDO> findTagList(int page, int size, String keyword, String category, Integer status);

    /**
     * 增加标签使用次数
     *
     * @param tagId 标签id
     * @return 增加结果
     */
    public int incrementUsageCount(Long tagId);

    /**
     * 减少标签使用次数
     *
     * @param tagId 标签id
     * @return 减少结果
     */
    public int decrementUsageCount(Long tagId);

    /**
     * 批量增加标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param increment 增加的数量
     * @return 受影响的行数
     */
    public int batchIncrementUsageCount(List<Long> tagIds, int increment);

    /**
     * 批量减少标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param decrement 减少的数量
     * @return 受影响的行数
     */
    public int batchDecrementUsageCount(List<Long> tagIds, int decrement);

    /**
     * 根据使用次数范围查询标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @return 标签列表
     */
    public List<ProblemTagDO> findByUsageCountRange(Long minUsageCount, Long maxUsageCount);

    /**
     * 获取标签使用统计信息
     *
     * @param category 标签分类（可选）
     * @return 统计信息
     */
    public List<ProblemTagDO> getTagUsageStatistics(String category);

    /**
     * 批量更新标签状态
     *
     * @param tagIds 标签ID列表
     * @param status 新状态
     * @return 受影响的行数
     */
    public int batchUpdateStatus(List<Long> tagIds, Integer status);

    /**
     * 查询热门标签
     *
     * @param limit    限制数量
     * @param category 标签分类（可选）
     * @return 热门标签列表
     */
    public List<ProblemTagDO> findPopularTags(int limit, String category);

    /**
     * 根据颜色查询标签
     *
     * @param tagColor 标签颜色
     * @return 标签列表
     */
    public List<ProblemTagDO> findByTagColor(String tagColor);

    /**
     * 检查标签名是否存在（排除指定ID）
     *
     * @param tagName   标签名
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    public boolean existsByTagNameExcludeId(String tagName, Long excludeId);

    /**
     * 分页查询问题标签列表
     *
     * @param page     页码，从1开始
     * @param size     每页大小
     * @param keyword  搜索关键词，可为空，用于模糊搜索标签名或英文名
     * @param category 标签分类，可为空
     * @param status   标签状态，可为空
     * @return 返回分页结果
     */
    public IPage<ProblemTagDO> findTagListWithPage(int page, int size, String keyword, String category, Integer status);

}
