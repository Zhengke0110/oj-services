package fun.timu.oj.judge.service.Problem;

import fun.timu.oj.judge.model.Enums.RankingType;

import java.util.List;
import java.util.Map;

public interface ProblemRankingService {
    List<Map<String, Object>> getPopularProblemsRanking(Integer limit, Integer timeRange);

    List<Map<String, Object>> getHardestProblemsRanking(Integer limit);

    List<Map<String, Object>> getEasiestProblemsRanking(Integer limit);

    List<Map<String, Object>> getMostSubmittedProblemsRanking(Integer limit, Integer timeRange);

    List<Map<String, Object>> getZeroSubmittedProblemsRanking(Integer limit, Integer timeRange);

    List<Map<String, Object>> getRecentPopularProblemsRanking(Integer limit, Integer days);

    List<Map<String, Object>> getProblemRanking(RankingType type, Integer limit);

    List<Map<String, Object>> getProblemRanking(String rankingType, Map<String, Object> criteria);
}