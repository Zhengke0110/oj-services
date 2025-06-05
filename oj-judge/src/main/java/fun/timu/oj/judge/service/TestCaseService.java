package fun.timu.oj.judge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.model.DTO.TestCaseConfigDTO;
import fun.timu.oj.judge.model.DTO.TestCaseValidationDTO;
import fun.timu.oj.judge.model.DO.TestCaseDO;

import java.util.HashMap;
import java.util.List;

/**
 * 测试用例服务接口
 * 提供测试用例相关的业务服务
 *
 * @author zhengke
 */
public interface TestCaseService {

    // ================== 基础CRUD操作 ==================

    /**
     * 根据ID查询测试用例
     *
     * @param id 测试用例ID
     * @return 测试用例对象
     */
    TestCaseDO findById(Long id);

    /**
     * 保存测试用例
     *
     * @param testCaseDO 测试用例对象
     * @return 保存结果，返回受影响的行数
     */
    int save(TestCaseDO testCaseDO);

    /**
     * 根据ID更新测试用例
     *
     * @param testCaseDO 测试用例对象
     * @return 更新结果，返回受影响的行数
     */
    int updateById(TestCaseDO testCaseDO);

    /**
     * 根据ID删除测试用例（软删除）
     *
     * @param id 测试用例ID
     * @return 删除结果，返回受影响的行数
     */
    int deleteById(Long id);

    // ================== 按条件查询操作 ==================

    /**
     * 根据题目ID查询所有测试用例
     *
     * @param problemId 题目ID
     * @return 测试用例列表
     */
    List<TestCaseDO> findByProblemId(Long problemId);

    /**
     * 根据题目ID查询启用的测试用例
     *
     * @param problemId 题目ID
     * @return 启用的测试用例列表
     */
    List<TestCaseDO> findEnabledByProblemId(Long problemId);

    /**
     * 根据题目ID查询示例测试用例
     *
     * @param problemId 题目ID
     * @return 示例测试用例列表
     */
    List<TestCaseDO> findExamplesByProblemId(Long problemId);

    /**
     * 根据题目ID查询公开测试用例
     *
     * @param problemId 题目ID
     * @return 公开测试用例列表
     */
    List<TestCaseDO> findPublicByProblemId(Long problemId);

    /**
     * 根据测试用例类型查询
     *
     * @param caseType 测试用例类型
     * @return 测试用例列表
     */
    List<TestCaseDO> findByCaseType(String caseType);

    /**
     * 根据题目ID和测试用例类型查询
     *
     * @param problemId 题目ID
     * @param caseType  测试用例类型
     * @return 测试用例列表
     */
    List<TestCaseDO> findByProblemIdAndCaseType(Long problemId, String caseType);

    // ================== 分页查询操作 ==================

    /**
     * 分页查询测试用例
     *
     * @param page 分页参数
     * @return 分页结果
     */
    IPage<TestCaseDO> findPage(Page<TestCaseDO> page);

    /**
     * 根据题目ID分页查询测试用例
     *
     * @param page      分页参数
     * @param problemId 题目ID
     * @return 分页结果
     */
    IPage<TestCaseDO> findPageByProblemId(Page<TestCaseDO> page, Long problemId);

    // ================== 批量操作 ==================

    /**
     * 批量保存测试用例
     *
     * @param testCases 测试用例列表
     * @return 成功保存的数量
     */
    int batchSave(List<TestCaseDO> testCases);

    /**
     * 批量删除测试用例（软删除）
     *
     * @param ids 测试用例ID列表
     * @return 成功删除的数量
     */
    int batchDeleteByIds(List<Long> ids);

    /**
     * 根据题目ID删除所有测试用例（软删除）
     *
     * @param problemId 题目ID
     * @return 删除的数量
     */
    int deleteByProblemId(Long problemId);

    // ================== 状态操作 ==================

    /**
     * 启用测试用例
     *
     * @param id 测试用例ID
     * @return 操作结果
     */
    int enableTestCase(Long id);

    /**
     * 禁用测试用例
     *
     * @param id 测试用例ID
     * @return 操作结果
     */
    int disableTestCase(Long id);

    /**
     * 批量更新测试用例状态
     *
     * @param ids    测试用例ID列表
     * @param status 目标状态（0-禁用，1-启用）
     * @return 更新的数量
     */
    int batchUpdateStatus(List<Long> ids, Integer status);

