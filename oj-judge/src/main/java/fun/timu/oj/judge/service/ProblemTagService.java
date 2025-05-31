package fun.timu.oj.judge.service;

import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;


/**
 * 问题标签服务接口
 */
public interface ProblemTagService {
    /**
     * 创建标签
     */
    Long createTag(ProblemTagCreateRequest request);

    /**
     * 更新标签
     */
    boolean updateTag(ProblemTagUpdateRequest request);

}
