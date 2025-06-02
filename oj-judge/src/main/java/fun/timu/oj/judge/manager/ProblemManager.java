package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ProblemManager {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemDO getById(Long id);


    /**
     * 分页查询题目列表
     *
     * @param pageNum            当前页码
     * @param pageSize           每页数量
     * @param problemType        题目类型
     * @param difficulty         难度等级
     * @param status             状态
     * @param visibility         可见性
     * @param supportedLanguages 支持的语言列表
     * @param hasInput           是否有输入
     * @param MinAcceptanceRate  最小通过率
     * @param MaxAcceptanceRate  最大通过率
     */
    public IPage<ProblemDO> findTagListWithPage(int pageNum, int pageSize, String problemType, Integer difficulty, Integer status, Integer visibility, List<String> supportedLanguages, Boolean hasInput, Double MinAcceptanceRate, Double MaxAcceptanceRate);

    /**
     * 根据创建者id查询题目列表
     *
     * @param creatorId 创建者id
     * @return 题目列表
     */
    public List<ProblemDO> findByCreatorId(Long creatorId);

    /**
     * 保存题目
     *
     * @param problemDO
     * @return
     */
    public int save(ProblemDO problemDO);

    /**
     * 更新题目
     *
     * @param problemDO
     * @return
     */
    public int updateById(ProblemDO problemDO);

    /**
     * 删除题目
     *
     * @param id
     * @return
     */
    public int deleteById(Long id);

    /**
     * 检查指定标题的题目是否已存在
     *
     * @param title 题目标题
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByTitle(String title);

    /**
     * 更新题目提交统计
     *
     * @param problemId  题目ID
     * @param isAccepted 是否通过
     * @return 更新的记录数
     */
    public int updateSubmissionStats(Long problemId, LoginUser loginUser, boolean isAccepted);

    /**
     * 查询热门题目（按提交次数排序）
     *
     * @param problemType 题目类型
     * @param difficulty  题目难度分级
     * @param limit       限制返回的题目数量，默认10个
     * @return 分页结果
     */
    List<ProblemDO> selectHotProblems(String problemType, Integer difficulty, Integer limit);

    /**
     * 查询推荐题目（通过率适中的题目）
     *
     * @param minAcceptanceRate 最小通过率
     * @param maxAcceptanceRate 最大通过率
     * @param difficulty        难度限制
     * @param limit             限制数量
     * @return 分页结果
     */
    List<ProblemDO> selectRecommendedProblems(Double minAcceptanceRate, Double maxAcceptanceRate, Integer difficulty, Integer limit);


    /**
     * 获取题目统计信息
     *
     * @return 统计信息列表（包含各难度级别的题目数量等）
     */
    List<ProblemStatisticsDTO> getProblemStatistics();

    /**
     * 批量更新题目状态
     *
     * @param problemIds 题目ID列表
     * @param status     要更新的状态值
     * @return 更新的记录数
     */
    int batchUpdateStatus(List<Long> problemIds, Integer status);

    /**
     * 根据创建者查询题目数量
     *
     * @param creatorId 创建者ID
     * @return 题目数量
     */
    Long countByCreator(Long creatorId);

    /**
     * 查询最近创建的题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param limit    限制返回的题目总数（可为 null，表示无上限）
     * @return 分页结果
     */
    IPage<ProblemDO> selectRecentProblems(int pageNum, int pageSize, Integer limit);

    /**
     * 根据支持的编程语言查询题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param language 编程语言
     * @return 分页结果
     */
    IPage<ProblemDO> selectByLanguage(int pageNum, int pageSize, String language);

    /**
     * 软删除题目（批量）
     *
     * @param problemIds 题目ID列表
     * @return 删除的记录数
     */
    int batchSoftDelete(List<Long> problemIds);

    /**
     * 恢复已删除的题目（批量）
     *
     * @param problemIds 题目ID列表
     * @return 恢复的记录数
     */
    int batchRestore(List<Long> problemIds);

    /**
     * 查询题目的通过率
     *
     * @param problemId 题目ID
     * @return 通过率（小数形式，如0.6表示60%）
     */
    Double getAcceptanceRate(Long problemId);

    /**
     * 批量获取题目的基本信息
     *
     * @param problemIds 题目ID列表
     * @return 题目信息列表
     */
    List<ProblemDO> selectBasicInfoByIds(List<Long> problemIds);

    /**
     * 获取题目详细统计信息
     *
     * @return 包含各种维度统计数据的HashMap，包括题目总数、难度分布、类型分布、提交情况等
     */
    ProblemDetailStatisticsDTO getProblemDetailStatistics();

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    List<PopularProblemCategoryDTO> getPopularProblemCategories(Integer limit);

    /**
     * 根据创建时间范围查询题目
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param status    状态筛选
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页结果
     */
    IPage<ProblemDO> selectByDateRange(Date startDate, Date endDate, Integer status, int pageNum, int pageSize);

    /**
     * 查询相似题目（基于标签和难度）
     *
     * @param problemId   题目ID
     * @param difficulty  难度限制
     * @param problemType 题目类型限制
     * @param limit       返回数量限制
     * @return 相似题目列表
     */
    List<ProblemDO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit);

    /**
     * 批量更新题目可见性
     *
     * @param problemIds 题目ID列表
     * @param visibility 可见性值
     * @return 更新的记录数
     */
    int batchUpdateVisibility(List<Long> problemIds, Integer visibility);

    /**
     * 批量更新题目的时间和内存限制
     *
     * @param problemIds  需要更新的题目ID列表
     * @param timeLimit   新的时间限制值（毫秒）
     * @param memoryLimit 新的内存限制值（MB）
     * @return 成功更新的记录数
     */
    public int batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit);

    /**
     * 重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 需要重置统计数据的题目ID列表
     * @return 更新成功的记录数
     */
    public int resetProblemStats(List<Long> problemIds);


    /**
     * 查询长时间未更新的题目
     *
     * @param lastUpdateBefore 上次更新时间早于此日期的题目将被视为陈旧题目
     * @param pageNum          页码（从1开始）
     * @param pageSize         每页大小
     * @return 分页结果，包含符合条件的题目列表
     */
    public IPage<ProblemDO> selectStaleProblems(Date lastUpdateBefore, int pageNum, int pageSize);

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public IPage<ProblemDO> selectProblemsWithoutSubmissions(int pageNum, int pageSize);

}
