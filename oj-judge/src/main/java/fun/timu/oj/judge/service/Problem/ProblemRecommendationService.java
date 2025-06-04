package fun.timu.oj.judge.service.Problem;

import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;

import java.util.List;
import java.util.Map;

public interface ProblemRecommendationService {
    List<ProblemVO> selectHotProblems(String problemType, Integer difficulty, Integer limit);

    List<ProblemVO> getRecommendedProblems(RecommendationCriteria criteria);

    List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria);

    List<ProblemVO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit);
}