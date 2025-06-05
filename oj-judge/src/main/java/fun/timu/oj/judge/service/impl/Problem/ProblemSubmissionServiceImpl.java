package fun.timu.oj.judge.service.impl.Problem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.interceptor.LoginInterceptor;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.Problem.ProblemSubmissionService;
import fun.timu.oj.judge.utils.ConvertToUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 题目提交服务实现类
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSubmissionServiceImpl implements ProblemSubmissionService {

    private final ProblemManager problemManager;

    /**
     * 更新题目提交统计
     * <p>
     * 此方法用于更新题目的提交统计信息，包括总提交次数和通过次数
     * 当用户提交一个题目的解答时，无论解答是否正确，总提交次数都会增加
     * 只有当解答被接受（即正确）时，通过次数才会增加
     *
     * @param problemId  题目ID
     * @param isAccepted 提交是否被接受
     * @return 更新是否成功
     */
    @Override
    public boolean updateSubmissionStats(Long problemId, boolean isAccepted) {
        try {
            // 获取当前登录用户信息
            LoginUser loginUser = LoginInterceptor.threadLocal.get();

            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            log.info("更新题目[{}]提交统计，提交结果: {}", problemId, isAccepted ? "通过" : "未通过");

            // TODO 多表联查优化：在ProblemCoreManager中优化updateSubmissionStats()方法
            // TODO 通过复杂的多表联查验证和更新提交统计：
            // TODO 1. JOIN user 表验证用户权限和状态，确保有效用户的提交
            // TODO 2. JOIN submission 表记录本次提交的详细信息和历史统计
            // TODO 3. JOIN problem_tag_relation 和 problem_tag 表分析用户在不同算法标签上的表现
            // TODO 4. 考虑调用ProblemTagRelationManager.getTagIdsByProblemId()获取题目标签，用于用户能力分析
            // TODO 5. 为用户推荐系统提供更精准的数据支持
            // 调用manager层方法更新统计数据
            int result = problemManager.updateSubmissionStats(problemId, loginUser, isAccepted);

            if (result > 0) {
                // 更新成功
                return true;
            } else if (result == 0) {
                log.warn("更新题目[{}]提交统计失败，可能无记录被更新", problemId);
                return false;
            } else {
                log.warn("更新题目[{}]提交统计失败，题目可能不存在", problemId);
                return false;
            }
        } catch (Exception e) {
            log.error("ProblemSubmissionService--->更新题目提交统计失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ProblemVO> selectProblemsWithoutSubmissions(int pageNum, int pageSize) {
        try {
            log.info("ProblemSubmissionService--->查询零提交的题目, 页码: {}, 每页大小: {}", pageNum, pageSize);

            // TODO 多表联查优化：在ProblemCoreManager中优化selectProblemsWithoutSubmissions()方法
            // TODO 通过多表联查提供零提交题目的深入分析：
            // TODO 1. JOIN problem_tag_relation 和 problem_tag 表分析零提交题目的标签分布特征
            // TODO 2. JOIN user 表获取题目创建者信息，分析创建者活跃度与题目受欢迎程度的关系
            // TODO 3. 分析零提交题目的难度分布、类型分布等特征，为题目优化提供依据
            // TODO 4. 调用ProblemTagRelationManager.getTagNamesByProblemIds()为零提交题目添加标签信息
            // TODO 5. 为平台运营提供题目质量和用户偏好的分析数据
            // 调用manager层查询零提交题目
            IPage<ProblemDO> problemPage = problemManager.selectProblemsWithoutSubmissions(pageNum, pageSize);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemPage.getRecords().stream()
                    .map(ConvertToUtils::convertToVO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // TODO 多表联查优化：为零提交题目补充标签和创建者信息
            // TODO 调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取题目标签
            // TODO 调用UserManager.findByIds()批量获取创建者信息，分析零提交的原因

            // 构建分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(
                    problemVOList,
                    problemPage.getTotal(),
                    (int) problemPage.getSize(),
                    (int) problemPage.getCurrent(),
                    (int) problemPage.getPages()
            );

            log.info("查询零提交题目成功，总数: {}", pageResult.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("ProblemSubmissionService--->查询零提交题目失败: {}", e.getMessage(), e);
            return new PageResult<>();
        }
    }

    /**
     * 批量重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 题目ID列表
     * @return 成功重置的题目数量
     */
    @Override
    @Transactional
    public int batchResetStatistics(List<Long> problemIds) {
        try {
            // 获取当前登录用户，验证权限
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有重置题目统计数据的权限");
            }

            log.info("ProblemSubmissionService--->批量重置题目统计数据, 题目数量: {}", problemIds == null ? 0 : problemIds.size());

            // 参数校验
            if (problemIds == null || problemIds.isEmpty()) {
                throw new RuntimeException("参数错误, 题目ID列表不能为空");
            }

            // TODO 多表联查优化：在ProblemBatchManager中优化batchResetStats()方法
            // TODO 通过多表联查确保重置操作的完整性和一致性：
            // TODO 1. JOIN submission 表备份或清理相关的提交记录统计信息
            // TODO 2. JOIN problem_tag_relation 表分析重置前的题目标签统计数据，为数据恢复提供依据
            // TODO 3. JOIN user 表记录管理员操作日志，包括操作者信息和操作时间
            // TODO 4. 考虑调用ProblemTagRelationManager.getTagNamesByProblemIds()分析被重置题目的特征
            // TODO 5. 为数据审计和恢复提供完整的操作追踪记录
            // 调用manager层执行重置操作
            int resetCount = problemManager.batchResetStats(problemIds);

            log.info("批量重置题目统计数据成功, 成功重置数量: {}", resetCount);
            return resetCount;
        } catch (Exception e) {
            log.error("ProblemSubmissionService--->批量重置题目统计数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量重置题目统计数据失败", e);
        }
    }
}