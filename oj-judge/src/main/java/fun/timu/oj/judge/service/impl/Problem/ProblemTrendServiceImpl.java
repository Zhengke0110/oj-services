package fun.timu.oj.judge.service.impl.Problem;

import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.service.Problem.ProblemTrendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemTrendServiceImpl implements ProblemTrendService {
    private final ProblemManager problemManager;

    /**
     * 获取题目创建趋势
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 创建趋势数据
     */
    @Override
    public List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity) {
        try {
            log.info("ProblemService--->获取题目创建趋势, 开始日期: {}, 结束日期: {}, 粒度: {}", startDate, endDate, granularity);
            List<Map<String, Object>> result = problemManager.getProblemCreationTrend(startDate, endDate, granularity);
            log.info("ProblemService--->获取题目创建趋势成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目创建趋势失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目创建趋势失败", e);
        }
    }

    /**
     * 获取提交趋势分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 提交趋势数据
     */
    @Override
    public List<Map<String, Object>> getSubmissionTrendAnalysis(Date startDate, Date endDate, String granularity) {
        try {
            log.info("ProblemService--->获取提交趋势分析, 开始日期: {}, 结束日期: {}, 粒度: {}", startDate, endDate, granularity);
            List<Map<String, Object>> result = problemManager.getSubmissionTrendAnalysis(startDate, endDate, granularity);
            log.info("ProblemService--->获取提交趋势分析成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取提交趋势分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取提交趋势分析失败", e);
        }
    }
}
