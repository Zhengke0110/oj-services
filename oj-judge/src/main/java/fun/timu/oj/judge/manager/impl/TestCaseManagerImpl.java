package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.TestCaseManager;
import fun.timu.oj.judge.model.DTO.TestCaseConfigDTO;
import fun.timu.oj.judge.model.DTO.TestCaseValidationDTO;
import fun.timu.oj.judge.utils.TestCaseUtils;
import fun.timu.oj.judge.mapper.TestCaseMapper;
import fun.timu.oj.judge.model.DO.TestCaseDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试用例管理器实现类
 * 基于MyBatis-Plus实现基础CRUD操作和业务逻辑
 *
 * @author zhengke
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestCaseManagerImpl implements TestCaseManager {

    private final TestCaseMapper testCaseMapper;

    // ================== 参数校验工具方法 ==================

    /**
     * 校验ID参数
     */
    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 校验problemId参数
     */
    private boolean isValidProblemId(Long problemId) {
        return problemId != null && problemId > 0;
    }

    /**
     * 校验字符串参数
     */
    private boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    // ================== 基础CRUD操作 ==================

    @Override
    public TestCaseDO findById(Long id) {
        if (!isValidId(id)) {
            return null;
        }
        return testCaseMapper.selectById(id);
    }

    @Override
    public int save(TestCaseDO testCaseDO) {
        if (testCaseDO == null || !TestCaseUtils.isValidTestCase(testCaseDO)) {
            return 0;
        }

        // 设置默认值
        TestCaseUtils.setDefaultValues(testCaseDO);
        return testCaseMapper.insert(testCaseDO);
    }

    @Override
    public int updateById(TestCaseDO testCaseDO) {
        if (testCaseDO == null || !isValidId(testCaseDO.getId())) {
            return 0;
        }
        return testCaseMapper.updateById(testCaseDO);
    }

    @Override
    public int deleteById(Long id) {
        if (!isValidId(id)) {
            return 0;
        }

        TestCaseDO testCaseDO = new TestCaseDO();
        testCaseDO.setId(id);
        testCaseDO.setIsDeleted(1);
        return testCaseMapper.updateById(testCaseDO);
    }

    // ================== 按条件查询操作 ==================

    @Override
    public List<TestCaseDO> findByProblemId(Long problemId) {
        if (!isValidProblemId(problemId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getIsDeleted, 0).orderByAsc(TestCaseDO::getOrderIndex, TestCaseDO::getId);

        return testCaseMapper.selectList(queryWrapper);
    }

    @Override
    public List<TestCaseDO> findEnabledByProblemId(Long problemId) {
        if (!isValidProblemId(problemId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getStatus, 1).eq(TestCaseDO::getIsDeleted, 0).orderByAsc(TestCaseDO::getOrderIndex, TestCaseDO::getId);

        return testCaseMapper.selectList(queryWrapper);
    }

    @Override
    public List<TestCaseDO> findExamplesByProblemId(Long problemId) {
        if (problemId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getIsExample, 1).eq(TestCaseDO::getIsDeleted, 0).orderByAsc(TestCaseDO::getOrderIndex, TestCaseDO::getId);

        return testCaseMapper.selectList(queryWrapper);
    }

    @Override
    public List<TestCaseDO> findPublicByProblemId(Long problemId) {
        if (problemId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getIsPublic, 1).eq(TestCaseDO::getIsDeleted, 0).orderByAsc(TestCaseDO::getOrderIndex, TestCaseDO::getId);

        return testCaseMapper.selectList(queryWrapper);
    }

    @Override
    public List<TestCaseDO> findByCaseType(String caseType) {
        if (caseType == null || caseType.trim().isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getCaseType, caseType).eq(TestCaseDO::getIsDeleted, 0).orderByAsc(TestCaseDO::getProblemId, TestCaseDO::getOrderIndex, TestCaseDO::getId);

        return testCaseMapper.selectList(queryWrapper);
    }

    @Override
    public List<TestCaseDO> findByProblemIdAndCaseType(Long problemId, String caseType) {
        if (problemId == null || caseType == null || caseType.trim().isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getCaseType, caseType).eq(TestCaseDO::getIsDeleted, 0).orderByAsc(TestCaseDO::getOrderIndex, TestCaseDO::getId);

        return testCaseMapper.selectList(queryWrapper);
    }

    // ================== 分页查询操作 ==================

    @Override
    public IPage<TestCaseDO> findPage(Page<TestCaseDO> page) {
        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getIsDeleted, 0).orderByDesc(TestCaseDO::getCreatedAt);

        return testCaseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<TestCaseDO> findPageByProblemId(Page<TestCaseDO> page, Long problemId) {
        if (problemId == null) {
            return page;
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getIsDeleted, 0).orderByAsc(TestCaseDO::getOrderIndex, TestCaseDO::getId);

        return testCaseMapper.selectPage(page, queryWrapper);
    }

    // ================== 批量操作 ==================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<TestCaseDO> testCases) {
        if (CollectionUtils.isEmpty(testCases)) {
            return 0;
        }

        // 预处理：设置默认值和验证
        List<TestCaseDO> validTestCases = testCases.stream().filter(TestCaseUtils::isValidTestCase).peek(TestCaseUtils::setDefaultValues).collect(Collectors.toList());

        if (validTestCases.isEmpty()) {
            log.warn("批量保存测试用例：没有有效的测试用例");
            return 0;
        }

        // 使用批量插入提高性能
        return testCaseMapper.batchInsert(validTestCases);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }

        // 过滤有效的ID
        List<Long> validIds = ids.stream().filter(this::isValidId).collect(Collectors.toList());

        if (validIds.isEmpty()) {
            return 0;
        }

        LambdaUpdateWrapper<TestCaseDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(TestCaseDO::getId, validIds).set(TestCaseDO::getIsDeleted, 1);

        return testCaseMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByProblemId(Long problemId) {
        if (!isValidProblemId(problemId)) {
            return 0;
        }

        LambdaUpdateWrapper<TestCaseDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TestCaseDO::getProblemId, problemId).set(TestCaseDO::getIsDeleted, 1);

        return testCaseMapper.update(null, updateWrapper);
    }

    // ================== 状态操作 ==================

    @Override
    public int enableTestCase(Long id) {
        return updateStatus(id, 1);
    }

    @Override
    public int disableTestCase(Long id) {
        return updateStatus(id, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> ids, Integer status) {
        if (CollectionUtils.isEmpty(ids) || status == null) {
            return 0;
        }

        LambdaUpdateWrapper<TestCaseDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(TestCaseDO::getId, ids).set(TestCaseDO::getStatus, status);

        return testCaseMapper.update(null, updateWrapper);
    }

    /**
     * 更新测试用例状态的私有方法
     */
    private int updateStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return 0;
        }

        LambdaUpdateWrapper<TestCaseDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TestCaseDO::getId, id).set(TestCaseDO::getStatus, status);

        return testCaseMapper.update(null, updateWrapper);
    }

    // ================== 统计操作 ==================

    @Override
    public int countByProblemId(Long problemId) {
        if (problemId == null) {
            return 0;
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getIsDeleted, 0);

        return Math.toIntExact(testCaseMapper.selectCount(queryWrapper));
    }

    @Override
    public int countEnabledByProblemId(Long problemId) {
        if (problemId == null) {
            return 0;
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getStatus, 1).eq(TestCaseDO::getIsDeleted, 0);

        return Math.toIntExact(testCaseMapper.selectCount(queryWrapper));
    }

    @Override
    public int countExamplesByProblemId(Long problemId) {
        if (problemId == null) {
            return 0;
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getIsExample, 1).eq(TestCaseDO::getIsDeleted, 0);

        return Math.toIntExact(testCaseMapper.selectCount(queryWrapper));
    }

    // ================== 业务操作 ==================

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getId, id).eq(TestCaseDO::getIsDeleted, 0);

        return testCaseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByProblemId(Long problemId) {
        if (problemId == null) {
            return false;
        }

        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId).eq(TestCaseDO::getIsDeleted, 0);

        return testCaseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateExecutionStatistics(Long id, boolean success) {
        if (id == null) {
            return 0;
        }

        // 先查询当前的统计数据
        TestCaseDO testCase = findById(id);
        if (testCase == null) {
            return 0;
        }

        // 更新统计数据
        long executionCount = testCase.getExecutionCount() != null ? testCase.getExecutionCount() : 0L;
        long successCount = testCase.getSuccessCount() != null ? testCase.getSuccessCount() : 0L;

        executionCount++;
        if (success) {
            successCount++;
        }

        LambdaUpdateWrapper<TestCaseDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TestCaseDO::getId, id).set(TestCaseDO::getExecutionCount, executionCount).set(TestCaseDO::getSuccessCount, successCount);

        return testCaseMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetOrderIndexByProblemId(Long problemId) {
        if (problemId == null) {
            return 0;
        }

        // 查询该题目下的所有测试用例，按当前顺序排列
        List<TestCaseDO> testCases = findByProblemId(problemId);
        if (CollectionUtils.isEmpty(testCases)) {
            return 0;
        }

        // 重新设置顺序索引
        int updateCount = 0;
        for (int i = 0; i < testCases.size(); i++) {
            TestCaseDO testCase = testCases.get(i);
            LambdaUpdateWrapper<TestCaseDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TestCaseDO::getId, testCase.getId()).set(TestCaseDO::getOrderIndex, i);

            updateCount += testCaseMapper.update(null, updateWrapper);
        }

        return updateCount;
    }

    // ================== 高级查询操作（使用自定义SQL） ==================

    @Override
    public List<HashMap<String, Object>> getTestCaseStatistics(Long problemId) {
        return testCaseMapper.selectTestCaseStatistics(problemId);
    }

    @Override
    public List<TestCaseDO> getHighFailureRateTestCases(Double failureThreshold, Long problemId, Integer limit) {
        if (failureThreshold == null || failureThreshold < 0 || failureThreshold > 100) {
            return Collections.emptyList();
        }

        return testCaseMapper.selectHighFailureRateTestCases(failureThreshold, problemId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateOrderIndex(List<TestCaseDO> testCases) {
        if (CollectionUtils.isEmpty(testCases)) {
            return 0;
        }

        return testCaseMapper.batchUpdateOrderIndex(testCases);
    }

    @Override
    public int getMaxWeightByProblemId(Long problemId) {
        if (problemId == null) {
            return 0;
        }

        Integer maxWeight = testCaseMapper.selectMaxWeightByProblemId(problemId);
        return maxWeight != null ? maxWeight : 0;
    }

    @Override
    public List<TestCaseDO> getDuplicateInputDataTestCases(Long problemId) {
        if (problemId == null) {
            return Collections.emptyList();
        }

        return testCaseMapper.selectDuplicateInputData(problemId);
    }

    @Override
    public List<TestCaseDO> findByInputFormat(String inputFormat, Long problemId) {
        if (inputFormat == null || inputFormat.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return testCaseMapper.selectByInputFormat(inputFormat, problemId);
    }

    @Override
    public List<TestCaseDO> getTestCasesWithSpecialLimits(Long problemId) {
        return testCaseMapper.selectWithSpecialLimits(problemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertTestCases(List<TestCaseDO> testCases) {
        if (CollectionUtils.isEmpty(testCases)) {
            return 0;
        }

        // 为每个测试用例设置默认值
        for (TestCaseDO testCase : testCases) {
            if (testCase.getIsDeleted() == null) {
                testCase.setIsDeleted(0);
            }
            if (testCase.getStatus() == null) {
                testCase.setStatus(1);
            }
            if (testCase.getIsExample() == null) {
                testCase.setIsExample(0);
            }
            if (testCase.getIsPublic() == null) {
                testCase.setIsPublic(0);
            }
            if (testCase.getWeight() == null) {
                testCase.setWeight(1);
            }
            if (testCase.getOrderIndex() == null) {
                testCase.setOrderIndex(0);
            }
            if (testCase.getExecutionCount() == null) {
                testCase.setExecutionCount(0L);
            }
            if (testCase.getSuccessCount() == null) {
                testCase.setSuccessCount(0L);
            }
        }

        return testCaseMapper.batchInsert(testCases);
    }

    @Override
    public List<HashMap<String, Object>> getExecutionSummary(Long problemId) {
        return testCaseMapper.selectExecutionSummary(problemId);
    }

    // ================== 数据验证和完整性检查 ==================

    @Override
    public TestCaseValidationDTO validateTestCaseIntegrity(Long problemId) {
        if (!isValidProblemId(problemId)) {
            return TestCaseValidationDTO.builder().valid(false).message("题目ID不能为空或无效").build();
        }

        // 获取基本统计信息
        int totalCount = countByProblemId(problemId);
        int enabledCount = countEnabledByProblemId(problemId);
        int exampleCount = countExamplesByProblemId(problemId);

        List<String> issues = new ArrayList<>();

        // 基本数量检查
        if (totalCount == 0) {
            issues.add("题目没有测试用例");
        }
        if (enabledCount == 0 && totalCount > 0) {
            issues.add("题目没有启用的测试用例");
        }
        if (exampleCount == 0 && totalCount > 0) {
            issues.add("题目没有示例测试用例，建议至少设置一个");
        }

        // 检查重复数据
        List<TestCaseDO> duplicates = getDuplicateInputDataTestCases(problemId);
        List<Object> duplicateTestCases = null;
        if (!duplicates.isEmpty()) {
            issues.add("发现" + duplicates.size() + "个重复的输入数据测试用例");
            duplicateTestCases = new ArrayList<>(duplicates);
        }

        // 检查权重分配
        int totalWeight = 0;
        List<TestCaseDO> testCases = findByProblemId(problemId);
        if (!testCases.isEmpty()) {
            totalWeight = testCases.stream().filter(tc -> tc.getWeight() != null).mapToInt(TestCaseDO::getWeight).sum();

            if (totalWeight == 0) {
                issues.add("所有测试用例的权重总和为0");
            }
        }

        return TestCaseValidationDTO.builder().valid(issues.isEmpty()).totalCount(totalCount).enabledCount(enabledCount).exampleCount(exampleCount).totalWeight(totalWeight).issues(issues).duplicateTestCases(duplicateTestCases).build();
    }

    @Override
    public TestCaseConfigDTO checkTestCaseConfiguration(Long problemId) {
        if (!isValidProblemId(problemId)) {
            return TestCaseConfigDTO.builder().reasonable(false).message("题目ID不能为空或无效").build();
        }

        List<String> suggestions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 获取基本统计信息
        int totalCount = countByProblemId(problemId);
        int enabledCount = countEnabledByProblemId(problemId);
        int exampleCount = countExamplesByProblemId(problemId);

        // 检查测试用例数量
        checkTestCaseCount(totalCount, warnings);

        // 检查示例用例比例
        checkExampleRatio(totalCount, exampleCount, suggestions);

        // 检查启用用例比例
        checkEnabledRatio(totalCount, enabledCount, suggestions);

        // 检查权重分配和特殊限制
        Map<Integer, Long> weightDistribution = null;
        Integer specialLimitCasesCount = null;

        List<TestCaseDO> testCases = findEnabledByProblemId(problemId);
        if (!testCases.isEmpty()) {
            weightDistribution = checkWeightDistribution(testCases, suggestions, warnings);
            specialLimitCasesCount = checkSpecialLimits(problemId, suggestions);
        }

        // 生成评分和等级
        int score = calculateConfigScore(warnings.size(), suggestions.size());
        String grade = determineGrade(score);

        return TestCaseConfigDTO.builder().reasonable(warnings.isEmpty() && suggestions.size() <= 2).suggestions(suggestions).warnings(warnings).weightDistribution(weightDistribution).specialLimitCasesCount(specialLimitCasesCount).score(score).grade(grade).build();
    }

    /**
     * 检查测试用例数量
     */
    private void checkTestCaseCount(int totalCount, List<String> warnings) {
        if (totalCount < 3) {
            warnings.add("测试用例数量过少（少于3个），可能无法充分验证代码正确性");
        } else if (totalCount > 100) {
            warnings.add("测试用例数量过多（超过100个），可能影响判题性能");
        }
    }

    /**
     * 检查示例用例比例
     */
    private void checkExampleRatio(int totalCount, int exampleCount, List<String> suggestions) {
        if (totalCount > 0) {
            double exampleRatio = (double) exampleCount / totalCount;
            if (exampleRatio > 0.5) {
                suggestions.add("示例用例比例过高（" + String.format("%.1f", exampleRatio * 100) + "%），建议减少示例用例数量");
            } else if (exampleRatio == 0) {
                suggestions.add("建议至少设置1-2个示例用例帮助用户理解题意");
            }
        }
    }

    /**
     * 检查启用用例比例
     */
    private void checkEnabledRatio(int totalCount, int enabledCount, List<String> suggestions) {
        if (totalCount > 0) {
            double enabledRatio = (double) enabledCount / totalCount;
            if (enabledRatio < 0.8) {
                suggestions.add("启用用例比例较低（" + String.format("%.1f", enabledRatio * 100) + "%），建议检查是否有必要禁用这些用例");
            }
        }
    }

    /**
     * 检查权重分配
     */
    private Map<Integer, Long> checkWeightDistribution(List<TestCaseDO> testCases, List<String> suggestions, List<String> warnings) {
        Map<Integer, Long> weightDistribution = testCases.stream().collect(Collectors.groupingBy(tc -> tc.getWeight() != null ? tc.getWeight() : 1, Collectors.counting()));

        if (weightDistribution.size() == 1 && weightDistribution.containsKey(1)) {
            suggestions.add("所有测试用例权重相同，考虑根据难度设置不同权重");
        }

        // 检查是否有权重过大的测试用例
        int maxWeight = testCases.stream().mapToInt(tc -> tc.getWeight() != null ? tc.getWeight() : 1).max().orElse(1);

        if (maxWeight > 10) {
            warnings.add("存在权重过大的测试用例（权重: " + maxWeight + "），可能导致单个用例影响过大");
        }

        return weightDistribution;
    }

    /**
     * 检查特殊限制
     */
    private Integer checkSpecialLimits(Long problemId, List<String> suggestions) {
        List<TestCaseDO> specialLimitCases = getTestCasesWithSpecialLimits(problemId);
        if (!specialLimitCases.isEmpty()) {
            suggestions.add("有" + specialLimitCases.size() + "个测试用例设置了特殊时间/内存限制，请确认配置合理");
        }
        return specialLimitCases.size();
    }

    /**
     * 计算配置评分
     */
    private int calculateConfigScore(int warningCount, int suggestionCount) {
        int score = 100;
        score -= warningCount * 15; // 每个警告扣15分
        score -= suggestionCount * 5; // 每个建议扣5分
        return Math.max(score, 0);
    }

    /**
     * 确定等级评价
     */
    private String determineGrade(int score) {
        if (score >= 90) {
            return "优秀";
        } else if (score >= 80) {
            return "良好";
        } else if (score >= 70) {
            return "一般";
        } else {
            return "需要改进";
        }
    }
}
