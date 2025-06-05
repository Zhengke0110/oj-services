package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.model.DO.CodeExecutionRecordDO;

import java.util.HashMap;
import java.util.List;

/**
 * 代码执行记录管理器接口
 * 提供代码执行记录表的基础CRUD操作和业务逻辑
 *
 * @author zhengke
 */
public interface CodeExecutionRecordManager {

    // ================== 基础CRUD操作 ==================

    /**
     * 根据ID查询代码执行记录
     *
     * @param id 记录ID
     * @return 代码执行记录对象
     */
    CodeExecutionRecordDO findById(Long id);

    /**
     * 保存代码执行记录
     *
     * @param codeExecutionRecord 代码执行记录对象
     * @return 保存结果，返回受影响的行数
     */
    int save(CodeExecutionRecordDO codeExecutionRecord);

    /**
     * 根据ID更新代码执行记录
     *
     * @param codeExecutionRecord 代码执行记录对象
     * @return 更新结果，返回受影响的行数
     */
    int updateById(CodeExecutionRecordDO codeExecutionRecord);

    /**
     * 根据ID删除代码执行记录（软删除）
     *
     * @param id 记录ID
     * @return 删除结果，返回受影响的行数
     */
    int deleteById(Long id);

    // ================== 按条件查询操作 ==================

    /**
     * 根据请求ID查询代码执行记录
     *
     * @param requestId 请求唯一标识
     * @return 代码执行记录对象
     */
    CodeExecutionRecordDO findByRequestId(String requestId);

    /**
     * 根据用户ID查询代码执行记录
     *
     * @param accountNo 用户唯一标识
     * @return 代码执行记录列表
     */
    List<CodeExecutionRecordDO> findByAccountNo(Long accountNo);

    /**
     * 根据问题ID查询代码执行记录
     *
     * @param problemId 问题ID
     * @return 代码执行记录列表
     */
    List<CodeExecutionRecordDO> findByProblemId(Long problemId);

    /**
     * 根据用户ID和问题ID查询代码执行记录
     *
     * @param accountNo 用户唯一标识
     * @param problemId 问题ID
     * @return 代码执行记录列表
     */
    List<CodeExecutionRecordDO> findByAccountNoAndProblemId(Long accountNo, Long problemId);

    /**
     * 根据编程语言查询代码执行记录
     *
     * @param language 编程语言
     * @return 代码执行记录列表
     */
    List<CodeExecutionRecordDO> findByLanguage(String language);

    /**
     * 根据执行状态查询代码执行记录
     *
     * @param executionStatus 执行状态
     * @return 代码执行记录列表
     */
    List<CodeExecutionRecordDO> findByExecutionStatus(String executionStatus);

    /**
     * 根据代码哈希查询代码执行记录
     *
     * @param codeHash 代码内容SHA256哈希值
     * @return 代码执行记录列表
     */
    List<CodeExecutionRecordDO> findByCodeHash(String codeHash);

    /**
     * 查询成功执行的记录
     *
     * @return 成功执行记录列表
     */
    List<CodeExecutionRecordDO> findSuccessfulExecutions();

    /**
     * 查询失败执行的记录
     *
     * @return 失败执行记录列表
     */
    List<CodeExecutionRecordDO> findFailedExecutions();

    // ================== 分页查询操作 ==================

    /**
     * 分页查询所有代码执行记录
     *
     * @param page 分页对象
     * @return 分页结果
     */
    IPage<CodeExecutionRecordDO> findPage(Page<CodeExecutionRecordDO> page);

    /**
     * 分页查询指定用户的代码执行记录
     *
     * @param page      分页对象
     * @param accountNo 用户唯一标识
     * @return 分页结果
     */
    IPage<CodeExecutionRecordDO> findPageByAccountNo(Page<CodeExecutionRecordDO> page, Long accountNo);

    /**
     * 分页查询指定问题的代码执行记录
     *
     * @param page      分页对象
     * @param problemId 问题ID
     * @return 分页结果
     */
    IPage<CodeExecutionRecordDO> findPageByProblemId(Page<CodeExecutionRecordDO> page, Long problemId);

    /**
     * 分页查询指定语言的代码执行记录
     *
     * @param page     分页对象
     * @param language 编程语言
     * @return 分页结果
     */
    IPage<CodeExecutionRecordDO> findPageByLanguage(Page<CodeExecutionRecordDO> page, String language);

    // ================== 批量操作 ==================

    /**
     * 批量保存代码执行记录
     *
     * @param records 代码执行记录列表
     * @return 保存结果，返回受影响的行数
     */
    int batchSave(List<CodeExecutionRecordDO> records);

    /**
     * 批量删除代码执行记录（软删除）
     *
     * @param ids 记录ID列表
     * @return 删除结果，返回受影响的行数
     */
    int batchDeleteByIds(List<Long> ids);

