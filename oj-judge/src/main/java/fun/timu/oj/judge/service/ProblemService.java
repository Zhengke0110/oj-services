package fun.timu.oj.judge.service;

import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VO.ProblemVO;


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
}
