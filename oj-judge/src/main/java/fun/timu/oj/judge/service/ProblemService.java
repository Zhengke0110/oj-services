package fun.timu.oj.judge.service;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.controller.request.ProblemUpdateRequest;
import fun.timu.oj.judge.model.VO.ProblemVO;

import java.util.List;


public interface ProblemService {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemVO getById(Long id);

    /**
     * 根据条件分页查询题目列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<ProblemVO> getProblemsWithConditions(ProblemQueryRequest request);

    /**
     * 获取当前用户创建的题目列表
     *
     * @return 题目列表
     */
    List<ProblemVO> getProblemsWithCurrentUser();

    /**
     * 创建题目
     *
     * @param request 创建题目请求
     * @return 创建的题目ID
     */
    Long createProblem(ProblemCreateRequest request);

    /**
     * 更新题目信息
     *
     * @param request 更新题目请求
     * @return 是否更新成功
     */
    boolean updateProblem(ProblemUpdateRequest request);

    /**
     * 删除题目
     *
     * @param id 题目ID
     * @return 是否删除成功
     */
    boolean deleteProblem(Long id);
}
