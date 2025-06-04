package fun.timu.oj.judge.service.impl.Problem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.enmus.ProblemStatusEnum;
import fun.timu.oj.common.enmus.ProblemVisibilityEnum;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import fun.timu.oj.judge.service.Problem.ProblemRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemRecommendationServiceImpl implements ProblemRecommendationService {

    private final ProblemManager problemManager;

    /**
     * 获取热门题目列表
     *
     * @param problemType 题目类型
     * @param difficulty  题目难度
     * @param limit       返回数量限制，默认为10
     * @return 热门题目列表
     */
    @Override
    public List<ProblemVO> selectHotProblems(String problemType, Integer difficulty, Integer limit) {
        try {
            // TODO 多表联查优化：调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取题目标签信息
            // TODO 在ProblemManager.selectHotProblems()方法中通过LEFT JOIN problem_tag_relation和problem_tag表
            // TODO 一次性获取热门题目及其关联标签，避免后续单独查询每个题目的标签信息
            // 调用manager层获取热门题目数据
            List<ProblemDO> problemDOList = problemManager.selectHotProblems(problemType, difficulty, limit);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemDOList.stream().map(ProblemUtils::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("ProblemRecommendationService--->获取热门题目列表成功，类型: {}, 难度: {}, 数量: {}", problemType, difficulty, problemVOList.size());
            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemRecommendationService--->获取热门题目列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    /**
     * 获取推荐题目（统一接口）
     * 支持多种推荐算法：通过率、相似性、热度、算法数据
     *
     * @param criteria 推荐条件，包含推荐类型和相关参数
     * @return 推荐题目列表
     * @since 2.0
     */
    @Override
    public List<ProblemVO> getRecommendedProblems(RecommendationCriteria criteria) {
        try {
            log.info("ProblemRecommendationService--->获取推荐题目开始, 推荐类型:{}, 条件:{}", criteria.getType(), criteria);

            // TODO 多表联查优化：在ProblemManager.getRecommendedProblems()方法中集成标签查询
            // TODO 通过JOIN problem_tag_relation和problem_tag表，一次性获取推荐题目及其标签信息
            // TODO 调用ProblemTagRelationManager.findByProblemIds()批量获取标签关联，避免N+1查询问题
            List<ProblemDO> problemDOList = problemManager.getRecommendedProblems(criteria);
            List<ProblemVO> result = problemDOList.stream().map(ProblemUtils::convertToVO).collect(Collectors.toList());

            log.info("ProblemRecommendationService--->获取推荐题目成功, 推荐类型:{}, 返回数量:{}", criteria.getType(), result.size());
            return result;
        } catch (Exception e) {
            log.error("ProblemRecommendationService--->获取推荐题目失败, 推荐类型:{}, 错误: {}", criteria.getType(), e.getMessage(), e);
            throw new RuntimeException("获取推荐题目失败", e);
        }
    }

    /**
     * 获取带评分的推荐题目（统一接口）
     * 支持多种推荐算法，返回题目及其推荐评分
     *
     * @param criteria 推荐条件，包含推荐类型和相关参数
     * @return 带评分的推荐题目列表
     * @since 2.0
     */
    @Override
    public List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria) {
        try {
            log.info("ProblemRecommendationService--->获取带评分推荐题目开始, 推荐类型:{}, 条件:{}", criteria.getType(), criteria);

            // TODO 多表联查优化：在ProblemManager.getRecommendedProblemsWithScore()方法中优化数据获取
            // TODO 通过JOIN problem_tag_relation和problem_tag表，将标签信息直接包含在返回的Map中
            // TODO 调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取标签名称，减少后续查询
            List<Map<String, Object>> recommendedProblemsWithScore = problemManager.getRecommendedProblemsWithScore(criteria);

            log.info("ProblemRecommendationService--->获取带评分推荐题目成功, 推荐类型:{}, 返回数量:{}", criteria.getType(), recommendedProblemsWithScore.size());
            return recommendedProblemsWithScore;
        } catch (Exception e) {
            log.error("ProblemRecommendationService--->获取带评分推荐题目失败, 推荐类型:{}, 错误: {}", criteria.getType(), e.getMessage(), e);
            throw new RuntimeException("获取带评分推荐题目失败", e);
        }
    }

    /**
     * 查询相似题目（基于标签和难度）
     *
     * @param problemId   题目ID
     * @param difficulty  难度限制
     * @param problemType 题目类型限制
     * @param limit       返回数量限制
     * @return 相似题目列表
     */
    @Override
    public List<ProblemVO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit) {
        try {
            log.info("ProblemService--->查询相似题目, 题目ID: {}, 难度: {}, 题目类型: {}, 限制数量: {}", problemId, difficulty, problemType, limit);

            // 参数校验
            if (problemId == null || problemId <= 0) {
                throw new RuntimeException("题目ID无效");
            }

            // TODO 多表联查优化：优化ProblemManager.findSimilarProblems()方法的实现
            // TODO 通过ProblemTagRelationManager.findSimilarProblems()方法基于共同标签查找相似题目
            // TODO 在查询相似题目时直接JOIN problem_tag_relation表，根据共同标签数量计算相似度
            // TODO 调用ProblemTagRelationManager.findRelatedTags()获取相关标签，提升推荐精度
            // 调用manager层查询相似题目
            List<ProblemDO> problemDOList = problemManager.findSimilarProblems(problemId, difficulty, problemType, limit);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemDOList.stream().map(ProblemUtils::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("查询相似题目成功, 题目ID: {}, 获取到 {} 个相似题目", problemId, problemVOList.size());
            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemService--->查询相似题目失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }


}