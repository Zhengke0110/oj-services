package fun.timu.oj.judge.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.judge.model.DO.ProblemDO;

import java.util.List;

public interface ProblemManager {

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemDO getById(Long id);


    /**
     * 根据类型、难度、状态分页获取题目列表
     *
     * @param current     当前页码
     * @param size        每页大小
     * @param problemType 题目类型
     * @param difficulty  题目难度
     * @param status      题目状态
     * @return 题目列表
     */
    public IPage<ProblemDO> findTagListWithPage(int current, int size, String problemType, Integer difficulty, Integer status, List<String> supportedLanguages, Boolean hasInput, Double MinAcceptanceRate, Double MaxAcceptanceRate);

}
