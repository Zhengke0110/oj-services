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
            
            // TODO 多表联查优化：在ProblemStatisticsManager中优化getSubmissionTrendAnalysis()方法
            // TODO 通过全面的多表联查分析提交趋势的深层规律：
            // TODO 1. JOIN problem_tag_relation 和 problem_tag 表分析不同算法标签的提交趋势差异
            // TODO 2. JOIN user 表分析用户群体的提交行为模式变化和用户留存趋势
            // TODO 3. 分析提交趋势与题目难度、类型、标签的关联性
            // TODO 4. 调用ProblemTagRelationManager.getPopularTags()分析热门标签在提交趋势中的影响
            // TODO 5. 调用ProblemTagRelationManager.getRecentActiveRelations()分析最近活跃的标签关联对提交的促进作用
            // TODO 6. 为平台运营提供用户行为洞察和题目优化建议
            List<Map<String, Object>> result = problemManager.getSubmissionTrendAnalysis(startDate, endDate, granularity);
            
            log.info("ProblemService--->获取提交趋势分析成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取提交趋势分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取提交趋势分析失败", e);
        }
    }
}
