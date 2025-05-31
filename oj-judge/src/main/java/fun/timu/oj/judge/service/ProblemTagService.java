package fun.timu.oj.judge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.model.VO.ProblemTagVO;

import java.util.List;


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

    /**
     * 删除标签
     */
    boolean deleteTag(Long id);

    /**
     * 根据ID获取标签
     */
    ProblemTagVO getTagById(Long id);

    /**
     * 分页查询标签
     */
    PageResult<ProblemTagVO> listTags(int current, int size, String tagName, Boolean isEnabled, String tagColor);

    /**
     * 获取所有启用的标签
     */
    List<ProblemTagVO> getAllEnabledTags();

}
