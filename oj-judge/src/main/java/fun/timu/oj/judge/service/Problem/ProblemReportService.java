package fun.timu.oj.judge.service.Problem;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ProblemReportService {
    Map<String, Object> getMonthlyReport(int year, int month);

    Map<String, Object> getAnnualReport(int year);

    Map<String, Object> getCustomReport(Date startDate, Date endDate, List<String> metrics);
}