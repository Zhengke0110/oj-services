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
            
            // TODO 多表联查优化：在ProblemStatisticsManager中优化getProblemCreationTrend()方法
            // TODO 通过复杂的多表联查分析题目创建趋势：
            // TODO 1. JOIN problem_tag_relation 和 problem_tag 表分析创建趋势中的标签分布变化
            // TODO 2. JOIN user 表分析创建者活跃度趋势和新用户贡献率
            // TODO 3. JOIN submission 表分析新创建题目的初期反响和用户参与度
            // TODO 4. 调用ProblemTagRelationManager.findByCreateTimeRange()获取时间范围内的标签关联趋势
            // TODO 5. 为管理员提供创建质量分析，包括新题目的标签覆盖率、难度分布等
            List<Map<String, Object>> result = problemManager.getProblemCreationTrend(startDate, endDate, granularity);
            
            log.info("ProblemService--->获取题目创建趋势成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目创建趋势失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目创建趋势失败", e);
        }
    }
}
