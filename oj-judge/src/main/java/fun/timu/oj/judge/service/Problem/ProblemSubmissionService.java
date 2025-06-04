package fun.timu.oj.judge.service.Problem;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.model.VO.ProblemVO;

import java.util.List;

public interface ProblemSubmissionService {
    boolean updateSubmissionStats(Long problemId, boolean isAccepted);
    int batchResetStatistics(List<Long> problemIds);
    PageResult<ProblemVO> selectProblemsWithoutSubmissions(int pageNum, int pageSize);
}