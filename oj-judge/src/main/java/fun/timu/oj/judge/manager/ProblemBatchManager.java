package fun.timu.oj.judge.manager;

import java.util.List;

/**
 * 题目批量操作管理器接口
 * 负责题目的批量操作和维护功能
 *
 * @author zhengke
 */
public interface ProblemBatchManager {

    /**
     * 批量更新题目状态
     *
     * @param problemIds 题目ID列表
     * @param status     要更新的状态值
     * @return 更新的记录数
     */
    int batchUpdateStatus(List<Long> problemIds, Integer status);

    /**
     * 批量软删除题目
     *
     * @param problemIds 题目ID列表
     * @return 删除的记录数
     */
    int batchSoftDelete(List<Long> problemIds);

    /**
     * 批量恢复已删除的题目
     *
     * @param problemIds 题目ID列表
     * @return 恢复的记录数
     */
    int batchRestore(List<Long> problemIds);

    /**
     * 批量更新题目可见性
     *
     * @param problemIds 题目ID列表
     * @param visibility 可见性值
     * @return 更新的记录数
     */
    int batchUpdateVisibility(List<Long> problemIds, Integer visibility);

    /**
     * 批量更新题目的时间和内存限制
     *
     * @param problemIds  题目ID列表
     * @param timeLimit   时间限制（秒）
     * @param memoryLimit 内存限制（MB）
     * @return 更新的记录数
     */
    int batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit);

    /**
     * 重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 题目ID列表
     * @return 重置的记录数
     */
    int batchResetStats(List<Long> problemIds);

}
