package fun.timu.oj.judge.service.impl.Problem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.enmus.ProblemStatusEnum;
import fun.timu.oj.common.enmus.ProblemVisibilityEnum;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.VTO.PopularProblemCategoryVTO;
import fun.timu.oj.judge.model.VTO.ProblemDetailStatisticsVTO;
import fun.timu.oj.judge.model.VTO.ProblemStatisticsVTO;
import fun.timu.oj.judge.service.Problem.ProblemStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目统计服务实现类
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemStatisticsServiceImpl implements ProblemStatisticsService {

    private final ProblemManager problemManager;

    /**
     * 获取题目统计信息
     * 该方法返回按题目类型和难度分组的统计数据，包括总题目数量、活跃题目数量、
     * 总提交次数、总通过次数以及平均通过率等信息
     *
     * @return 统计信息列表
     */
    @Override
    public List<ProblemStatisticsVTO> getProblemStatistics() {
        try {
            log.info("ProblemStatisticsService--->获取题目统计信息");
            
            // TODO 多表联查优化：在ProblemStatisticsManager中优化getProblemStatistics()方法
            // TODO 通过复杂的多表联查获取全面的统计信息：
            // TODO 1. JOIN submission 表统计真实的提交和通过数据
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析标签分布和热门标签
            // TODO 3. JOIN user 表统计创建者活跃度和贡献度
            // TODO 4. 调用ProblemTagRelationManager.getPopularTags()获取热门标签统计
            // TODO 5. 提供更丰富的统计维度，包括标签分布、创建者排行等
            List<ProblemStatisticsDTO> problemStatistics = problemManager.getProblemStatistics();
            
            List<ProblemStatisticsVTO> problemStatisticsVTOList = problemStatistics.stream().map(problemStatisticsDTO -> {
                ProblemStatisticsVTO problemStatisticsVTO = ProblemStatisticsVTO.fromDTO(problemStatisticsDTO);
                return problemStatisticsVTO;
            }).collect(Collectors.toList());
            log.info("ProblemStatisticsService--->获取题目统计信息成功，数量: {}", problemStatisticsVTOList.size());
            return problemStatisticsVTOList;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->获取题目统计信息异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目统计信息失败", e);
        }
    }

    /**
     * 根据创建者ID统计题目数量
     *
     * @param creatorId 创建者ID
     * @return 创建者创建的题目数量
     */
    @Override
    public Long countByCreator(Long creatorId) {
        try {
            // 参数校验
            if (creatorId == null || creatorId <= 0) {
                throw new RuntimeException("创建者ID无效");
            }

            // TODO 多表联查优化：在ProblemStatisticsManager中新增getCreatorStatistics()方法
            // TODO 通过多表联查提供创建者的全面统计信息：
            // TODO 1. JOIN submission 表统计该创建者题目的总提交量和通过率
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析创建者偏好的算法标签
            // TODO 3. 统计该创建者题目的平均质量评分和受欢迎程度
            // TODO 4. 调用ProblemTagRelationManager.findByCreatorId()获取创建者的标签偏好分析
            // 调用manager层查询题目数量
            Long count = problemManager.countByCreator(creatorId);
            log.info("查询创建者题目数量成功，创建者ID: {}, 题目数量: {}", creatorId, count);
            return count;
        } catch (Exception e) {
            log.error("查询创建者题目数量失败, 创建者ID: {}, 错误: {}", creatorId, e.getMessage(), e);
            throw new RuntimeException("查询创建者题目数量失败", e);
        }
    }

    /**
     * 获取题目详细统计信息
     *
     * @return 包含各种维度统计数据的HashMap，包括题目总数、难度分布、类型分布、提交情况等
     */
    @Override
    public ProblemDetailStatisticsVTO getProblemDetailStatistics() {
        try {
            log.info("ProblemStatisticsService--->获取题目详细统计信息");
            
            // TODO 多表联查优化：在ProblemStatisticsManager中优化getProblemDetailStatistics()方法
            // TODO 通过全面的多表联查构建详细统计报告：
            // TODO 1. JOIN submission 表获取提交趋势、用户参与度、代码语言使用统计
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析标签分布、标签组合热度
            // TODO 3. JOIN user 表分析用户行为模式、地区分布、活跃时段等
            // TODO 4. 调用ProblemTagRelationManager.getTagDistributionStats()获取标签分布详情
            // TODO 5. 调用ProblemTagRelationManager.getStatisticsReport()获取关联关系健康度报告
            // TODO 6. 提供多维度交叉分析，如难度-标签分布、类型-地区偏好等
            ProblemDetailStatisticsDTO problemDetailStatistics = problemManager.getProblemDetailStatistics();
            
            ProblemDetailStatisticsVTO problemDetailStatisticsVTO = ProblemDetailStatisticsVTO.fromDTO(problemDetailStatistics);
            log.info("ProblemStatisticsService--->获取题目详细统计信息成功, 统计信息: {}", problemDetailStatisticsVTO.toString());
            return problemDetailStatisticsVTO;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->获取题目详细统计信息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    @Override
    public List<PopularProblemCategoryVTO> getPopularProblemCategories(Integer limit) {
        try {
            log.info("ProblemStatisticsService--->获取最受欢迎的题目类型和难度组合, limit: {}", limit);
            
            // TODO 多表联查优化：在ProblemStatisticsManager中优化getPopularProblemCategories()方法
            // TODO 通过多表联查获取更准确的受欢迎程度数据：
            // TODO 1. JOIN submission 表统计真实的用户参与度和提交活跃度
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析热门类型-难度组合的标签特征
            // TODO 3. JOIN user 表分析不同用户群体对类型-难度组合的偏好差异
            // TODO 4. 调用ProblemTagRelationManager.getPopularTags()结合标签热度分析
            // TODO 5. 提供时间维度的热门趋势变化分析
            List<PopularProblemCategoryDTO> popularProblemCategories = problemManager.getPopularProblemCategories(limit);
            
            List<PopularProblemCategoryVTO> popularProblemCategoriesVTO = PopularProblemCategoryVTO.fromDTOList(popularProblemCategories);
            log.info("ProblemStatisticsService--->获取最受欢迎的题目类型和难度组合成功, 获取到的组合数量: {}", popularProblemCategoriesVTO.size());
            return popularProblemCategoriesVTO;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->获取最受欢迎的题目类型和难度组合失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 按难度获取统计信息
     *
     * @return 各难度级别的题目统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByDifficulty() {
        try {
            log.info("ProblemStatisticsService--->按难度获取统计信息开始");

            // TODO 多表联查优化：在ProblemStatisticsManager中优化按难度统计的方法
            // TODO 通过多表联查提供难度维度的深入分析：
            // TODO 1. JOIN submission 表统计各难度的真实通过率和用户完成时间分布
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析各难度对应的算法标签分布
            // TODO 3. JOIN user 表分析不同经验水平用户在各难度的表现差异
            // TODO 4. 调用ProblemTagRelationManager.getTagDistributionStats()按难度分析标签偏好
            // TODO 5. 提供难度梯度的学习路径建议和题目推荐依据
            List<Map<String, Object>> result = problemManager.getStatisticsByDifficulty();

            log.info("ProblemStatisticsService--->按难度获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->按难度获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按难度获取统计信息失败", e);
        }
    }

    /**
     * 按题目类型获取统计信息
     *
     * @return 各题目类型的统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByType() {
        try {
            log.info("ProblemStatisticsService--->按题目类型获取统计信息开始");

            // TODO 多表联查优化：在ProblemStatisticsManager中优化按类型统计的方法
            // TODO 通过多表联查分析题目类型的特征和趋势：
            // TODO 1. JOIN submission 表统计各类型题目的用户偏好和完成情况
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析类型与算法标签的关联模式
            // TODO 3. JOIN user 表分析不同背景用户对题目类型的选择偏好
            // TODO 4. 调用ProblemTagRelationManager.getPopularTags()按类型分析热门算法方向
            // TODO 5. 提供类型发展趋势和新兴算法领域的识别
            List<Map<String, Object>> result = problemManager.getStatisticsByType();

            log.info("ProblemStatisticsService--->按题目类型获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->按题目类型获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按题目类型获取统计信息失败", e);
        }
    }

    /**
     * 按编程语言获取统计信息
     *
     * @return 各编程语言的统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByLanguage() {
        try {
            log.info("ProblemStatisticsService--->按编程语言获取统计信息开始");

            // TODO 多表联查优化：在ProblemStatisticsManager中优化按语言统计的方法
            // TODO 通过多表联查分析编程语言的使用模式和效果：
            // TODO 1. JOIN submission 表统计各语言的实际使用率、成功率和性能表现
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析语言与算法类型的适配情况
            // TODO 3. JOIN user 表分析不同用户群体的语言选择偏好和熟练度
            // TODO 4. 分析语言在不同难度和类型题目中的表现差异
            // TODO 5. 提供语言学习路径建议和最佳实践指导
            List<Map<String, Object>> result = problemManager.getStatisticsByLanguage();

            log.info("ProblemStatisticsService--->按编程语言获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->按编程语言获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按编程语言获取统计信息失败", e);
        }
    }

    /**
     * 按状态获取统计信息
     *
     * @return 各状态的统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByStatus() {
        try {
            log.info("ProblemStatisticsService--->按状态获取统计信息");
            
            // TODO 多表联查优化：在ProblemStatisticsManager中优化按状态统计的方法
            // TODO 通过多表联查分析题目状态的分布和转换模式：
            // TODO 1. JOIN submission 表分析不同状态题目的用户参与度和活跃程度
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析各状态题目的标签分布特征
            // TODO 3. JOIN user 表分析题目状态变化的创建者行为模式
            // TODO 4. 调用ProblemTagRelationManager.findByConditions()分析状态与标签的关联
            // TODO 5. 提供题目生命周期管理和质量控制的数据支持
            List<Map<String, Object>> result = problemManager.getStatisticsByStatus();
            
            log.info("ProblemStatisticsService--->按状态获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->按状态获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按状态获取统计信息失败", e);
        }
    }

    /**
     * 获取指定题目的通过率
     *
     * @param problemId 题目ID
     * @return 题目的通过率，如果题目不存在或从未被提交过，则返回0.0
     */
    @Override
    public Double getAcceptanceRate(Long problemId) {
        try {
            log.info("ProblemStatisticsService--->获取题目通过率, 题目ID: {}", problemId);

            // 参数校验
            if (problemId == null || problemId <= 0) {
                log.warn("获取题目通过率失败：题目ID无效");
                return 0.0;
            }

            // TODO 多表联查优化：在ProblemStatisticsManager中新增getDetailedAcceptanceRate()方法
            // TODO 通过多表联查提供更精准的通过率分析：
            // TODO 1. JOIN submission 表分析提交的时间分布、语言分布、用户水平分布
            // TODO 2. JOIN problem_tag_relation 和 problem_tag 表分析通过率与算法标签的关联性
            // TODO 3. JOIN user 表分析不同用户群体在该题目上的表现差异
            // TODO 4. 提供通过率的趋势变化分析和影响因素识别
            // TODO 5. 为题目难度评估和推荐算法提供更准确的数据支持
            // 调用manager层获取通过率
            Double acceptanceRate = problemManager.getAcceptanceRate(problemId);

            log.info("获取题目通过率成功, 题目ID: {}, 通过率: {}", problemId, acceptanceRate);
            return acceptanceRate;
        } catch (Exception e) {
            log.error("ProblemStatisticsService--->获取题目通过率失败: {}", e.getMessage(), e);
            return 0.0;
        }
    }
}