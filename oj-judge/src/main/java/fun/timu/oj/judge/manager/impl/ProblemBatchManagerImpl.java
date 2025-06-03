package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import fun.timu.oj.judge.manager.ProblemBatchManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目批量操作管理实现
 * 负责处理题目的批量更新、删除、恢复等操作
 *
 * @author zhengke
 * @since 2025-06-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemBatchManagerImpl implements ProblemBatchManager {

    private final ProblemMapper problemMapper;

    /**
     * 批量更新题目状态
     *
     * @param problemIds 题目ID列表
     * @param status     要更新的状态值
     * @return 更新的记录数
     */
    @Override
    public int batchUpdateStatus(List<Long> problemIds, Integer status) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty() || status == null) {
            throw new RuntimeException("批量更新题目状态失败:参数无效，题目ID列表为空或状态值为null");
        }

        // 创建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 只更新未删除的题目
        updateWrapper.eq(ProblemDO::getIsDeleted, 0);
        // 限定要更新的ID列表
        updateWrapper.in(ProblemDO::getId, problemIds);
        // 设置要更新的字段
        updateWrapper.set(ProblemDO::getStatus, status);

        // 执行批量更新并返回影响的行数
        return problemMapper.update(null, updateWrapper);
    }

    /**
     * 批量删除题目
     *
     * @param problemIds 待删除的题目ID列表
     * @return 删除的记录数
     */
    @Override
    public int batchSoftDelete(List<Long> problemIds) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty()) {
            return 0;
        }

        // 创建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 只更新未删除的题目
        updateWrapper.eq(ProblemDO::getIsDeleted, 0);
        // 限定要更新的ID列表
        updateWrapper.in(ProblemDO::getId, problemIds);
        // 设置要更新的字段
        updateWrapper.set(ProblemDO::getIsDeleted, 1);

        // 执行批量更新并返回影响的行数
        return problemMapper.update(null, updateWrapper);
    }

    /**
     * 批量恢复已删除的题目
     *
     * @param problemIds 待恢复的题目ID列表
     * @return 恢复的记录数
     */
    @Override
    public int batchRestore(List<Long> problemIds) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty()) {
            return 0;
        }

        // 创建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 只更新已删除的题目
        updateWrapper.eq(ProblemDO::getIsDeleted, 1);
        // 限定要更新的ID列表
        updateWrapper.in(ProblemDO::getId, problemIds);
        // 设置要更新的字段
        updateWrapper.set(ProblemDO::getIsDeleted, 0);

        // 执行批量更新并返回影响的行数
        return problemMapper.update(null, updateWrapper);
    }

    /**
     * 批量更新题目可见性
     *
     * @param problemIds 待更新的题目ID列表
     * @param visibility 要更新的可见性值
     * @return 更新的记录数
     */
    @Override
    public int batchUpdateVisibility(List<Long> problemIds, Integer visibility) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty() || visibility == null) {
            throw new IllegalArgumentException("参数错误");
        }

        // 使用LambdaUpdateWrapper构建更新条件
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ProblemDO::getId, problemIds).set(ProblemDO::getVisibility, visibility);

        // 执行更新操作
        int updatedCount = problemMapper.update(null, updateWrapper);
        if (updatedCount <= 0) throw new RuntimeException("更新失败");
        return updatedCount;
    }

    /**
     * 批量更新题目限制
     *
     * @param problemIds  待更新的题目ID列表
     * @param timeLimit   要更新的时间限制
     * @param memoryLimit 要更新的内存限制
     * @return 更新的记录数
     */
    @Override
    public int batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit) {
        // 参数校验
        if (problemIds == null || problemIds.isEmpty()) {
            throw new IllegalArgumentException("参数错误");
        }

        if (timeLimit == null && memoryLimit == null) {
            throw new IllegalArgumentException("参数错误");
        }

        // 创建更新构造器
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();

        // 设置更新条件：ID在指定列表中且未删除
        updateWrapper.in(ProblemDO::getId, problemIds).eq(ProblemDO::getIsDeleted, 0);

        // 设置更新字段：如果参数不为空，则更新对应字段
        if (timeLimit != null) {
            updateWrapper.set(ProblemDO::getTimeLimit, timeLimit);
        }

        if (memoryLimit != null) {
            updateWrapper.set(ProblemDO::getMemoryLimit, memoryLimit);
        }

        // 执行更新操作
        int updatedRows = problemMapper.update(null, updateWrapper);
        if (updatedRows <= 0) throw new RuntimeException("更新失败,请检查题目ID是否存在或未被删除");
        return updatedRows;

    }

    /**
     * 重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 题目ID列表
     * @return 重置的记录数
     */
    @Override
    public int batchResetStats(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            throw new IllegalArgumentException("参数错误");
        }

        // 使用MyBatis-Plus的LambdaUpdateWrapper构建更新操作
        LambdaUpdateWrapper<ProblemDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ProblemDO::getId, problemIds).eq(ProblemDO::getIsDeleted, 0)  // 只重置未删除的题目
                .set(ProblemDO::getSubmissionCount, 0).set(ProblemDO::getAcceptedCount, 0);

        return problemMapper.update(null, updateWrapper);
    }

}
