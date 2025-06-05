package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.TestCaseDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author zhengke
 * @description 针对表【test_case(测试用例表(优化版))】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.TestCase
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCaseDO> {

    /**
     * 查询测试用例统计信息
     *
     * @param problemId 题目ID，可为null表示查询所有题目
     * @return 统计信息列表
     */
    List<HashMap<String, Object>> selectTestCaseStatistics(@Param("problemId") Long problemId);

    /**
     * 查询执行失败率较高的测试用例
     *
     * @param failureThreshold 失败率阈值（百分比）
     * @param problemId        题目ID，可为null
     * @param limit            限制数量，可为null
     * @return 失败率较高的测试用例列表
     */
    List<TestCaseDO> selectHighFailureRateTestCases(
            @Param("failureThreshold") Double failureThreshold,
            @Param("problemId") Long problemId,
            @Param("limit") Integer limit
    );

    /**
     * 批量更新测试用例的执行顺序
     *
     * @param testCases 测试用例列表（需包含id和orderIndex）
     * @return 更新的行数
     */
    int batchUpdateOrderIndex(@Param("testCases") List<TestCaseDO> testCases);

    /**
     * 查询题目下权重最大的测试用例权重值
     *
     * @param problemId 题目ID
     * @return 最大权重值
     */
    Integer selectMaxWeightByProblemId(@Param("problemId") Long problemId);

    /**
     * 查询相同输入数据的测试用例（用于去重检查）
     *
     * @param problemId 题目ID
     * @return 有重复输入数据的测试用例列表
     */
    List<TestCaseDO> selectDuplicateInputData(@Param("problemId") Long problemId);

    /**
     * 根据输入格式查询测试用例
     *
     * @param inputFormat 输入格式
     * @param problemId   题目ID，可为null
     * @return 测试用例列表
     */
    List<TestCaseDO> selectByInputFormat(
            @Param("inputFormat") String inputFormat,
            @Param("problemId") Long problemId
    );

    /**
     * 查询需要特殊配置的测试用例（有时间或内存限制覆盖的）
     *
     * @param problemId 题目ID，可为null
     * @return 有特殊限制配置的测试用例列表
     */
    List<TestCaseDO> selectWithSpecialLimits(@Param("problemId") Long problemId);

    /**
     * 批量插入测试用例
     *
     * @param testCases 测试用例列表
     * @return 插入的行数
     */
    int batchInsert(@Param("list") List<TestCaseDO> testCases);

    /**
     * 查询测试用例执行统计摘要（按类型分组）
     *
     * @param problemId 题目ID，可为null
     * @return 执行统计摘要
     */
    List<HashMap<String, Object>> selectExecutionSummary(@Param("problemId") Long problemId);
}




