package fun.timu.oj.judge.service.impl.Problem;

import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.service.Problem.ProblemReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 题目报告服务实现类
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemReportServiceImpl implements ProblemReportService {

    private final ProblemManager problemManager;

    /**
     * 获取月度报告
     *
     * @param year  年份
     * @param month 月份
     * @return 月度报告数据
     */
    @Override
    public Map<String, Object> getMonthlyReport(int year, int month) {
        try {
            log.info("ProblemReportService--->获取月度报告开始, 年份: {}, 月份: {}", year, month);

            Map<String, Object> result = problemManager.getMonthlyReport(year, month);

            log.info("ProblemReportService--->获取月度报告成功, 年份: {}, 月份: {}", year, month);
            return result;
        } catch (Exception e) {
            log.error("ProblemReportService--->获取月度报告失败, 年份: {}, 月份: {}, 错误: {}", year, month, e.getMessage(), e);
            throw new RuntimeException("获取月度报告失败", e);
        }
    }

    /**
     * 获取年度报告
     *
     * @param year 年份
     * @return 年度报告数据
     */
    @Override
    public Map<String, Object> getAnnualReport(int year) {
        try {
            log.info("ProblemReportService--->获取年度报告开始, 年份: {}", year);

            Map<String, Object> result = problemManager.getAnnualReport(year);

            log.info("ProblemReportService--->获取年度报告成功, 年份: {}", year);
            return result;
        } catch (Exception e) {
            log.error("ProblemReportService--->获取年度报告失败, 年份: {}, 错误: {}", year, e.getMessage(), e);
            throw new RuntimeException("获取年度报告失败", e);
        }
    }

    /**
     * 获取自定义报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param metrics   指标列表
     * @return 自定义报表数据
     */
    @Override
    public Map<String, Object> getCustomReport(Date startDate, Date endDate, List<String> metrics) {
        try {
            log.info("ProblemReportService--->获取自定义报表, 开始日期: {}, 结束日期: {}, 指标: {}", startDate, endDate, metrics);
            Map<String, Object> result = problemManager.getCustomRangeReport(startDate, endDate, metrics);
            log.info("ProblemReportService--->获取自定义报表成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemReportService--->获取自定义报表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取自定义报表失败", e);
        }
    }
}