    /**
     * 根据问题ID批量删除代码执行记录（软删除）
     *
     * @param problemId 问题ID
     * @return 删除结果，返回受影响的行数
     */
    int batchDeleteByProblemId(Long problemId);

    /**
     * 根据用户ID批量删除代码执行记录（软删除）
     *
     * @param accountNo 用户唯一标识
     * @return 删除结果，返回受影响的行数
     */
    int batchDeleteByAccountNo(Long accountNo);

    // ================== 统计操作 ==================

    /**
     * 统计指定用户的执行记录总数
     *
     * @param accountNo 用户唯一标识
     * @return 执行记录总数
     */
    int countByAccountNo(Long accountNo);

    /**
     * 统计指定问题的执行记录总数
     *
     * @param problemId 问题ID
     * @return 执行记录总数
     */
    int countByProblemId(Long problemId);

    /**
     * 统计指定语言的执行记录总数
     *
     * @param language 编程语言
     * @return 执行记录总数
     */
    int countByLanguage(String language);

    /**
     * 统计成功执行记录总数
     *
     * @return 成功执行记录总数
     */
    int countSuccessfulExecutions();

    /**
     * 统计失败执行记录总数
     *
     * @return 失败执行记录总数
     */
    int countFailedExecutions();

    /**
     * 统计指定用户的成功执行记录数
     *
     * @param accountNo 用户唯一标识
     * @return 成功执行记录数
     */
    int countSuccessfulExecutionsByAccountNo(Long accountNo);

    /**
     * 统计指定问题的成功执行记录数
     *
     * @param problemId 问题ID
     * @return 成功执行记录数
     */
    int countSuccessfulExecutionsByProblemId(Long problemId);

    // ================== 业务查询操作 ==================

    /**
     * 检查记录是否存在
     *
     * @param id 记录ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 检查请求ID是否已存在
     *
     * @param requestId 请求唯一标识
     * @return 是否存在
     */
    boolean existsByRequestId(String requestId);

    /**
     * 查询用户最近的执行记录
     *
     * @param accountNo 用户唯一标识
     * @param limit     限制数量
     * @return 最近执行记录列表
     */
    List<CodeExecutionRecordDO> findRecentExecutionsByAccountNo(Long accountNo, int limit);

    /**
     * 查询问题最近的执行记录
     *
     * @param problemId 问题ID
     * @param limit     限制数量
     * @return 最近执行记录列表
     */
    List<CodeExecutionRecordDO> findRecentExecutionsByProblemId(Long problemId, int limit);

    /**
     * 查询执行时间超过指定值的记录
     *
     * @param minExecutionTime 最小执行时间（毫秒）
     * @return 超时执行记录列表
     */
    List<CodeExecutionRecordDO> findLongRunningExecutions(Long minExecutionTime);

    /**
     * 查询内存使用超过指定值的记录
     *
     * @param minMemoryUsed 最小内存使用（字节）
     * @return 高内存使用记录列表
     */
    List<CodeExecutionRecordDO> findHighMemoryExecutions(Long minMemoryUsed);

    /**
     * 查询指定时间范围内的执行记录
     *
     * @param startTime 开始时间（毫秒时间戳）
     * @param endTime   结束时间（毫秒时间戳）
     * @return 时间范围内的执行记录列表
     */
    List<CodeExecutionRecordDO> findExecutionsByTimeRange(Long startTime, Long endTime);

    // ================== 统计分析操作 ==================

    /**
     * 获取执行统计信息
     *
     * @return 统计信息Map
     */
    HashMap<String, Object> getExecutionStatistics();

    /**
     * 获取用户执行统计信息
     *
     * @param accountNo 用户唯一标识
     * @return 用户统计信息Map
     */
    HashMap<String, Object> getUserExecutionStatistics(Long accountNo);

    /**
     * 获取问题执行统计信息
     *
     * @param problemId 问题ID
     * @return 问题统计信息Map
     */
    HashMap<String, Object> getProblemExecutionStatistics(Long problemId);

    /**
     * 获取语言使用统计
     *
     * @return 语言统计列表
     */
    List<HashMap<String, Object>> getLanguageStatistics();

    /**
     * 获取执行状态统计
     *
     * @return 执行状态统计列表
     */
    List<HashMap<String, Object>> getExecutionStatusStatistics();

    /**
     * 获取热门问题统计（按执行次数排序）
     *
     * @param limit 限制数量
     * @return 热门问题统计列表
     */
    List<HashMap<String, Object>> getPopularProblems(int limit);

    /**
     * 获取活跃用户统计（按执行次数排序）
     *
     * @param limit 限制数量
     * @return 活跃用户统计列表
     */
    List<HashMap<String, Object>> getActiveUsers(int limit);
}
