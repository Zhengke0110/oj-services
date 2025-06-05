package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.CodeExecutionRecordManager;
import fun.timu.oj.judge.mapper.CodeExecutionRecordMapper;
import fun.timu.oj.judge.model.DO.CodeExecutionRecordDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代码执行记录管理器实现类
 * 基于MyBatis-Plus实现基础CRUD操作和业务逻辑
 *
 * @author zhengke
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeExecutionRecordManagerImpl implements CodeExecutionRecordManager {

    private final CodeExecutionRecordMapper codeExecutionRecordMapper;

    // ================== 参数校验工具方法 ==================

    /**
     * 校验ID参数
     */
    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 校验字符串参数
     */
    private boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 校验时间戳参数
     */
    private boolean isValidTimestamp(Long timestamp) {
        return timestamp != null && timestamp > 0;
    }

    /**
     * 设置记录默认值
     */
    private void setDefaultValues(CodeExecutionRecordDO record) {
        if (record.getIsDeleted() == null) {
            record.setIsDeleted(0);
        }
        if (record.getSuccess() == null) {
            record.setSuccess(0);
        }
        if (record.getExecutionCount() == null) {
            record.setExecutionCount(1);
        }
        if (record.getActualExecutionCount() == null) {
            record.setActualExecutionCount(0);
        }
        if (record.getOutputMatched() == null) {
            record.setOutputMatched(0);
        }
        if (record.getSubmissionTime() == null) {
            record.setSubmissionTime(System.currentTimeMillis());
        }
    }

    // ================== 基础CRUD操作 ==================

    @Override
    public CodeExecutionRecordDO findById(Long id) {
        if (!isValidId(id)) {
            return null;
        }
        return codeExecutionRecordMapper.selectById(id);
    }

    @Override
    public int save(CodeExecutionRecordDO codeExecutionRecord) {
        if (codeExecutionRecord == null) {
            return 0;
        }

        // 设置默认值
        setDefaultValues(codeExecutionRecord);
        return codeExecutionRecordMapper.insert(codeExecutionRecord);
    }

    @Override
    public int updateById(CodeExecutionRecordDO codeExecutionRecord) {
        if (codeExecutionRecord == null || !isValidId(codeExecutionRecord.getId())) {
            return 0;
        }
        return codeExecutionRecordMapper.updateById(codeExecutionRecord);
    }

    @Override
    public int deleteById(Long id) {
        if (!isValidId(id)) {
            return 0;
        }

        CodeExecutionRecordDO record = new CodeExecutionRecordDO();
        record.setId(id);
        record.setIsDeleted(1);
        return codeExecutionRecordMapper.updateById(record);
    }

    // ================== 按条件查询操作 ==================

    @Override
    public CodeExecutionRecordDO findByRequestId(String requestId) {
        if (!isValidString(requestId)) {
            return null;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getRequestId, requestId).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getCreatedAt).last("LIMIT 1");

        return codeExecutionRecordMapper.selectOne(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findByAccountNo(Long accountNo) {
        if (!isValidId(accountNo)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findByProblemId(Long problemId) {
        if (!isValidId(problemId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getProblemId, problemId).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findByAccountNoAndProblemId(Long accountNo, Long problemId) {
        if (!isValidId(accountNo) || !isValidId(problemId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo).eq(CodeExecutionRecordDO::getProblemId, problemId).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findByLanguage(String language) {
        if (!isValidString(language)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getLanguage, language).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findByExecutionStatus(String executionStatus) {
        if (!isValidString(executionStatus)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getExecutionStatus, executionStatus).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findByCodeHash(String codeHash) {
        if (!isValidString(codeHash)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getCodeHash, codeHash).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findSuccessfulExecutions() {
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getSuccess, 1).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findFailedExecutions() {
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getSuccess, 0).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    // ================== 分页查询操作 ==================

    @Override
    public IPage<CodeExecutionRecordDO> findPage(Page<CodeExecutionRecordDO> page) {
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<CodeExecutionRecordDO> findPageByAccountNo(Page<CodeExecutionRecordDO> page, Long accountNo) {
        if (!isValidId(accountNo)) {
            return page;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<CodeExecutionRecordDO> findPageByProblemId(Page<CodeExecutionRecordDO> page, Long problemId) {
        if (!isValidId(problemId)) {
            return page;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getProblemId, problemId).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<CodeExecutionRecordDO> findPageByLanguage(Page<CodeExecutionRecordDO> page, String language) {
        if (!isValidString(language)) {
            return page;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getLanguage, language).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectPage(page, queryWrapper);
    }

    // ================== 批量操作 ==================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<CodeExecutionRecordDO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return 0;
        }

        // 预处理：设置默认值
        records.forEach(this::setDefaultValues);

        // 使用自定义的批量插入 SQL
        return codeExecutionRecordMapper.batchInsert(records);
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

        LambdaUpdateWrapper<CodeExecutionRecordDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CodeExecutionRecordDO::getId, validIds).set(CodeExecutionRecordDO::getIsDeleted, 1);

        return codeExecutionRecordMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteByProblemId(Long problemId) {
        if (!isValidId(problemId)) {
            return 0;
        }

        LambdaUpdateWrapper<CodeExecutionRecordDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CodeExecutionRecordDO::getProblemId, problemId).set(CodeExecutionRecordDO::getIsDeleted, 1);

        return codeExecutionRecordMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteByAccountNo(Long accountNo) {
        if (!isValidId(accountNo)) {
            return 0;
        }

        LambdaUpdateWrapper<CodeExecutionRecordDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo).set(CodeExecutionRecordDO::getIsDeleted, 1);

        return codeExecutionRecordMapper.update(null, updateWrapper);
    }

    // ================== 统计操作 ==================

    @Override
    public int countByAccountNo(Long accountNo) {
        if (!isValidId(accountNo)) {
            return 0;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return Math.toIntExact(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

    @Override
    public int countByProblemId(Long problemId) {
        if (!isValidId(problemId)) {
            return 0;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getProblemId, problemId).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return Math.toIntExact(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

    @Override
    public int countByLanguage(String language) {
        if (!isValidString(language)) {
            return 0;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getLanguage, language).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return Math.toIntExact(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

    @Override
    public int countSuccessfulExecutions() {
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getSuccess, 1).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return Math.toIntExact(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

    @Override
    public int countFailedExecutions() {
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getSuccess, 0).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return Math.toIntExact(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

    @Override
    public int countSuccessfulExecutionsByAccountNo(Long accountNo) {
        if (!isValidId(accountNo)) {
            return 0;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo).eq(CodeExecutionRecordDO::getSuccess, 1).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return Math.toIntExact(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

    @Override
    public int countSuccessfulExecutionsByProblemId(Long problemId) {
        if (!isValidId(problemId)) {
            return 0;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getProblemId, problemId).eq(CodeExecutionRecordDO::getSuccess, 1).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return Math.toIntExact(codeExecutionRecordMapper.selectCount(queryWrapper));
    }

    // ================== 业务查询操作 ==================

    @Override
    public boolean existsById(Long id) {
        if (!isValidId(id)) {
            return false;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getId, id).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return codeExecutionRecordMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByRequestId(String requestId) {
        if (!isValidString(requestId)) {
            return false;
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getRequestId, requestId).eq(CodeExecutionRecordDO::getIsDeleted, 0);

        return codeExecutionRecordMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<CodeExecutionRecordDO> findRecentExecutionsByAccountNo(Long accountNo, int limit) {
        if (!isValidId(accountNo) || limit <= 0) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getAccountNo, accountNo).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime).last("LIMIT " + limit);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findRecentExecutionsByProblemId(Long problemId, int limit) {
        if (!isValidId(problemId) || limit <= 0) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getProblemId, problemId).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime).last("LIMIT " + limit);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findLongRunningExecutions(Long minExecutionTime) {
        if (!isValidTimestamp(minExecutionTime)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(CodeExecutionRecordDO::getExecutionTime, minExecutionTime).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getExecutionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findHighMemoryExecutions(Long minMemoryUsed) {
        if (!isValidTimestamp(minMemoryUsed)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(CodeExecutionRecordDO::getMemoryUsed, minMemoryUsed).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getMemoryUsed);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<CodeExecutionRecordDO> findExecutionsByTimeRange(Long startTime, Long endTime) {
        if (!isValidTimestamp(startTime) || !isValidTimestamp(endTime) || startTime >= endTime) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(CodeExecutionRecordDO::getSubmissionTime, startTime, endTime).eq(CodeExecutionRecordDO::getIsDeleted, 0).orderByDesc(CodeExecutionRecordDO::getSubmissionTime);

        return codeExecutionRecordMapper.selectList(queryWrapper);
    }

    // ================== 统计分析操作（使用MyBatis-Plus实现简单统计） ==================

    @Override
    public HashMap<String, Object> getExecutionStatistics() {
        HashMap<String, Object> statistics = new HashMap<>();
        LambdaQueryWrapper<CodeExecutionRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodeExecutionRecordDO::getIsDeleted, 0);

        // 总执行次数
        long totalExecutions = codeExecutionRecordMapper.selectCount(queryWrapper);
        statistics.put("totalExecutions", totalExecutions);

        // 成功执行次数
        long successfulExecutions = countSuccessfulExecutions();
        statistics.put("successfulExecutions", successfulExecutions);

        // 失败执行次数
        long failedExecutions = totalExecutions - successfulExecutions;
        statistics.put("failedExecutions", failedExecutions);

        // 成功率
        double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0;
        statistics.put("successRate", Math.round(successRate * 100.0) / 100.0);

        return statistics;
    }

    @Override
    public HashMap<String, Object> getUserExecutionStatistics(Long accountNo) {
        HashMap<String, Object> statistics = new HashMap<>();
        if (!isValidId(accountNo)) {
            return statistics;
        }

        // 总执行次数
        int totalExecutions = countByAccountNo(accountNo);
        statistics.put("totalExecutions", totalExecutions);

        // 成功执行次数
        int successfulExecutions = countSuccessfulExecutionsByAccountNo(accountNo);
        statistics.put("successfulExecutions", successfulExecutions);

        // 失败执行次数
        int failedExecutions = totalExecutions - successfulExecutions;
        statistics.put("failedExecutions", failedExecutions);

        // 成功率
        double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0;
        statistics.put("successRate", Math.round(successRate * 100.0) / 100.0);

        return statistics;
    }

    @Override
    public HashMap<String, Object> getProblemExecutionStatistics(Long problemId) {
        HashMap<String, Object> statistics = new HashMap<>();
        if (!isValidId(problemId)) {
            return statistics;
        }

        // 总执行次数
        int totalExecutions = countByProblemId(problemId);
        statistics.put("totalExecutions", totalExecutions);

        // 成功执行次数
        int successfulExecutions = countSuccessfulExecutionsByProblemId(problemId);
        statistics.put("successfulExecutions", successfulExecutions);

        // 失败执行次数
        int failedExecutions = totalExecutions - successfulExecutions;
        statistics.put("failedExecutions", failedExecutions);

        // 成功率
        double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0;
        statistics.put("successRate", Math.round(successRate * 100.0) / 100.0);

        return statistics;
    }

    // 以下方法使用自定义SQL实现
    @Override
    public List<HashMap<String, Object>> getLanguageStatistics() {
        return codeExecutionRecordMapper.selectLanguageStatistics();
    }

    @Override
    public List<HashMap<String, Object>> getExecutionStatusStatistics() {
        return codeExecutionRecordMapper.selectExecutionStatusStatistics();
    }

    @Override
    public List<HashMap<String, Object>> getPopularProblems(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        return codeExecutionRecordMapper.selectPopularProblems(limit);
    }

    @Override
    public List<HashMap<String, Object>> getActiveUsers(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        return codeExecutionRecordMapper.selectActiveUsers(limit);
    }

    @Override
    public int batchInsert(List<CodeExecutionRecordDO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return 0;
        }
        return codeExecutionRecordMapper.batchInsert(records);
    }

    // ================== 时间范围统计查询 ==================

    @Override
    public HashMap<String, Object> getExecutionCountByTimeRange(long startTime, long endTime) {
        return codeExecutionRecordMapper.selectExecutionCountByTimeRange(startTime, endTime);
    }

    @Override
    public List<HashMap<String, Object>> getExecutionCountByHour() {
        return codeExecutionRecordMapper.selectExecutionCountByHour();
    }

    @Override
    public List<HashMap<String, Object>> getExecutionCountByDay() {
        return codeExecutionRecordMapper.selectExecutionCountByDay();
    }

    @Override
    public List<HashMap<String, Object>> getExecutionCountByMonth() {
        return codeExecutionRecordMapper.selectExecutionCountByMonth();
    }

    // ================== 性能分析统计 ==================

    @Override
    public HashMap<String, Object> getExecutionTimeStatistics() {
        return codeExecutionRecordMapper.selectExecutionTimeStatistics();
    }

    @Override
    public HashMap<String, Object> getMemoryUsageStatistics() {
        return codeExecutionRecordMapper.selectMemoryUsageStatistics();
    }

    @Override
    public List<HashMap<String, Object>> getPerformanceByLanguage() {
        return codeExecutionRecordMapper.selectPerformanceByLanguage();
    }

    @Override
    public List<HashMap<String, Object>> getPerformanceByProblem(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectPerformanceByProblem(limit);
    }

    // ================== 用户行为分析 ==================

    @Override
    public List<HashMap<String, Object>> getSubmissionTimeDistribution() {
        return codeExecutionRecordMapper.selectSubmissionTimeDistribution();
    }

    @Override
    public List<HashMap<String, Object>> getSuccessRateDistribution() {
        return codeExecutionRecordMapper.selectSuccessRateDistribution();
    }

    @Override
    public List<HashMap<String, Object>> getActiveTimePeriods() {
        return codeExecutionRecordMapper.selectActiveTimePeriods();
    }

    // ================== 问题难度分析 ==================

    @Override
    public List<HashMap<String, Object>> getProblemSuccessRates() {
        return codeExecutionRecordMapper.selectProblemSuccessRates();
    }

    @Override
    public List<HashMap<String, Object>> getHardestProblems(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectHardestProblems(limit);
    }

    @Override
    public List<HashMap<String, Object>> getEasiestProblems(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectEasiestProblems(limit);
    }

    // ================== 代码质量分析 ==================

    @Override
    public List<HashMap<String, Object>> getCodeReuseStatistics() {
        return codeExecutionRecordMapper.selectCodeReuseStatistics();
    }

    @Override
    public List<HashMap<String, Object>> getCommonCodePatterns(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectCommonCodePatterns(limit);
    }

    // ================== 系统性能监控 ==================

    @Override
    public List<HashMap<String, Object>> getSystemLoadStatistics() {
        return codeExecutionRecordMapper.selectSystemLoadStatistics();
    }

    @Override
    public List<HashMap<String, Object>> getContainerUsageStatistics() {
        return codeExecutionRecordMapper.selectContainerUsageStatistics();
    }

    @Override
    public List<HashMap<String, Object>> getErrorTypeStatistics() {
        return codeExecutionRecordMapper.selectErrorTypeStatistics();
    }

    // ================== 趋势分析 ==================

    @Override
    public List<HashMap<String, Object>> getUserGrowthTrend(int days) {
        if (days <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectUserGrowthTrend(days);
    }

    @Override
    public List<HashMap<String, Object>> getExecutionVolumeTrend(int days) {
        if (days <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectExecutionVolumeTrend(days);
    }

    @Override
    public List<HashMap<String, Object>> getSuccessRateTrend(int days) {
        if (days <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectSuccessRateTrend(days);
    }

    // ================== 高级分析查询 ==================

    @Override
    public List<HashMap<String, Object>> getUserAbilityAnalysis(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectUserAbilityAnalysis(limit);
    }

    @Override
    public List<HashMap<String, Object>> getLanguagePopularityTrend(int months) {
        if (months <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectLanguagePopularityTrend(months);
    }

    @Override
    public List<HashMap<String, Object>> getProblemHeatAnalysis(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectProblemHeatAnalysis(limit);
    }

    @Override
    public List<HashMap<String, Object>> getPerformanceAnomalies(long executionTimeThreshold, long memoryThreshold) {
        return codeExecutionRecordMapper.selectPerformanceAnomalies(executionTimeThreshold, memoryThreshold);
    }

    // ================== 竞赛和比赛分析 ==================

    @Override
    public List<HashMap<String, Object>> getContestAnalysis(long startTime, long endTime) {
        return codeExecutionRecordMapper.selectContestAnalysis(startTime, endTime);
    }

    @Override
    public List<HashMap<String, Object>> getLeaderboard(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectLeaderboard(limit);
    }

    @Override
    public HashMap<String, Object> getUserProgressAnalysis(String accountNo, int days) {
        if (!isValidString(accountNo) || days <= 0) {
            return new HashMap<>();
        }
        return codeExecutionRecordMapper.selectUserProgressAnalysis(accountNo, days);
    }

    // ================== 实时监控和预警 ==================

    @Override
    public HashMap<String, Object> getSystemHealthMetrics(int minutes) {
        if (minutes <= 0) {
            return new HashMap<>();
        }
        return codeExecutionRecordMapper.selectSystemHealthMetrics(minutes);
    }

    @Override
    public List<HashMap<String, Object>> getAbnormalExecutions(int minutes) {
        if (minutes <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectAbnormalExecutions(minutes);
    }

    @Override
    public List<HashMap<String, Object>> getResourceUsageAlerts() {
        return codeExecutionRecordMapper.selectResourceUsageAlerts();
    }

    // ================== 预测分析和机器学习支持 ==================

    @Override
    public HashMap<String, Object> getUserBehaviorFeatures(String accountNo) {
        if (!isValidString(accountNo)) {
            return new HashMap<>();
        }
        return codeExecutionRecordMapper.selectUserBehaviorFeatures(accountNo);
    }

    @Override
    public HashMap<String, Object> getProblemDifficultyFeatures(long problemId) {
        if (problemId <= 0) {
            return new HashMap<>();
        }
        return codeExecutionRecordMapper.selectProblemDifficultyFeatures(problemId);
    }

    @Override
    public HashMap<String, Object> getExecutionTimePredictionFeatures(String language, long problemId) {
        if (!isValidString(language) || problemId <= 0) {
            return new HashMap<>();
        }
        return codeExecutionRecordMapper.selectExecutionTimePredictionFeatures(language, problemId);
    }

    // ================== 地理和时区分析 ==================

    @Override
    public List<HashMap<String, Object>> getGeographicStatistics() {
        return codeExecutionRecordMapper.selectGeographicStatistics();
    }

    @Override
    public List<HashMap<String, Object>> getTimezoneActivityAnalysis() {
        return codeExecutionRecordMapper.selectTimezoneActivityAnalysis();
    }

    // ================== A/B测试和实验分析 ==================

    @Override
    public List<HashMap<String, Object>> getExperimentAnalysis(String experimentId) {
        if (!isValidString(experimentId)) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectExperimentAnalysis(experimentId);
    }

    @Override
    public List<HashMap<String, Object>> getFeatureUsageAnalysis() {
        return codeExecutionRecordMapper.selectFeatureUsageAnalysis();
    }

    // ================== 安全和合规分析 ==================

    @Override
    public List<HashMap<String, Object>> getSecurityRiskDetection() {
        return codeExecutionRecordMapper.selectSecurityRiskDetection();
    }

    @Override
    public List<HashMap<String, Object>> getPlagiarismDetection() {
        return codeExecutionRecordMapper.selectPlagiarismDetection();
    }

    @Override
    public HashMap<String, Object> getComplianceReport(long startTime, long endTime) {
        return codeExecutionRecordMapper.selectComplianceReport(startTime, endTime);
    }

    // ================== 高级业务智能分析 ==================

    @Override
    public List<HashMap<String, Object>> getUserRetentionAnalysis(int cohortDays) {
        if (cohortDays <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectUserRetentionAnalysis(cohortDays);
    }

    @Override
    public List<HashMap<String, Object>> getLearningPathAnalysis(String accountNo) {
        if (!isValidString(accountNo)) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectLearningPathAnalysis(accountNo);
    }

    @Override
    public List<HashMap<String, Object>> getKnowledgePointMastery(String accountNo) {
        if (!isValidString(accountNo)) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectKnowledgePointMastery(accountNo);
    }

    @Override
    public List<HashMap<String, Object>> getPersonalizationRecommendations(String accountNo, int limit) {
        if (!isValidString(accountNo) || limit <= 0) {
            return Collections.emptyList();
        }
        return codeExecutionRecordMapper.selectPersonalizedRecommendations(accountNo, limit);
    }
}
