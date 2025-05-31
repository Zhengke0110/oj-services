package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.TestCaseDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhengke
 * @description 针对表【test_case(测试用例表(优化版))】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.TestCase
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCaseDO> {

}




