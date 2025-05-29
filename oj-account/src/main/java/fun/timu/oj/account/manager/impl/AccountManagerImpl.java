package fun.timu.oj.account.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import fun.timu.oj.account.manager.AccountManager;
import fun.timu.oj.account.mapper.AccountMapper;
import fun.timu.oj.account.model.DO.AccountDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AccountManagerImpl implements AccountManager {
    private final AccountMapper accountMapper;

    public AccountManagerImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 插入账户信息
     * <p>
     * 该方法用于向数据库中插入一条新的账户信息它封装了对账户数据访问层（DAO）的调用，
     * 允许上层业务逻辑在需要创建新账户时，将账户信息传递给此方法，由其负责执行数据库插入操作
     *
     * @param accountDO 包含待插入账户信息的数据对象
     * @return 插入操作影响的行数，通常为1表示成功，0表示失败
     */
    @Override
    public int insert(AccountDO accountDO) {
        return accountMapper.insert(accountDO);
    }

    /**
     * 根据电话号码查询账户信息列表
     *
     * @param phone 电话号码，用于查询账户信息
     * @return 返回一个AccountDO对象列表，包含匹配指定电话号码的所有账户信息
     */
    @Override
    public List<AccountDO> findByPhone(String phone) {
        // 使用MyBatis-Plus的QueryWrapper构建查询条件，等价于SQL中的"WHERE phone = ?"
        List<AccountDO> accountDOList = accountMapper.selectList(new QueryWrapper<AccountDO>().eq("phone", phone));
        // 返回查询结果列表
        return accountDOList;
    }

    /**
     * 根据账户编号详细查询账户信息
     *
     * @param accountNo 账户编号，用于唯一标识一个账户
     * @return AccountDO对象，包含查询到的账户详细信息如果没有找到对应的账户信息，则返回null
     */
    @Override
    public AccountDO detail(long accountNo) {
        // 使用账户编号查询账户信息，确保查询条件的唯一性
        AccountDO accountDO = accountMapper.selectOne(new QueryWrapper<AccountDO>().eq("account_no", accountNo));

        // 返回查询到的账户信息
        return accountDO;
    }

    /**
     * 更新账户信息
     * <p>
     * 该方法用于根据账户编号更新账户的详细信息它首先创建一个UpdateWrapper对象来指定更新条件，
     * 即账户编号匹配，然后执行更新操作，返回受影响的行数
     *
     * @param accountDO 包含要更新的账户信息的AccountDO对象
     * @return 受更新影响的行数
     */
    @Override
    public int updateInfo(AccountDO accountDO) {
        // 使用UpdateWrapper指定更新条件和需要更新的字段
        UpdateWrapper<AccountDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("account_no", accountDO.getAccountNo());

        // 执行更新操作
        return accountMapper.update(accountDO, updateWrapper);
    }
}
