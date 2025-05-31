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
    public IPage<ProblemTagDO> findTagListWithPage(int page, int size, String keyword, String category, Integer status, String tagColor);
}
