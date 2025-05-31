package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.ProblemTagDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * @param tagIds 标签ID列表
     * @param increment 增加的数量
     * @return 受影响的行数
     */
    int batchIncrementUsageCount(@Param("tagIds") List<Long> tagIds, @Param("increment") int increment);

    /**
     * 批量减少标签使用次数
     * @param tagIds 标签ID列表
     * @param decrement 减少的数量
     * @return 受影响的行数
     */
    int batchDecrementUsageCount(@Param("tagIds") List<Long> tagIds, @Param("decrement") int decrement);

    /**
     * 根据使用次数范围查询标签
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @return 标签列表
     */
    List<ProblemTagDO> findByUsageCountRange(@Param("minUsageCount") Long minUsageCount, @Param("maxUsageCount") Long maxUsageCount);

    /**
     * 获取标签使用统计信息
     * @param category 标签分类（可选）
     * @return 统计信息
     */
    List<ProblemTagDO> getTagUsageStatistics(@Param("category") String category);

    /**
     * 批量更新标签状态
     * @param tagIds 标签ID列表
     * @param status 新状态
     * @return 受影响的行数
     */
    int batchUpdateStatus(@Param("tagIds") List<Long> tagIds, @Param("status") Integer status);

    /**
     * 查询热门标签（根据使用次数排序）
     * @param limit 限制数量
     * @param category 标签分类（可选）
     * @return 热门标签列表
     */
    List<ProblemTagDO> findPopularTags(@Param("limit") int limit, @Param("category") String category);

    /**
     * 根据颜色查询标签
     * @param tagColor 标签颜色
     * @return 标签列表
     */
    List<ProblemTagDO> findByTagColor(@Param("tagColor") String tagColor);

    /**
     * 检查标签名是否存在（排除指定ID）
     * @param tagName 标签名
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean existsByTagNameExcludeId(@Param("tagName") String tagName, @Param("excludeId") Long excludeId);

}




