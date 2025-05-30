package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.model.DO.ProblemDO;

import java.util.List;

public interface ProblemManager {

    /**
     * 根据id查询题目
     *
     * @param id
     * @return
     */
    public ProblemDO findById(Long id);

    /**
     * 分页查询题目列表
     *
     * @param current     当前页
     * @param size        每页大小
     * @param problemType 题目类型
     * @param difficulty  难度
     * @param status      状态
     * @return 分页结果
     */
    public Page<ProblemDO> findPageList(int current, int size, String problemType, Integer difficulty, Integer status);

    /**
     * 根据创建者id查询题目列表
     *
     * @param creatorId 创建者id
     * @return 题目列表
     */
    public List<ProblemDO> findByCreatorId(Long creatorId);

    /**
     * 根据题目标题搜索题目
     *
     * @param keyword 关键字
     * @return 搜索结果
     */
    public List<ProblemDO> searchByTitle(String keyword);

    /**
     * 保存题目
     *
     * @param problemDO
     * @return
     */
    public int save(ProblemDO problemDO);

    /**
     * 更新题目
     *
     * @param problemDO
     * @return
     */
    public int updateById(ProblemDO problemDO);

    /**
     * 删除题目
     *
     * @param id
     * @return
     */
    public int deleteById(Long id);

    /**
     * 更新题目提交统计
     *
     * @param problemId  题目ID
     * @param isAccepted 是否通过
     * @return 更新的记录数
     */
    public int updateSubmissionStats(Long problemId, boolean isAccepted);
}
