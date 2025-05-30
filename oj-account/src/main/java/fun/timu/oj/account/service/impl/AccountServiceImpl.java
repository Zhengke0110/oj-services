package fun.timu.oj.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.oj.account.controller.request.AccountLoginRequest;
import fun.timu.oj.account.controller.request.AccountRegisterRequest;
import fun.timu.oj.account.controller.request.AccountUpdateRequest;
import fun.timu.oj.account.manager.AccountManager;
import fun.timu.oj.account.model.DO.AccountDO;
import fun.timu.oj.account.model.VO.AccountVO;
import fun.timu.oj.account.service.AccountService;
import fun.timu.oj.account.mapper.AccountMapper;
import fun.timu.oj.account.service.NotifyService;
import fun.timu.oj.common.enmus.AuthTypeEnum;
import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.enmus.SendCodeEnum;
import fun.timu.oj.common.interceptor.LoginInterceptor;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.common.utils.CommonUtil;
import fun.timu.oj.common.utils.IDUtil;
import fun.timu.oj.common.utils.JWTUtil;
import fun.timu.oj.common.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author zhengke
 * @description 针对表【account(用户账户表(优化版，兼容沙箱执行记录))】的数据库操作Service实现
 * @createDate 2025-05-29 22:51:41
 */
@Slf4j
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, AccountDO> implements AccountService {
    private static final Long FREE_TRAFFIC_PRODUCT_ID = 1L;
    private final NotifyService notifyService;

    private final AccountManager accountManager;
    private final RedisTemplate<Object, Object> redisTemplate;

    public AccountServiceImpl(NotifyService notifyService, AccountManager accountManager, RedisTemplate<Object, Object> redisTemplate) {
        this.notifyService = notifyService;
        this.accountManager = accountManager;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 处理用户注册请求
     *
     * @param registerRequest 包含用户注册信息的请求对象
     * @return 返回一个JsonData对象，表示注册结果
     */
    @Override
    public JsonData register(AccountRegisterRequest registerRequest) {
        // 初始化验证码验证结果为false
        boolean checkCode = false;
        //判断验证码
        if (StringUtils.isNotBlank(registerRequest.getPhone())) {
            // 调用通知服务检查验证码是否正确
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER, registerRequest.getPhone(), registerRequest.getCode());
        }
        //验证码错误
        if (!checkCode) {
            // 如果验证码错误，返回错误信息
            return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        }

        // 检查手机号是否已存在
        List<AccountDO> existingAccounts = accountManager.findByPhone(registerRequest.getPhone());
        if (existingAccounts != null && !existingAccounts.isEmpty()) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_PHONE_EXIST);
        }

        // 创建Account对象以存储用户账户信息
        AccountDO accountDO = new AccountDO();

        // 将注册请求中的属性复制到Account对象中
        BeanUtils.copyProperties(registerRequest, accountDO);
        // 设置默认的认证级别
        accountDO.setAuth(AuthTypeEnum.DEFAULT.name());

        // 启用状态
        accountDO.setStatus(1);

        // 生成唯一的账号
        accountDO.setAccountNo(Long.valueOf(IDUtil.geneSnowFlakeID().toString()));

        // 设置密码加密所需的秘钥和盐
        accountDO.setSecret("$1$" + CommonUtil.getStringNumRandom(8));
        // 对用户输入的密码进行加密处理
        String cryptPwd = Md5Crypt.md5Crypt(registerRequest.getPwd().getBytes(), accountDO.getSecret());
        accountDO.setPwd(cryptPwd);

        // 调用账户管理器插入新的账户信息
        int rows = accountManager.insert(accountDO);
        // 记录日志，显示插入的行数和账户信息
        log.info("rows:{},注册成功:{}", rows, accountDO);

        // 执行用户注册后的初始化任务
        userRegisterInitTask(accountDO);

        // 返回注册成功的JSON数据
        return JsonData.buildSuccess();
    }


    /**
     * 处理账户登录请求
     *
     * @param request 包含手机号和密码的登录请求对象
     * @return 返回一个JsonData对象，包含登录结果或错误信息
     */
    @Override
    public JsonData login(AccountLoginRequest request) {
        // 根据手机号查询账户信息
        List<AccountDO> accountDOList = accountManager.findByPhone(request.getPhone());
        // 检查查询结果，确保只有一个账户与手机号关联
        if (accountDOList != null && accountDOList.size() == 1) {

            AccountDO accountDO = accountDOList.get(0);

            // 使用MD5加密用户输入的密码，并与数据库中的密码进行比较
            String md5Crypt = Md5Crypt.md5Crypt(request.getPwd().getBytes(), accountDO.getSecret());
            if (md5Crypt.equalsIgnoreCase(accountDO.getPwd())) {

                // 更新最近登录时间
                accountDO.setLastLoginAt(new Date());
                // 执行更新操作
                int rows = accountManager.updateInfo(accountDO);
                if (rows <= 0) {
                    log.warn("更新最近登录时间失败: {}", accountDO.getAccountNo());
                } else {
                    log.info("更新最近登录时间, 用户ID:{}，更新结果:{}", accountDO.getAccountNo(), rows > 0);
                }

                // 创建LoginUser对象，并从账户信息中复制属性
                LoginUser loginUser = LoginUser.builder().build();
                BeanUtils.copyProperties(accountDO, loginUser);

                // 生成JWT令牌并返回
                String token = JWTUtil.geneJsonWebTokne(loginUser);
                return JsonData.buildSuccess(token);
            } else {
                // 密码错误，返回错误信息
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        } else {
            // 用户未注册，返回错误信息
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }
    }

    /**
     * 获取当前登录用户的账户详细信息
     * <p>
     * 此方法首先从线程局部变量中获取当前登录用户的信息，
     * 然后通过账户管理器根据账户编号获取账户的详细数据，
     * 并将这些数据复制到一个账户信息传输对象中，
     * 最后构建并返回一个包含账户信息的成功响应Json数据
     */
    @Override
    public JsonData detail() {

        // 从线程局部变量中获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        // 通过账户管理器根据账户编号获取账户详细信息
        AccountDO accountDO = accountManager.detail(loginUser.getAccountNo());

        // 创建一个账户信息传输对象
        AccountVO accountVO = new AccountVO();

        // 将账户详细信息从账户数据对象复制到账户信息传输对象
        BeanUtils.copyProperties(accountDO, accountVO);

        // 构建并返回包含账户信息传输对象的成功响应Json数据
        return JsonData.buildSuccess(accountVO);
    }

    /**
     * 更新用户信息
     * <p>
     * 此方法用于处理用户信息的更新请求，主要执行以下操作：
     * 1. 从线程局部变量中获取当前登录用户信息
     * 2. 对获取到的用户信息进行校验，如果未登录则返回未登录错误信息
     * 3. 创建AccountDO对象，并根据请求参数设置需要更新的用户信息
     * 4. 调用账户管理器更新数据库中的用户信息
     * 5. 根据更新结果记录日志并返回相应的JSON数据
     *
     * @param request 包含用户信息更新请求的数据对象
     * @return 返回一个JsonData对象，表示更新操作的结果
     */
    @Override
    public JsonData updateInfo(AccountUpdateRequest request) {
        // 从线程局部变量中获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        // 参数校验
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        // 创建账户对象，设置账号
        AccountDO accountDO = new AccountDO();
        accountDO.setAccountNo(loginUser.getAccountNo());

        // 设置需要更新的字段
        if (StringUtils.isNotBlank(request.getHeadImg())) {
            accountDO.setHeadImg(request.getHeadImg());
        }
        if (StringUtils.isNotBlank(request.getNickname())) {
            accountDO.setNickname(request.getNickname());
        }

        // 调用账户管理器更新账户信息
        int rows = accountManager.updateInfo(accountDO);

        // 记录日志
        log.info("更新用户信息，用户ID:{}，更新结果:{}", loginUser.getAccountNo(), rows > 0);

        // 根据更新结果返回相应的JSON数据
        return rows > 0 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.ACCOUNT_UPDATE_ERROR);
    }

    /**
     * 用户登出功能
     * 该方法通过移除线程局部变量中的用户信息，并将传入的token加入黑名单来实现用户登出
     *
     * @param token 用户登录token，用于标识用户会话
     * @return 返回一个JsonData对象，包含登出结果信息
     */
    @Override
    public JsonData logout(String token) {
        // 从线程局部变量中获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        // 如果当前用户信息为空，则表示用户未登录，返回未登录错误信息
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        try {
            // 清除线程局部变量中的用户信息，以确保后续操作不会受到当前用户信息的影响
            LoginInterceptor.threadLocal.remove();

            // 将token加入黑名单，以防止该token再次被用于认证
            if (StringUtils.isNotBlank(token)) {
                boolean added = JWTUtil.addToBlacklist(token, redisTemplate);
                if (!added) {
                    log.warn("添加token到黑名单失败，token可能已过期: {}", token);
                }
            }

            // 记录用户登出日志，包含用户ID，以便于追踪用户行为
            log.info("用户登出成功，用户ID:{}", loginUser.getAccountNo());

            // 返回成功信息，表示用户已成功登出
            return JsonData.buildSuccess("登出成功");
        } catch (Exception e) {
            // 记录用户登出异常信息，以便于问题排查
            log.error("用户登出异常：{}", e.getMessage());
            // 返回错误信息，表示用户登出失败，需要重试
            return JsonData.buildError("登出失败，请重试");
        }
    }

    private void userRegisterInitTask(AccountDO accountDO) {
        // TODO 用户注册初始化操作
    }
}




