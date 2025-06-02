package fun.timu.oj.judge.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.judge.model.DO.ProblemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【problem(题目信息表(优化版))】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.Problem
 */
@Mapper
public interface ProblemMapper extends BaseMapper<ProblemDO> {
    /**
     * 选择热门题目
     * 该方法用于根据题目类型、难度和限制数量来选择热门题目
     * 主要用于首页或特定页面展示热门题目的功能
     *
     * @param problemType 题目类型，用于筛选题目的类型
     * @param difficulty  题目难度，用于筛选题目的难度级别
     * @param limit       限制数量，限制返回的热门题目数量
     * @return 返回一个包含热门题目的列表
     */
    List<ProblemDO> selectHotProblems(@Param("problemType") String problemType,
                                      @Param("difficulty") Integer difficulty,
                                      @Param("limit") Integer limit
    );


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
     * 批量更新题目状态
     *
     * @param problemIds 题目ID列表
     * @param status     新状态
     * @return 更新的记录数
     */
    int batchUpdateStatus(@Param("problemIds") List<Long> problemIds, @Param("status") Integer status);

    /**
     * 根据创建者查询题目数量
     *
     * @param creatorId 创建者ID
     * @return 题目数量
     */
    Long countByCreator(@Param("creatorId") Long creatorId);


}




