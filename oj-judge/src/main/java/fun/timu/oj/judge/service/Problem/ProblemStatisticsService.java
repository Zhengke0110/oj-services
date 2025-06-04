package fun.timu.oj.judge.service.Problem;

import fun.timu.oj.judge.model.VTO.PopularProblemCategoryVTO;
import fun.timu.oj.judge.model.VTO.ProblemDetailStatisticsVTO;
import fun.timu.oj.judge.model.VTO.ProblemStatisticsVTO;

import java.util.List;
import java.util.Map;

public interface ProblemStatisticsService {
    List<ProblemStatisticsVTO> getProblemStatistics();

    Long countByCreator(Long creatorId);

    Double getAcceptanceRate(Long problemId);

    ProblemDetailStatisticsVTO getProblemDetailStatistics();

    List<PopularProblemCategoryVTO> getPopularProblemCategories(Integer limit);

    // 分布统计
    List<Map<String, Object>> getStatisticsByDifficulty();

    List<Map<String, Object>> getStatisticsByType();

    List<Map<String, Object>> getStatisticsByLanguage();

    List<Map<String, Object>> getStatisticsByStatus();
}