package fun.timu.oj.account.mapper;

import fun.timu.oj.account.model.DO.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhengke
 * @description 针对表【account(用户账户表(优化版，兼容沙箱执行记录))】的数据库操作Mapper
 * @createDate 2025-05-29 22:51:41
 * @Entity fun.timu.oj.account.model.DO.Account
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}




