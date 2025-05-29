package fun.timu.oj.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.oj.account.manager.AccountManager;
import fun.timu.oj.account.model.DO.Account;
import fun.timu.oj.account.service.AccountService;
import fun.timu.oj.account.mapper.AccountMapper;
import org.springframework.stereotype.Service;

/**
 * @author zhengke
 * @description 针对表【account(用户账户表(优化版，兼容沙箱执行记录))】的数据库操作Service实现
 * @createDate 2025-05-29 22:51:41
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    private final AccountManager accountManager;

    public AccountServiceImpl(AccountManager accountManager) {
        this.accountManager = accountManager;
    }
}




