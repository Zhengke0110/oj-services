package fun.timu.oj.account.manager;

import fun.timu.oj.account.model.DO.AccountDO;

import java.util.List;

public interface AccountManager {
    int insert(AccountDO accountDO);

    List<AccountDO> findByPhone(String phone);

    AccountDO detail(long accountNo);

    int updateInfo(AccountDO accountDO);

}
