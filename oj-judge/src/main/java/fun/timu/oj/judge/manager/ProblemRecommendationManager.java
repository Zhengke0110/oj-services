package fun.timu.oj.judge.manager;

import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;

import java.util.List;
import java.util.Map;

/**
 * 题目推荐管理器接口
 * 负责处理所有题目推荐相关的业务逻辑
 *
 * @author zhengke
 */
public interface ProblemRecommendationManager {

    /**
     * 选择热门题目
     *
     * @param problemType 题目类型，用于过滤特定类型的题目
     * @param difficulty  题目难度，用于过滤特定难度的题目
     * @param limit       返回的题目数量限制如果为null或小于等于0，则使用默认值10
     * @return 包含热门题目的列表
     */
    List<ProblemDO> selectHotProblems(String problemType, Integer difficulty, Integer limit);

    /**
     * 统一的推荐题目接口
     * 支持多种推荐算法：通过率推荐、相似性推荐、热门推荐、算法数据推荐
     *
     * @param criteria 推荐条件
     * @return 推荐题目列表
     */
    List<ProblemDO> getRecommendedProblems(RecommendationCriteria criteria);

    /**
     * 统一的推荐算法数据接口
     * 返回包含推荐评分等详细信息的数据
     *
     * @param criteria 推荐条件
     * @return 推荐数据列表（包含评分信息）
     */
    List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria);

    /**
     * 统一推荐方法
     * 支持根据难度、题目类型、标签、创建者等多种条件进行灵活组合查询
     *
     * @param criteria 推荐标准，包含多种筛选条件
     * @return 符合条件的题目列表
     */
    List<ProblemDO> recommendProblems(RecommendationCriteria criteria);
}
