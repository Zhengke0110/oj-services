package fun.timu.oj.judge.service.Problem;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.controller.request.ProblemUpdateRequest;
import fun.timu.oj.judge.model.VO.ProblemVO;

import java.util.List;

public interface ProblemCoreService {
    JsonData getById(Long id);

    JsonData getProblemsWithConditions(ProblemQueryRequest request);

    List<ProblemVO> getProblemsWithCurrentUser();

    Long createProblem(ProblemCreateRequest request);

    boolean updateProblem(ProblemUpdateRequest request);

    boolean deleteProblem(Long id);

    // 批量操作方法
    boolean batchUpdateStatus(List<Long> problemIds, Integer status);

    boolean batchUpdateVisibility(List<Long> problemIds, Integer visibility);

    boolean batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit);

    int batchSoftDelete(List<Long> problemIds);

    int batchRestore(List<Long> problemIds);

    boolean publishProblem(Long id);

    boolean unpublishProblem(Long id);
}