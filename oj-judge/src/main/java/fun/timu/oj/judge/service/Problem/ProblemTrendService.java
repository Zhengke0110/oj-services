package fun.timu.oj.judge.service.Problem;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ProblemTrendService {
    List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity);
}