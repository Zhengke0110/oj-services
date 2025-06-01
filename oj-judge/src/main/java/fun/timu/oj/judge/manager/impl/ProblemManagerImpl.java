package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemManagerImpl implements ProblemManager {
    private final ProblemMapper problemMapper;

    /**
     * 根据id查询问题
     *
     * @param id 问题id
     * @return 问题
     */
    @Override
    public ProblemDO getById(Long id) {
        return problemMapper.selectById(id);
    }


    /**
     * 根据多个筛选条件分页查询问题列表
     *
     * @param current            当前页码
     * @param size               每页大小
     * @param problemType        问题类型
     * @param difficulty         难度
     * @param status             状态
     * @param supportedLanguages 支持的编程语言列表
     * @param hasInput           是否有输入
     * @param MinAcceptanceRate  最小通过率
     * @param MaxAcceptanceRate  最大通过率
     * @return 分页的问题列表
     */
    @Override
    public IPage<ProblemDO> findTagListWithPage(int current, int size, String problemType, Integer difficulty, Integer status, List<String> supportedLanguages, Boolean hasInput, Double MinAcceptanceRate, Double MaxAcceptanceRate) {
        // 创建分页对象
        Page<ProblemDO> page = new Page<>(current, size);

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 未删除的记录
        queryWrapper.eq(ProblemDO::getIsDeleted, false);

        // 按题目类型筛选
        if (problemType != null && !problemType.isEmpty()) {
            queryWrapper.eq(ProblemDO::getProblemType, problemType);
        }

        // 按难度筛选
        if (difficulty != null) {
            queryWrapper.eq(ProblemDO::getDifficulty, difficulty);
        }

        // 按状态筛选
        if (status != null) {
            queryWrapper.eq(ProblemDO::getStatus, status);
        }

        // 按是否需要输入筛选
        if (hasInput != null) {
            queryWrapper.eq(ProblemDO::getHasInput, hasInput);
        }

        // 通过率筛选 (通过率 = accepted_count / submission_count)
        if (MinAcceptanceRate != null || MaxAcceptanceRate != null) {
            // 判断是否需要真正的通过率筛选
            boolean needAcceptanceRateFilter = false;

            if (MinAcceptanceRate != null && MinAcceptanceRate > 0.0) {
                needAcceptanceRateFilter = true;
            }

            if (MaxAcceptanceRate != null && MaxAcceptanceRate < 1.0) {
                needAcceptanceRateFilter = true;
            }

            // 只有在真正需要通过率筛选时才添加submission_count > 0的条件
            if (needAcceptanceRateFilter) {
                // 防止除以零，确保至少有一次提交
                queryWrapper.gt(ProblemDO::getSubmissionCount, 0);

                if (MinAcceptanceRate != null && MinAcceptanceRate > 0.0) {
                    // acceptedCount >= submissionCount * MinAcceptanceRate
                    queryWrapper.apply("accepted_count >= submission_count * {0}", MinAcceptanceRate);
                }

                if (MaxAcceptanceRate != null && MaxAcceptanceRate < 1.0) {
                    // acceptedCount <= submissionCount * MaxAcceptanceRate
                    queryWrapper.apply("accepted_count <= submission_count * {0}", MaxAcceptanceRate);
                }
            }
        }

        // 支持的编程语言筛选 (因为是JSON字段，MyBatis-Plus不能直接处理)
        // 需要使用JSON查询，这里使用原生SQL
        if (supportedLanguages != null && !supportedLanguages.isEmpty()) {
            // 构造JSON查询条件，始终用括号包围
            StringBuilder jsonCondition = new StringBuilder("(");
            for (int i = 0; i < supportedLanguages.size(); i++) {
                if (i > 0) jsonCondition.append(" AND ");
                // 使用 JSON_SEARCH 函数查找数组中的字符串元素
                jsonCondition.append("JSON_SEARCH(supported_languages, 'one', '").append(supportedLanguages.get(i)).append("') IS NOT NULL");
            }
            jsonCondition.append(")");
            queryWrapper.apply(jsonCondition.toString());
        }

        // 执行查询
        return problemMapper.selectPage(page, queryWrapper);
    }

}
