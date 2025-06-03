package fun.timu.oj.judge.manager.impl.Problem;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.Problem.ProblemRecommendationManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目推荐管理器实现类
 * 负责处理所有题目推荐相关的业务逻辑
 *
 * @author zhengke
 */
@Component
@RequiredArgsConstructor
public class ProblemRecommendationManagerImpl implements ProblemRecommendationManager {

    private final ProblemMapper problemMapper;

    /**
     * 选择热门题目
     *
     * @param problemType 题目类型，用于过滤特定类型的题目
     * @param difficulty  题目难度，用于过滤特定难度的题目
     * @param limit       返回的题目数量限制如果为null或小于等于0，则使用默认值10
     * @return 包含热门题目的列表
     */
    @Override
    public List<ProblemDO> selectHotProblems(String problemType, Integer difficulty, Integer limit) {
        // 如果limit为null或小于等于0，设置默认值为10
        if (limit == null || limit <= 0) limit = 10;

        // 创建查询条件
        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 未删除的题目
        queryWrapper.eq(ProblemDO::getIsDeleted, 0);

        // 状态为激活的题目
        queryWrapper.eq(ProblemDO::getStatus, 1);

        // 提交次数大于0的题目
        queryWrapper.gt(ProblemDO::getSubmissionCount, 0);

        // 可选的题目类型筛选
        if (problemType != null && !problemType.isEmpty()) {
            queryWrapper.eq(ProblemDO::getProblemType, problemType);
        }

        // 可选的难度筛选
        if (difficulty != null) {
            queryWrapper.eq(ProblemDO::getDifficulty, difficulty);
        }

        // 按提交次数降序排序，如果提交次数相同则按通过次数降序排序
        queryWrapper.orderByDesc(ProblemDO::getSubmissionCount, ProblemDO::getAcceptedCount);

        // 限制返回的结果数量
        Page<ProblemDO> page = new Page<>(1, limit);

        // 执行查询并返回结果
        IPage<ProblemDO> pageResult = problemMapper.selectPage(page, queryWrapper);
        List<ProblemDO> result = pageResult.getRecords();
        return result;
    }


    /**
     * 统一的推荐题目接口
     * 支持多种推荐算法：通过率推荐、相似性推荐、热门推荐、算法数据推荐
     *
     * @param criteria 推荐条件
     * @return 推荐题目列表
     */
    @Override
    public List<ProblemDO> getRecommendedProblems(RecommendationCriteria criteria) {
        // 参数校验
        if (criteria == null) {
            throw new IllegalArgumentException("推荐条件不能为空");
        }
        // 调用Mapper层统一推荐接口
        List<ProblemDO> result = problemMapper.getRecommendedProblems(criteria);
        return result;
    }

    /**
     * 统一的推荐算法数据接口
     * 返回包含推荐评分等详细信息的数据
     *
     * @param criteria 推荐条件
     * @return 推荐数据列表（包含评分信息）
     */
    @Override
    public List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria) {

        // 参数校验
        if (criteria == null) {
            throw new IllegalArgumentException("推荐条件不能为空");
        }

        // 调用Mapper层统一推荐接口
        List<HashMap<String, Object>> result = problemMapper.getRecommendedProblemsWithScore(criteria);

        // 转换为标准Map接口
        List<Map<String, Object>> finalResult = result.stream().map(map -> (Map<String, Object>) map).collect(Collectors.toList());
        return finalResult;
    }

    /**
     * 统一推荐方法
     * 支持根据难度、题目类型、标签、创建者等多种条件进行灵活组合查询
     *
     * @param criteria 推荐标准，包含多种筛选条件
     * @return 符合条件的题目列表
     */
    @Override
    public List<ProblemDO> recommendProblems(RecommendationCriteria criteria) {

        LambdaQueryWrapper<ProblemDO> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询未删除的题目
        queryWrapper.eq(ProblemDO::getIsDeleted, false);

        // 按照创建时间降序排序
        queryWrapper.orderByDesc(ProblemDO::getCreatedAt);

        // 根据难度筛选
        if (criteria.getDifficulty() != null) {
            queryWrapper.eq(ProblemDO::getDifficulty, criteria.getDifficulty());
        }

        // 根据题目类型筛选
        if (criteria.getProblemType() != null && !criteria.getProblemType().isEmpty()) {
            queryWrapper.eq(ProblemDO::getProblemType, criteria.getProblemType());
        }

        // 根据标签筛选（假设标签存储在JSON字段中）
        if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
            StringBuilder tagCondition = new StringBuilder("(");
            for (int i = 0; i < criteria.getTags().size(); i++) {
                if (i > 0) tagCondition.append(" AND ");
                tagCondition.append("JSON_CONTAINS(tags, JSON_QUOTE('").append(criteria.getTags().get(i)).append("'))");
            }
            tagCondition.append(")");
            queryWrapper.apply(tagCondition.toString());
        }

        // 根据创建者ID筛选
        if (criteria.getCreatorId() != null) {
            queryWrapper.eq(ProblemDO::getCreatorId, criteria.getCreatorId());
        }

        // 根据可见性筛选
        if (criteria.getVisibility() != null) {
            queryWrapper.eq(ProblemDO::getVisibility, criteria.getVisibility());
        }

        // 根据状态筛选
        if (criteria.getStatus() != null) {
            queryWrapper.eq(ProblemDO::getStatus, criteria.getStatus());
        }

        return problemMapper.selectList(queryWrapper);
    }
}
