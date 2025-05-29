package fun.timu.oj.account.service;

import fun.timu.oj.account.controller.request.AccountLoginRequest;
import fun.timu.oj.account.controller.request.AccountRegisterRequest;
import fun.timu.oj.account.controller.request.AccountUpdateRequest;
import fun.timu.oj.account.model.DO.AccountDO;
import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.oj.common.utils.JsonData;

/**
 * @author zhengke
 * @description 针对表【account(用户账户表(优化版，兼容沙箱执行记录))】的数据库操作Service
 * @createDate 2025-05-29 22:51:41
 */
public interface AccountService extends IService<AccountDO> {

    /**
     * 注册账户
     * <p>
     * 此方法用于处理账户注册请求，根据传入的注册信息创建新账户
     *
     * @param registerRequest 包含用户注册信息的请求对象，包括用户名、密码等必要信息
     * @return 返回一个JsonData对象，包含注册结果和可能的错误信息
     */
    JsonData register(AccountRegisterRequest registerRequest);

    /**
     * 登录账户
     * <p>
     * 此方法用于处理账户登录请求，验证用户身份
     *
     * @param request 包含用户登录信息的请求对象，如用户名和密码
     * @return 返回一个JsonData对象，包含登录结果和可能的错误信息
     */
    JsonData login(AccountLoginRequest request);

    /**
     * 获取当前登录用户信息
     * <p>
     * 此方法用于获取当前登录用户的信息，包括用户名、权限等
     *
     * @return 返回一个JsonData对象，包含用户详细信息和可能的错误信息
     */
    JsonData detail();

    /**
     * 更新用户信息
     * <p>
     * 此方法用于更新当前登录用户的个人信息
     *
     * @param request 包含用户更新信息的请求对象
     * @return 返回更新结果的JsonData对象
     */
    JsonData updateInfo(AccountUpdateRequest request);

    /**
     * 用户登出
     *
     * @param token 当前用户的token
     * @return 登出结果
     */
    JsonData logout(String token);

}
