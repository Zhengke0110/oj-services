package fun.timu.oj.judge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.TagCategoryEnum;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VO.TagUsageStatisticsVO;
import fun.timu.oj.judge.model.VO.CategoryAggregateStatisticsVO;

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

    /**
     * 获取标签使用统计
     */
    List<TagUsageStatisticsVO> getTagUsageStatistics(TagCategoryEnum category);

    /**
     * 获取所有分类的标签使用聚合统计信息
     *
     * @return 每个分类的标签总数和使用情况统计
     */
    List<CategoryAggregateStatisticsVO> getCategoryAggregateStatistics();

    /**
     * 根据使用次数范围查询标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @param category      标签分类（可选）
     * @return 标签列表
     */
    List<ProblemTagVO> findByUsageCountRange(Long minUsageCount, Long maxUsageCount, TagCategoryEnum category);

    /**
     * 查询热门标签
     *
     * @param limit    限制数量
     * @param category 标签分类（可选）
     * @return 热门标签列表
     */
    List<ProblemTagVO> findPopularTags(int limit, TagCategoryEnum category);
}
