package fun.timu.oj.judge.service;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
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
}
