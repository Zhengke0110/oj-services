package fun.timu.oj.judge.manager;

import fun.timu.oj.judge.model.DO.TestCaseDO;

import java.util.List;

public interface TestCaseManager {
    /**
     * 根据ID查询测试用例
     *
     * @param id 测试用例ID
     * @return 测试用例对象
     */
    public TestCaseDO findById(Long id);

    /**
     * 根据问题ID查询所有测试用例
     *
     * @param problemId 问题ID
     * @return 测试用例列表
     */
    public List<TestCaseDO> findByProblemId(Long problemId);

    /**
     * 根据问题ID查询所有示例测试用例
     *
     * @param problemId 问题ID
     * @return 测试用例列表
     */
    public List<TestCaseDO> findExamplesByProblemId(Long problemId);

    /**
     * 根据问题ID查询所有公开测试用例
     *
     * @param problemId 问题ID
     * @return 测试用例列表
     */
    public List<TestCaseDO> findPublicByProblemId(Long problemId);

    /**
     * 根据问题ID和类型查询测试用例
     *
     * @param problemId 问题ID
     * @param caseType  测试用例类型
     * @return 测试用例列表
     */
    public List<TestCaseDO> findByProblemIdAndType(Long problemId, String caseType);

    /**
     * 保存测试用例
     *
     * @param testCaseDO 测试用例对象
     * @return 影响的行数
     */
    public int save(TestCaseDO testCaseDO);

    /**
     * 更新测试用例
     *
     * @param testCaseDO 测试用例对象
     * @return 影响的行数
     */
    public int updateById(TestCaseDO testCaseDO);

    /**
     * 根据ID删除测试用例
     *
     * @param id 测试用例ID
     * @return 影响的行数
     */
    public int deleteById(Long id);

    /**
     * 根据问题ID删除测试用例
     *
     * @param problemId 问题ID
     * @return 影响的行数
     */
    public int deleteByProblemId(Long problemId);

    /**
     * 更新测试用例的执行统计信息
     *
     * @param testCaseId 测试用例ID
     * @param isSuccess  是否成功
     * @return 影响的行数
     */
    public int updateExecutionStats(Long testCaseId, boolean isSuccess);
}
