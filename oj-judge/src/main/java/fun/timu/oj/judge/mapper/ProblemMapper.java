package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.ProblemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhengke
 * @description 针对表【problem(问题信息表(优化版))】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.Problem
 */
@Mapper
public interface ProblemMapper extends BaseMapper<ProblemDO> {

}




