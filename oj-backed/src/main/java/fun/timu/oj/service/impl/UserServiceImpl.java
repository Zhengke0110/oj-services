package fun.timu.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import fun.timu.oj.common.ErrorCode;
import fun.timu.oj.constant.CommonConstant;
import fun.timu.oj.constant.UserConstant;
import fun.timu.oj.exception.BusinessException;
import fun.timu.oj.mapper.UserMapper;
import fun.timu.oj.modal.dto.user.UserQueryRequest;
import fun.timu.oj.modal.entity.User;
import fun.timu.oj.modal.enums.UserRoleEnum;
import fun.timu.oj.modal.vo.LoginUserVO;
import fun.timu.oj.modal.vo.UserVO;
import fun.timu.oj.service.UserService;
import fun.timu.oj.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


/**
 * @author zhengke
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-04-10 11:45:52
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    public static final String SALT = "oj";// 盐值，混淆密码
    private Lock lock = new ReentrantLock(); // 锁

    /**
     * 用户注册方法
     *
     * @param userAccount   用户账号，要求长度至少为4
     * @param userPassword  用户密码，要求长度至少为8
     * @param checkPassword 用于确认的密码，必须与userPassword相同
     * @return 注册成功后用户的ID
     * @throws BusinessException 当参数不合法、密码不匹配或账号已存在时抛出
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数是否为空或长度是否符合要求
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 检查密码和校验密码是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 使用锁确保接下来的操作是原子性的，避免并发问题
        lock.lock();
        try {
            // 检查数据库中是否已存在相同账号的用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");

            // 2. 对用户密码进行加密处理
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            // 3. 创建User对象并插入到数据库
            User user = new User();
            user.setUseraccount(userAccount);
            // 使用UUID生成唯一的用户名
            user.setUsername("普通用户" + UUID.randomUUID().toString());
            user.setUserpassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            // 返回新注册用户ID
            return user.getId();
        } finally {
            // 确保在方法结束后释放锁
            lock.unlock();
        }
    }

    /**
     * 用户登录方法
     *
     * @param userAccount  用户账号，用于查询用户信息
     * @param userPassword 用户密码，用于验证用户身份
     * @param request      HTTP请求对象，用于保存用户登录状态
     * @return 登录成功的用户信息视图对象
     * @throws BusinessException 当参数验证失败、密码错误或用户不存在时抛出业务异常
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验参数有效性
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密用户密码以进行安全验证
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 用户不存在，记录日志并抛出异常
        if (user == null) {
            logger.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 3. 记录用户的登录态（Redis）
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录的用户信息
     * <p>
     * 此方法主要用于获取当前请求的用户是否已登录，以及登录用户的详细信息
     * 它首先检查用户是否已登录，然后根据登录状态从数据库中获取用户信息
     *
     * @param request HttpServletRequest对象，用于获取当前请求的会话信息
     * @return 如果用户已登录且信息完整，则返回User对象；否则返回null或抛出异常
     * @throws BusinessException 如果用户信息不完整，则抛出此异常
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1. 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) return null;

        // 2. 检查用户信息是否完整
        if (currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户信息不完整");
        }

        // 3. 从数据库查询（追求性能的话，直接走缓存，不查数据库）
        long userId = currentUser.getId();
        return this.getById(userId);
    }


    /**
     * 判断当前用户是否为管理员
     *
     * @param request HTTP请求对象，用于获取当前会话中的用户登录状态
     * @return 如果当前用户是管理员，则返回true；否则返回false
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    /**
     * 判断用户是否为管理员
     *
     * @param user 待判断的用户对象
     * @return 如果用户是管理员，则返回true；否则返回false
     */
    @Override
    public boolean isAdmin(User user) {
        // 检查用户是否为null，以及用户角色是否为管理员
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserrole());
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 1. 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) return null;


        if (currentUser.getId() == null) {
            // 如果ID为空，同样抛出未登录异常（尽管这种情况不太可能发生）
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户信息不完整");
        }

        // 从数据库查询（追求性能的话，直接走缓存，不查数据库）
        long userId = currentUser.getId();
        return this.getById(userId);
