package fun.timu.oj.judge.manager;

import fun.timu.oj.judge.model.DO.ProblemDO;

public interface ProblemManager {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemDO getById(Long id);
}
