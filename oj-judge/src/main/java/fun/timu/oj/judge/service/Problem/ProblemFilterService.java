package fun.timu.oj.judge.service.Problem;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.model.VO.ProblemVO;

import java.util.Date;
import java.util.List;

public interface ProblemFilterService {
    PageResult<ProblemVO> selectByDateRange(Date startDate, Date endDate, int pageNum, int pageSize);

    PageResult<ProblemVO> selectStaleProblems(int days, int pageNum, int pageSize);

    List<ProblemVO> selectBasicInfoByIds(List<Long> problemIds);

    PageResult<ProblemVO> selectByLanguage(int pageNum, int pageSize, String language);

    List<ProblemVO> selectRecentProblems(int pageNum, int pageSize, Integer limit);
}