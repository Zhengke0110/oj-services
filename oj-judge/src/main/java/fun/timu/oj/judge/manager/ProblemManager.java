package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;

import java.util.List;

public interface ProblemManager {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemDO getById(Long id);


    /**
     * 根据类型、难度、状态分页获取题目列表
     *
     * @param pageNum     当前页码
     * @param pageSize    每页大小
     * @param problemType 题目类型
     * @param difficulty  题目难度
     * @param status      题目状态
     * @return 题目列表
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
}
