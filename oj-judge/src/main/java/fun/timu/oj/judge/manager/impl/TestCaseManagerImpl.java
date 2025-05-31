package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.timu.oj.judge.manager.TestCaseManager;
import fun.timu.oj.judge.mapper.TestCaseMapper;
import fun.timu.oj.judge.model.DO.TestCaseDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestCaseManagerImpl implements TestCaseManager {
    private final TestCaseMapper testCaseMapper;

    public TestCaseManagerImpl(TestCaseMapper testCaseMapper) {
        this.testCaseMapper = testCaseMapper;
    }

    /**
     * 根据测试用例ID查找测试用例详情
     *
     * @param id 测试用例的唯一标识符
     * @return 返回TestCaseDO对象，包含测试用例的详细信息如果找不到对应的测试用例，则返回null
     */
    @Override
    public TestCaseDO findById(Long id) {
        return testCaseMapper.selectById(id);
    }


    /**
     * 根据问题ID查询测试用例列表
     * 此方法用于获取与特定问题相关联的所有测试用例，这些测试用例被认为是未删除且已启用的
     *
     * @param problemId 问题的唯一标识符，用于数据库查询
     * @return 返回一个测试用例列表，这些测试用例与指定的问题相关联，并且是按顺序索引升序排列的
     */
    @Override
    public List<TestCaseDO> findByProblemId(Long problemId) {
        // 创建一个Lambda查询包装器，用于构建查询条件和排序
        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件，匹配问题ID、未删除标记和启用状态
        queryWrapper.eq(TestCaseDO::getProblemId, problemId)
                .eq(TestCaseDO::getIsDeleted, false)
                .eq(TestCaseDO::getStatus, 1) // 只查询启用的测试用例
                .orderByAsc(TestCaseDO::getOrderIndex);
        // 执行查询并返回结果列表
        return testCaseMapper.selectList(queryWrapper);
    }


    /**
     * 根据问题ID查找示例测试用例
     * <p>
     * 本方法通过指定的问题ID查询相关的测试用例数据对象（TestCaseDO），这些对象在数据库中被标记为示例，
     * 且未被删除，状态为1这通常表示这些记录是有效且可展示的示例用例
     *
     * @param problemId 问题的唯一标识符，用于数据库查询
     * @return 返回一个测试用例数据对象（TestCaseDO）列表，这些对象与指定的问题ID相关联，
     * 被标记为示例用例，且按照顺序索引升序排列
     */
    @Override
    public List<TestCaseDO> findExamplesByProblemId(Long problemId) {
        // 创建一个Lambda查询包装器，用于构建复杂的查询条件
        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：匹配问题ID、是示例用例、未被删除、状态为1
        queryWrapper.eq(TestCaseDO::getProblemId, problemId)
                .eq(TestCaseDO::getIsExample, true)
                .eq(TestCaseDO::getIsDeleted, false)
                .eq(TestCaseDO::getStatus, 1)
                // 设置排序条件：按照顺序索引升序排列
                .orderByAsc(TestCaseDO::getOrderIndex);
        // 执行查询并返回结果列表
        return testCaseMapper.selectList(queryWrapper);
    }


    /**
     * 根据问题ID查找公开的测试用例
     * <p>
     * 此方法用于查询与特定问题ID关联的所有公开且未删除的测试用例它确保了返回的测试用例是有效的，
     * 即状态为1（通常表示激活或可用），并且根据测试用例的顺序索引进行升序排列
     *
     * @param problemId 问题的唯一标识符
     * @return 返回一个TestCaseDO对象列表，每个对象代表一个符合查询条件的测试用例
     */
    @Override
    public List<TestCaseDO> findPublicByProblemId(Long problemId) {
        // 创建一个Lambda查询包装器，用于构建复杂的查询条件
        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();

        // 设置查询条件：匹配问题ID、公开状态、未删除状态、状态为1
        queryWrapper.eq(TestCaseDO::getProblemId, problemId)
                .eq(TestCaseDO::getIsPublic, true)
                .eq(TestCaseDO::getIsDeleted, false)
                .eq(TestCaseDO::getStatus, 1)
                // 按顺序索引升序排列结果
                .orderByAsc(TestCaseDO::getOrderIndex);

        // 执行查询并返回结果列表
        return testCaseMapper.selectList(queryWrapper);
    }


    /**
     * 根据问题ID和用例类型查询测试用例列表
     * <p>
     * 此方法用于从数据库中筛选出特定问题ID和用例类型的测试用例，并确保这些用例未被删除且处于激活状态
     * 它首先创建一个查询条件对象，然后设置多个查询条件，包括问题ID、用例类型、未删除状态和激活状态，
     * 最后根据排序索引对结果进行升序排序，以确保返回的测试用例列表是有序的
     *
     * @param problemId 问题的唯一标识符，用于定位特定问题的测试用例
     * @param caseType  用例类型，用于过滤出特定类型的测试用例
     * @return 返回一个测试用例对象列表，这些用例符合给定的问题ID和用例类型条件
     */
    @Override
    public List<TestCaseDO> findByProblemIdAndType(Long problemId, String caseType) {
        // 创建一个Lambda查询条件对象，用于构建后续的查询条件
        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件，包括问题ID、用例类型、未删除状态和激活状态
        queryWrapper.eq(TestCaseDO::getProblemId, problemId)
                .eq(TestCaseDO::getCaseType, caseType)
                .eq(TestCaseDO::getIsDeleted, false)
                .eq(TestCaseDO::getStatus, 1)
                // 对结果进行排序，确保返回的列表是根据排序索引升序排列的
                .orderByAsc(TestCaseDO::getOrderIndex);
        // 执行查询并返回结果列表
        return testCaseMapper.selectList(queryWrapper);
    }


    /**
     * 保存测试用例信息到数据库中
     *
     * @param testCaseDO 测试用例对象，包含测试用例的相关信息
     * @return 返回插入操作的影响行数，通常为1表示成功，0表示失败
     */
    @Override
    public int save(TestCaseDO testCaseDO) {
        // 调用Mapper接口的插入方法，将测试用例对象插入到数据库中
        return testCaseMapper.insert(testCaseDO);
    }


    /**
     * 根据ID更新测试用例信息
     *
     * @param testCaseDO 包含更新信息的测试用例数据对象
     * @return 更新操作影响的行数，通常为1，如果未找到对应ID的记录则为0
     */
    @Override
    public int updateById(TestCaseDO testCaseDO) {
        return testCaseMapper.updateById(testCaseDO);
    }


    /**
     * 根据ID删除测试用例
     * 逻辑删除测试用例，将其标记为已删除
     *
     * @param id 测试用例的ID
     * @return 影响的行数，1表示删除成功，0表示删除失败或记录不存在
     */
    @Override
    public int deleteById(Long id) {
        // 创建一个TestCaseDO对象，用于封装要更新的数据
        TestCaseDO testCaseDO = new TestCaseDO();
        // 设置要删除的测试用例的ID
        testCaseDO.setId(id);
        // 设置删除标志，1通常表示已删除
        testCaseDO.setIsDeleted(1);
        // 调用Mapper的updateById方法，根据ID更新测试用例的删除标志
        return testCaseMapper.updateById(testCaseDO);
    }


    /**
     * 根据问题ID删除测试用例
     * <p>
     * 该方法通过更新测试用例的is_deleted字段为1，来实现逻辑删除
     * 使用LambdaQueryWrapper构建查询条件，以匹配并更新具有指定问题ID的测试用例
     *
     * @param problemId 问题的唯一标识符
     * @return 返回受影响的行数，表示删除操作影响的测试用例数量
     */
    @Override
    public int deleteByProblemId(Long problemId) {
        // 构建查询条件，选择问题ID等于给定值的测试用例
        LambdaQueryWrapper<TestCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCaseDO::getProblemId, problemId);

        // 创建一个更新对象，设置is_deleted字段为1，表示测试用例已被删除
        TestCaseDO updateObj = new TestCaseDO();
        updateObj.setIsDeleted(1);

        // 执行更新操作，返回受影响的行数
        return testCaseMapper.update(updateObj, queryWrapper);
    }


    /**
     * 更新测试用例的执行统计信息
     *
     * @param testCaseId 测试用例的唯一标识符
     * @param isSuccess  表示测试用例执行是否成功
     * @return 返回更新操作的影响行数，失败则返回0
     */
    @Override
    public int updateExecutionStats(Long testCaseId, boolean isSuccess) {
        // 根据测试用例ID查询测试用例详细信息
        TestCaseDO testCaseDO = testCaseMapper.selectById(testCaseId);
        // 如果找到对应的测试用例，则更新其执行统计信息
        if (testCaseDO != null) {
            // 增加测试用例的执行次数
            testCaseDO.setExecutionCount(testCaseDO.getExecutionCount() + 1);
            // 如果执行成功，则增加成功次数
            if (isSuccess) {
                testCaseDO.setSuccessCount(testCaseDO.getSuccessCount() + 1);
            }
            // 更新数据库中的测试用例信息，并返回影响行数
            return testCaseMapper.updateById(testCaseDO);
        }
        // 未找到对应的测试用例，返回0表示更新失败
        return 0;
    }

}
