package fun.timu.oj.judge.utils;

import fun.timu.oj.judge.model.DO.TestCaseDO;
import lombok.experimental.UtilityClass;

/**
 * 测试用例工具类
 */
@UtilityClass
public class TestCaseUtils {
    
    /**
     * 为测试用例设置默认值
     *
     * @param testCase 测试用例对象
     */
    public static void setDefaultValues(TestCaseDO testCase) {
        if (testCase == null) {
            return;
        }
        
        if (testCase.getIsDeleted() == null) {
            testCase.setIsDeleted(0);
        }
        if (testCase.getStatus() == null) {
            testCase.setStatus(1); // 默认启用
        }
        if (testCase.getIsExample() == null) {
            testCase.setIsExample(0); // 默认非示例
        }
        if (testCase.getIsPublic() == null) {
            testCase.setIsPublic(0); // 默认非公开
        }
        if (testCase.getWeight() == null) {
            testCase.setWeight(1); // 默认权重1
        }
        if (testCase.getOrderIndex() == null) {
            testCase.setOrderIndex(0); // 默认顺序0
        }
        if (testCase.getExecutionCount() == null) {
            testCase.setExecutionCount(0L);
        }
        if (testCase.getSuccessCount() == null) {
            testCase.setSuccessCount(0L);
        }
    }
    
    /**
     * 验证测试用例基本信息是否有效
     *
     * @param testCase 测试用例对象
     * @return 是否有效
     */
    public static boolean isValidTestCase(TestCaseDO testCase) {
        return testCase != null 
            && testCase.getProblemId() != null 
            && testCase.getInputData() != null 
            && testCase.getExpectedOutput() != null;
    }
    
    /**
     * 计算成功率
     *
     * @param successCount 成功次数
     * @param totalCount 总次数
     * @return 成功率（0-100）
     */
    public static double calculateSuccessRate(Long successCount, Long totalCount) {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        if (successCount == null) {
            return 0.0;
        }
        return Math.round(successCount * 100.0 / totalCount * 100.0) / 100.0;
    }
}
