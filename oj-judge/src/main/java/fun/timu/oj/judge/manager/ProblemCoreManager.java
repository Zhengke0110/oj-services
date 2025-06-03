package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.model.DO.ProblemDO;

import java.util.Date;
import java.util.List;

/**
 * 题目核心管理器接口
 * 负责题目的基础CRUD操作和基本查询功能
 *
 * @author zhengke
 */
public interface ProblemCoreManager {

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
     * @return 分页结果
     */
    IPage<ProblemDO> findTagListWithPage(int pageNum, int pageSize, String problemType, Integer difficulty, Integer status, Integer visibility, List<String> supportedLanguages, Boolean hasInput, Double MinAcceptanceRate, Double MaxAcceptanceRate);

    /**
     * 根据创建者id查询题目列表
     *
     * @param creatorId 创建者id
     * @return 题目列表
     */
    List<ProblemDO> findByCreatorId(Long creatorId);

    /**
     * 保存题目
     *
     * @param problemDO 题目信息
     * @return 保存结果
     */
    int save(ProblemDO problemDO);

    /**
     * 更新题目
     *
     * @param problemDO 题目信息
     * @return 更新结果
     */
    int updateById(ProblemDO problemDO);

    /**
     * 删除题目
     *
     * @param id 题目ID
     * @return 删除结果
     */
    int deleteById(Long id);

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
     * @param loginUser  登录用户
     * @param isAccepted 是否通过
     * @return 更新的记录数
     */
    int updateSubmissionStats(Long problemId, LoginUser loginUser, boolean isAccepted);

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
     * 查询长时间未更新的题目
     *
     * @param lastUpdateBefore 上次更新时间早于此日期的题目将被视为陈旧题目
     * @param pageNum          页码（从1开始）
     * @param pageSize         每页大小
     * @return 分页结果，包含符合条件的题目列表
     */
    IPage<ProblemDO> selectStaleProblems(Date lastUpdateBefore, int pageNum, int pageSize);

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<ProblemDO> selectProblemsWithoutSubmissions(int pageNum, int pageSize);
}
