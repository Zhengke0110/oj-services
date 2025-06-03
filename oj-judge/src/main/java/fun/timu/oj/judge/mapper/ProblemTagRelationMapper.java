package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ProblemTagRelationMapper extends BaseMapper<ProblemTagRelationDO> {

    /**
     * 查询没有任何标签的题目ID列表
     *
     * @return 没有标签的题目ID列表
     */
    List<Long> findProblemsWithoutTags();

    /**
     * 查询没有任何题目的标签ID列表
     *
     * @return 没有题目的标签ID列表
     */
    List<Long> findTagsWithoutProblems();

    /**
     * 批量插入题目标签关联
     *
     * @param relations 关联关系列表
     * @return 插入的行数
     */
    int batchInsertRelations(@Param("relations") List<ProblemTagRelationDO> relations);

    /**
     * 根据题目ID列表查询所有相关的标签关联
     *
     * @param problemIds 题目ID列表
     * @return 标签关联列表
     */
    List<ProblemTagRelationDO> findByProblemIds(@Param("problemIds") List<Long> problemIds);

    /**
     * 根据标签ID列表查询所有相关的题目关联
     *
     * @param tagIds 标签ID列表
     * @return 题目关联列表
     */
    List<ProblemTagRelationDO> findByTagIds(@Param("tagIds") List<Long> tagIds);

    /**
     * 统计每个题目的标签数量
     *
     * @param problemIds 题目ID列表
     * @return 题目ID和标签数量的映射
     */
    List<HashMap<String, Object>> countTagsByProblemIds(@Param("problemIds") List<Long> problemIds);

    /**
     * 统计每个标签的题目数量
     *
     * @param tagIds 标签ID列表
     * @return 标签ID和题目数量的映射
     */
    List<HashMap<String, Object>> countProblemsByTagIds(@Param("tagIds") List<Long> tagIds);

}




