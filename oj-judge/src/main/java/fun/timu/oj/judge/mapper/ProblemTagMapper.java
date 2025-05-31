package fun.timu.oj.judge.mapper;

import fun.timu.oj.common.enmus.TagCategoryEnum;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.timu.oj.judge.model.DTO.CategoryAggregateStatisticsDTO;
import fun.timu.oj.judge.model.DTO.TagUsageStatisticsDTO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author zhengke
 * @description 针对表【problem_tag(题目标签表)】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.ProblemTag
 */
@Mapper
public interface ProblemTagMapper extends BaseMapper<ProblemTagDO> {

    /**
     * 批量增加标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param increment 增加的数量
     * @return 受影响的行数
     */
    int batchIncrementUsageCount(@Param("tagIds") List<Long> tagIds, @Param("increment") int increment);

    /**
     * 批量减少标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param decrement 减少的数量
     * @return 受影响的行数
     */
    int batchDecrementUsageCount(@Param("tagIds") List<Long> tagIds, @Param("decrement") int decrement);

    /**
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @param category      标签分类（可选）
     * @return 标签列表
     */
    List<ProblemTagDO> findByUsageCountRange(@Param("minUsageCount") Long minUsageCount, @Param("maxUsageCount") Long maxUsageCount, @Param("category") String category);

    /**
     * @param category 标签分类（可选）
     * @return 标签使用统计信息列表
     */
    List<TagUsageStatisticsDTO> getTagUsageStatistics(@Param("category") String category);

    /**
     * @return 每个分类的标签总数和使用情况统计
     */
    List<CategoryAggregateStatisticsDTO> getCategoryAggregateStatistics();

    /**
     * 批量更新标签状态
     *
     * @param tagIds 标签ID列表
     * @param status 新状态
     * @return 受影响的行数
     */
    int batchUpdateStatus(@Param("tagIds") List<Long> tagIds, @Param("status") Integer status);

    /**
     * @param limit    限制数量
     * @param category 标签分类（可选）
     * @return 热门标签列表
     */
    List<ProblemTagDO> findPopularTags(@Param("limit") int limit, @Param("category") String category);



    /**
     * 使用悲观锁锁定指定标签记录
     *
     * @param tagIds 标签ID列表
     * @return 锁定的标签记录列表
     */
    List<ProblemTagDO> lockTagsForUpdate(List<Long> tagIds);
}




