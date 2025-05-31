package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.CodeExecutionRecordManager;
import fun.timu.oj.judge.mapper.CodeExecutionRecordMapper;
import fun.timu.oj.judge.model.DO.CodeExecutionRecordDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodeExecutionRecordManagerImpl implements CodeExecutionRecordManager {
    private final CodeExecutionRecordMapper codeExecutionRecordMapper;

    public CodeExecutionRecordManagerImpl(CodeExecutionRecordMapper codeExecutionRecordMapper) {
        this.codeExecutionRecordMapper = codeExecutionRecordMapper;
    }

    /**
     * 根据记录ID查找代码执行记录
     * <p>
     * 此方法通过调用MyBatis-Plus的selectById方法，实现根据主键ID查询数据库中对应的代码执行记录
     * 它主要用于需要根据记录的唯一标识符（ID）获取详细信息的场景
     *
     * @param id 代码执行记录的唯一标识符
     * @return 返回查询到的代码执行记录对象，如果没有找到，则返回null
     */
    @Override
    public CodeExecutionRecordDO findById(Long id) {
        return codeExecutionRecordMapper.selectById(id);
    }


    /**
     * 根据请求ID查找代码执行记录
     *
     * @param requestId 请求ID，用于唯一标识一个请求
     * @return 返回匹配请求ID的代码执行记录对象，如果不存在则返回null
     */
    @Override
    public CodeExecutionRecordDO findByRequestId(String requestId) {
        // 创建Lambda查询条件器，用于构建查询条件
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件，筛选出请求ID与参数requestId相等的记录
        queryWrapper.eq(CodeExecutionRecordDO::getRequestId, requestId);
        // 调用Mapper的selectOne方法，根据查询条件获取一条代码执行记录
        return codeExecutionRecordMapper.selectOne(queryWrapper);
    }


    /**
     * 根据账户编号查询代码执行记录
     *
     * @param accountNo 账户编号，用于筛选代码执行记录
     * @param current   当前页码，用于分页查询
     * @param size      每页记录数，用于分页查询
     * @return 返回一个分页对象，包含查询到的代码执行记录
     */
    @Override
    public Page<CodeExecutionRecordDO> findByAccountNo(Long accountNo, int current, int size) {
        // 初始化分页对象
        Page<CodeExecutionRecordDO> page = new Page<>(current, size);
        // 创建查询条件对象
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：根据账户编号筛选，并按提交时间降序排序
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo)
                .orderByDesc(CodeExecutionRecordDO::getSubmissionTime);
        // 执行分页查询，并返回结果
        return codeExecutionRecordMapper.selectPage(page, queryWrapper);
    }


    /**
     * 根据问题ID获取代码执行记录列表
     * <p>
     * 此方法通过问题ID查询相关的代码执行记录，并按照提交时间降序排列返回一个列表
     * 它首先创建一个Lambda查询包装器，然后设置查询条件和排序方式，最后调用Mapper的selectList方法执行查询
     *
     * @param problemId 问题的唯一标识符
     * @return 包含代码执行记录的列表
     */
    @Override
    public List<CodeExecutionRecordDO> findByProblemId(Long problemId) {
        // 创建Lambda查询包装器
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件为问题ID等于给定的值，并按提交时间降序排序
        queryWrapper.eq(CodeExecutionRecordDO::getProblemId, problemId)
                .orderByDesc(CodeExecutionRecordDO::getSubmissionTime);
        // 执行查询并返回结果列表
        return codeExecutionRecordMapper.selectList(queryWrapper);
    }


    /**
     * 根据账户编号和问题ID查询代码执行记录
     * <p>
     * 此方法通过使用LambdaQueryWrapper构建查询条件来筛选特定的代码执行记录查询条件包括账户编号和问题ID，
     * 并且结果按照提交时间降序排列这种方法的选择是因为它能够直观地反映出查询条件和排序规则，
     * 同时利用Lambda表达式提高了代码的可读性和易维护性
     *
     * @param accountNo 账户编号，用于筛选特定账户的代码执行记录
     * @param problemId 问题ID，用于筛选特定问题的代码执行记录
     * @return 返回符合条件的代码执行记录列表如果无符合条件的记录，返回空列表
     */
    @Override
    public List<CodeExecutionRecordDO> findByAccountNoAndProblemId(Long accountNo, Long problemId) {
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo)
                .eq(CodeExecutionRecordDO::getProblemId, problemId)
                .orderByDesc(CodeExecutionRecordDO::getSubmissionTime);
        return codeExecutionRecordMapper.selectList(queryWrapper);
    }


    /**
     * 根据编程语言和结果数量限制查询代码执行记录
     * <p>
     * 此方法通过指定的编程语言来筛选代码执行记录，并根据提交时间降序排序
     * 最后，只返回指定数量的记录，这样可以有效地限制查询结果的大小
     *
     * @param language 查询条件，指定的编程语言
     * @param limit    查询结果限制的数量
     * @return 返回按照提交时间降序排序并按数量限制的代码执行记录列表
     */
    @Override
    public List<CodeExecutionRecordDO> findByLanguage(String language, int limit) {
        // 创建一个Lambda查询包装器，用于构建查询条件和排序
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件为编程语言等于传入的language参数，并按提交时间降序排序
        queryWrapper.eq(CodeExecutionRecordDO::getLanguage, language)
                .orderByDesc(CodeExecutionRecordDO::getSubmissionTime)
                // 添加SQL的LIMIT子句，限制查询结果的数量
                .last("LIMIT " + limit);
        // 执行查询并返回结果列表
        return codeExecutionRecordMapper.selectList(queryWrapper);
    }


    /**
     * 根据账户编号统计成功执行的代码记录数
     *
     * @param accountNo 账户编号，用于查询执行记录
     * @return 成功执行的代码记录数
     */
    @Override
    public Long countSuccessByAccountNo(Long accountNo) {
        // 创建查询条件对象，并设置查询条件：账户编号匹配且执行成功
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo)
                .eq(CodeExecutionRecordDO::getSuccess, true);
        // 执行查询并返回成功记录的数量
        return Long.valueOf(codeExecutionRecordMapper.selectCount(queryWrapper));
    }


    /**
     * 保存代码执行记录
     * <p>
     * 此方法负责将代码执行记录对象（CodeExecutionRecordDO）插入到数据库中
     * 它覆盖了父类或接口的save方法，以实现特定的插入逻辑
     *
     * @param recordDO 代码执行记录对象，包含需要保存的记录信息
     * @return 插入操作的结果，通常是一个表示受影响行数的整数
     */
    @Override
    public int save(CodeExecutionRecordDO recordDO) {
        // 使用mapper接口的insert方法将记录对象插入数据库
        return codeExecutionRecordMapper.insert(recordDO);
    }


    /**
     * 根据ID更新代码执行记录
     *
     * @param recordDO 包含更新信息的代码执行记录对象
     * @return 影响的行数，通常为1如果更新成功，否则为0
     */
    @Override
    public int updateById(CodeExecutionRecordDO recordDO) {
        return codeExecutionRecordMapper.updateById(recordDO);
    }


    /**
     * 根据ID删除代码执行记录
     *
     * @param id 要删除的代码执行记录的ID
     * @return 删除操作的结果，通常表示受影响的行数
     */
    @Override
    public int deleteById(Long id) {
        return codeExecutionRecordMapper.deleteById(id);
    }


    /**
     * 根据账户编号查找最近的代码执行记录
     * <p>
     * 此方法旨在从数据库中检索特定账户最近的代码执行记录，按照提交时间降序排列，并根据指定的限制返回结果
     *
     * @param accountNo 账户编号，用于标识特定的账户
     * @param limit     结果限制，指定返回的记录数量上限
     * @return 返回一个CodeExecutionRecordDO对象列表，包含最近的代码执行记录
     */
    @Override
    public List<CodeExecutionRecordDO> findRecentByAccountNo(Long accountNo, int limit) {
        // 创建一个Lambda查询包装器，用于构建查询条件和排序
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件为账户编号等于传入的accountNo，并按照提交时间降序排序
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo)
                .orderByDesc(CodeExecutionRecordDO::getSubmissionTime)
                // 添加SQL的LIMIT子句，限制返回的记录数量
                .last("LIMIT " + limit);
        // 使用构建好的查询条件，调用Mapper的selectList方法，返回查询结果列表
        return codeExecutionRecordMapper.selectList(queryWrapper);
    }


    /**
     * 根据账户编号统计代码执行记录的数量
     *
     * @param accountNo 账户编号，用于查询特定账户的代码执行记录数量
     * @return 返回代码执行记录的数量如果查询结果为0，则返回0
     */
    @Override
    public Long countByAccountNo(Long accountNo) {
        // 创建LambdaQueryWrapper对象，用于构建查询条件
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件为账户编号等于传入的accountNo参数
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo);
        // 调用mapper的selectCount方法，统计满足查询条件的代码执行记录数量，并将结果转换为Long类型返回
        return Long.valueOf(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

}
