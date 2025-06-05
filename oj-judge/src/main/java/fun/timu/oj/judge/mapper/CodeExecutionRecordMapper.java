package fun.timu.oj.judge.mapper;

import fun.timu.oj.judge.model.DO.CodeExecutionRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author zhengke
 * @description 针对表【code_execution_record(代码执行记录表(优化版))】的数据库操作Mapper
 * @createDate 2025-05-30 18:41:57
 * @Entity generator.domain.CodeExecutionRecord
 */
@Mapper
public interface CodeExecutionRecordMapper extends BaseMapper<CodeExecutionRecordDO> {

    /**
     * 获取语言使用统计
     *
     * @return 语言统计列表
     */
    List<HashMap<String, Object>> selectLanguageStatistics();

    /**
     * 获取执行状态统计
     *
     * @return 执行状态统计列表
     */
    List<HashMap<String, Object>> selectExecutionStatusStatistics();

    /**
     * 获取热门问题统计（按执行次数排序）
     *
     * @param limit 限制数量
     * @return 热门问题统计列表
     */
    List<HashMap<String, Object>> selectPopularProblems(@Param("limit") int limit);

    /**
     * 获取活跃用户统计（按执行次数排序）
     *
     * @param limit 限制数量
     * @return 活跃用户统计列表
     */
    List<HashMap<String, Object>> selectActiveUsers(@Param("limit") int limit);

    /**
     * 批量插入代码执行记录
     *
     * @param records 记录列表
     * @return 插入的行数
     */
    int batchInsert(@Param("records") List<CodeExecutionRecordDO> records);
}




