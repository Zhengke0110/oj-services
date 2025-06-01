package fun.timu.oj.judge.service;

import fun.timu.oj.judge.model.VO.ProblemVO;


public interface ProblemService {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemVO getById(Long id);
}
