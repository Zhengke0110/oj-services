package fun.timu.oj.judge.service.impl.Problem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.interceptor.LoginInterceptor;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.Problem.ProblemSubmissionService;
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

            // 调用manager层查询零提交题目
            IPage<ProblemDO> problemPage = problemManager.selectProblemsWithoutSubmissions(pageNum, pageSize);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemPage.getRecords().stream()
                    .map(ProblemUtils::convertToVO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

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