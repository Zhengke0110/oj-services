package fun.timu.oj.account.manager.impl;

import fun.timu.oj.account.manager.AccountManager;
import fun.timu.oj.account.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountManagerImpl implements AccountManager {
    private final AccountMapper accountMapper;

    public AccountManagerImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

}