//        return currentUser;
    }

    /**
     * 用户退出登录功能
     * <p>
     * 该方法通过检查用户登录状态并移除该状态来实现用户退出登录的功能
     *
     * @param request HTTP请求对象，用于访问会话状态
     * @return 总是返回true，表示退出登录操作执行了
     * @throws BusinessException 如果未检测到用户登录状态，抛出操作错误异常
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 检查用户是否已登录，如果未登录，则抛出异常
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null)
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");

        // 移除登录状态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 根据用户对象获取登录用户视图对象
     * <p>
     * 此方法的作用是将用户对象转换为登录用户视图对象（LoginUserVO）这主要用于在用户登录时，
     * 将用户信息以视图对象的形式展示或传递到前端界面方法首先检查输入的用户对象是否为null，
     * 如果是，则返回null，表示没有提供有效的用户信息如果用户对象不为null，则创建一个新的LoginUserVO对象，
     * 并将用户对象的属性值复制到该视图对象中，最后返回这个视图对象
     *
     * @param user 用户对象，包含用户的基本信息如果为null，表示没有提供用户信息
     * @return 登录用户视图对象如果输入为null，则返回null
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        // 检查输入的用户对象是否为null
        if (user == null) return null;

        // 创建一个新的登录用户视图对象
        LoginUserVO loginUserVO = new LoginUserVO();

        // 将用户对象的属性值复制到登录用户视图对象中
        BeanUtils.copyProperties(user, loginUserVO);

        // 返回填充了用户信息的登录用户视图对象
        return loginUserVO;
    }

    /**
     * 根据User对象获取UserVO对象
     * 此方法用于将User实体类对象转换为UserVO视图对象，便于在不同层次之间传递数据
     *
     * @param user User实体类对象，包含用户相关信息如果传入的User对象为null，则返回null
     * @return UserVO视图对象，包含与User实体类相同的信息如果输入为null，则返回null
     */
    @Override
    public UserVO getUserVO(User user) {
        // 检查输入的User对象是否为null，如果为null则直接返回null
        if (user == null) return null;

        // 创建一个新的UserVO对象
        UserVO userVO = new UserVO();
        // 使用Spring框架的BeanUtils工具类，将User对象的属性值复制到UserVO对象中
        BeanUtils.copyProperties(user, userVO);
        // 返回填充好的UserVO对象
        return userVO;
    }

    /**
     * 根据用户列表获取用户视图对象列表
     * 此方法主要用于将一系列用户对象转换为一系列用户视图对象，便于在不同层次之间传递数据
     *
     * @param userList 用户列表，不应为null
     * @return 用户视图对象列表如果输入列表为空，则返回空列表
     */
    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        // 检查输入列表是否为空，如果为空则直接返回一个新的空列表，避免后续操作中的空指针异常
        if (CollUtil.isEmpty(userList)) return new ArrayList<>();

        // 使用流处理将每个用户对象映射到用户视图对象，并收集结果到一个新的列表中
        // 这里利用了Java 8的Stream API来简化集合的处理，提高代码的可读性和效率
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 生成用户查询包装器
     * 根据用户查询请求中的条件，创建一个QueryWrapper对象，用于后续的数据库查询操作
     *
     * @param userQueryRequest 用户查询请求对象，包含查询条件和排序信息
     * @return QueryWrapper<User> 用户查询包装器，用于执行数据库查询
     * @throws BusinessException 如果请求参数为空，则抛出业务异常
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 检查请求参数是否为空，如果为空则抛出异常
        if (userQueryRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");

        // 从查询请求中获取用户ID
        Long id = userQueryRequest.getId();
        // 从查询请求中获取微信联合ID
        String unionId = userQueryRequest.getUnionId();
        // 从查询请求中获取小程序开放ID
        String mpOpenId = userQueryRequest.getMpOpenId();
        // 从查询请求中获取用户名
        String userName = userQueryRequest.getUserName();
        // 从查询请求中获取用户简介
        String userProfile = userQueryRequest.getUserProfile();
        // 从查询请求中获取用户角色
        String userRole = userQueryRequest.getUserRole();
        // 从查询请求中获取排序字段
        String sortField = userQueryRequest.getSortField();
        // 从查询请求中获取排序顺序
        String sortOrder = userQueryRequest.getSortOrder();

        // 创建一个新的用户查询包装器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 如果ID不为空，则添加ID的等值查询条件
        queryWrapper.eq(id != null, "id", id);
        // 如果微信联合ID非空，则添加微信联合ID的等值查询条件
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        // 如果小程序开放ID非空，则添加小程序开放ID的等值查询条件
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        // 如果用户角色非空，则添加用户角色的等值查询条件
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        // 如果用户简介非空，则添加用户简介的模糊查询条件
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        // 如果用户名非空，则添加用户名的模糊查询条件
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        // 如果排序字段有效且排序顺序为升序，则添加排序条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        // 返回构建好的查询包装器
        return queryWrapper;
    }
}




