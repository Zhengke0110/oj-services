package fun.timu.oj.judge.manager;

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
}
