package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.model.DO.CodeExecutionRecordDO;

import java.util.List;

public interface CodeExecutionRecordManager {

    /**
     * 根据ID查询执行记录
     *
     * @param id
     * @return
     */
    public CodeExecutionRecordDO findById(Long id);

    /**
     * 根据请求ID查询执行记录
     *
     * @param requestId
     * @return
     */
    public CodeExecutionRecordDO findByRequestId(String requestId);

    /**
     * 根据用户ID分页查询执行记录
     *
     * @param accountNo
     * @param current
     * @param size
     * @return
     */
    public Page<CodeExecutionRecordDO> findByAccountNo(Long accountNo, int current, int size);

    /**
     * 根据题目ID查询执行记录
     *
     * @param problemId
     * @return
     */
    public List<CodeExecutionRecordDO> findByProblemId(Long problemId);

    /**
     * 根据用户ID和题目ID查询执行记录
     *
     * @param accountNo
     * @param problemId
     * @return
     */
    public List<CodeExecutionRecordDO> findByAccountNoAndProblemId(Long accountNo, Long problemId);

    /**
     * 根据编程语言查询执行记录
     *
     * @param language
     * @param limit
     * @return
     */
    public List<CodeExecutionRecordDO> findByLanguage(String language, int limit);

    /**
     * 查询成功次数
     *
     * @param accountNo
     * @return
     */
    public Long countSuccessByAccountNo(Long accountNo);

    /**
     * 保存执行记录
     *
     * @param recordDO
     * @return
     */
    public int save(CodeExecutionRecordDO recordDO);

    /**
     * 更新执行记录
     *
     * @param recordDO
     * @return
     */
    public int updateById(CodeExecutionRecordDO recordDO);

    /**
     * 删除执行记录
     *
     * @param id
     * @return
     */
    public int deleteById(Long id);

    /**
     * 获取用户最近的执行记录
     *
     * @param accountNo 用户ID
     * @param limit     限制条数
     * @return 最近的执行记录列表
     */
    public List<CodeExecutionRecordDO> findRecentByAccountNo(Long accountNo, int limit);

    /**
     * 根据账号查询执行记录数量
     *
     * @param accountNo 账号
     * @return 执行记录数量
     */
    public Long countByAccountNo(Long accountNo);
}