    // ================== 统计操作 ==================

    /**
     * 统计题目的测试用例数量
     *
     * @param problemId 题目ID
     * @return 测试用例数量
     */
    int countByProblemId(Long problemId);

    /**
     * 统计题目的启用测试用例数量
     *
     * @param problemId 题目ID
     * @return 启用的测试用例数量
     */
    int countEnabledByProblemId(Long problemId);

    /**
     * 统计题目的示例测试用例数量
     *
     * @param problemId 题目ID
     * @return 示例测试用例数量
     */
    int countExamplesByProblemId(Long problemId);

    // ================== 业务操作 ==================

    /**
     * 检查测试用例是否存在
     *
     * @param id 测试用例ID
     * @return true如果存在，false如果不存在
     */
    boolean existsById(Long id);

    /**
     * 检查题目是否有测试用例
     *
     * @param problemId 题目ID
     * @return true如果有测试用例，false如果没有
     */
    boolean existsByProblemId(Long problemId);

    /**
     * 更新测试用例执行统计
     *
     * @param id      测试用例ID
     * @param success 是否执行成功
     * @return 更新结果
     */
    int updateExecutionStatistics(Long id, boolean success);

    /**
     * 重置题目测试用例的执行顺序
     *
     * @param problemId 题目ID
     * @return 更新的数量
     */
    int resetOrderIndexByProblemId(Long problemId);

    // ================== 高级查询操作（使用自定义SQL） ==================

    /**
     * 查询测试用例统计信息
     *
     * @param problemId 题目ID，可为null表示查询所有题目
     * @return 统计信息列表
     */
    List<HashMap<String, Object>> getTestCaseStatistics(Long problemId);

    /**
     * 查询执行失败率较高的测试用例
     *
     * @param failureThreshold 失败率阈值（百分比，如10.0表示10%）
     * @param problemId        题目ID，可为null
     * @param limit            限制数量，可为null
     * @return 失败率较高的测试用例列表
     */
    List<TestCaseDO> getHighFailureRateTestCases(Double failureThreshold, Long problemId, Integer limit);

    /**
     * 批量更新测试用例的执行顺序
     *
     * @param testCases 测试用例列表（需包含id和orderIndex）
     * @return 更新的数量
     */
    int batchUpdateOrderIndex(List<TestCaseDO> testCases);

    /**
     * 获取题目下权重最大的测试用例权重值
     *
     * @param problemId 题目ID
     * @return 最大权重值，如果没有测试用例返回0
     */
    int getMaxWeightByProblemId(Long problemId);

    /**
     * 查询相同输入数据的测试用例（用于去重检查）
     *
     * @param problemId 题目ID
     * @return 有重复输入数据的测试用例列表
     */
    List<TestCaseDO> getDuplicateInputDataTestCases(Long problemId);

    /**
     * 根据输入格式查询测试用例
     *
     * @param inputFormat 输入格式（TEXT/JSON/ARGS/NONE）
     * @param problemId   题目ID，可为null
     * @return 测试用例列表
     */
    List<TestCaseDO> findByInputFormat(String inputFormat, Long problemId);

    /**
     * 查询需要特殊配置的测试用例（有时间或内存限制覆盖的）
     *
     * @param problemId 题目ID，可为null
     * @return 有特殊限制配置的测试用例列表
     */
    List<TestCaseDO> getTestCasesWithSpecialLimits(Long problemId);

    /**
     * 批量插入测试用例（使用自定义SQL优化性能）
     *
     * @param testCases 测试用例列表
     * @return 插入的数量
     */
    int batchInsertTestCases(List<TestCaseDO> testCases);

    /**
     * 查询测试用例执行统计摘要（按类型分组）
     *
     * @param problemId 题目ID，可为null
     * @return 执行统计摘要
     */
    List<HashMap<String, Object>> getExecutionSummary(Long problemId);

    // ================== 数据验证和完整性检查 ==================

    /**
     * 验证测试用例数据完整性
     *
     * @param problemId 题目ID
     * @return 验证结果信息
     */
    TestCaseValidationDTO validateTestCaseIntegrity(Long problemId);

    /**
     * 检查题目的测试用例配置是否合理
     *
     * @param problemId 题目ID
     * @return 检查结果和建议
     */
    TestCaseConfigDTO checkTestCaseConfiguration(Long problemId);
}
