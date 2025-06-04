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