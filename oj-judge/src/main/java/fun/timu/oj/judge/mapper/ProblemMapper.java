package fun.timu.oj.judge.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.judge.model.DO.ProblemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengke
 * @description 针对表【problem(题目信息表(优化版))】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.Problem
 */
@Mapper
public interface ProblemMapper extends BaseMapper<ProblemDO> {

    /**
     * 查询推荐题目（通过率适中的题目）
     *
     * @param minAcceptanceRate 最小通过率
     * @param maxAcceptanceRate 最大通过率
     * @param difficulty        难度限制
     * @param limit             限制数量
     * @return 分页结果
     */
    List<ProblemDO> selectRecommendedProblems(
            @Param("minAcceptanceRate") Double minAcceptanceRate,
            @Param("maxAcceptanceRate") Double maxAcceptanceRate,
            @Param("difficulty") Integer difficulty,
            @Param("limit") Integer limit
    );

    /**
     * 获取题目统计信息
     *
     * @return 统计信息列表（包含各难度级别的题目数量等）
     */
    List<Object> getProblemStatistics();


    /**
     * 获取题目详细统计信息（包含各种维度的数据）
     *
     * @return 详细统计信息Map
     */
    HashMap<String, Object> getProblemDetailStatistics();

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    List<HashMap<String, Object>> getPopularProblemCategories(@Param("limit") Integer limit);

    /**
     * 查询相似题目（基于标签和难度）
     *
     * @param problemId   题目ID
     * @param difficulty  难度限制
     * @param problemType 题目类型限制
     * @param limit       返回数量限制
     * @return 相似题目列表
     */
    List<ProblemDO> findSimilarProblems(@Param("problemId") Long problemId,
                                        @Param("difficulty") Integer difficulty,
                                        @Param("problemType") String problemType,
                                        @Param("limit") Integer limit);

}




