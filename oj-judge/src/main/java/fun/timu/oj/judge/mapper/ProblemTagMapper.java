package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.ProblemTagDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhengke
 * @description 针对表【problem_tag(题目标签表)】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.ProblemTag
 */
@Mapper
public interface ProblemTagMapper extends BaseMapper<ProblemTagDO> {

}




