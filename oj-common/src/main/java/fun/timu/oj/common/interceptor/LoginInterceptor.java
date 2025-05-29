package fun.timu.oj.common.interceptor;


import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.common.utils.CommonUtil;
import fun.timu.oj.common.utils.JWTUtil;
import fun.timu.oj.common.utils.JsonData;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    private final RedisTemplate<Object, Object> redisTemplate;

    // 添加构造函数接收RedisTemplate
    public LoginInterceptor(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在请求处理之前进行预处理
     * 主要用于处理跨域请求和验证用户登录状态
     *
     * @param request  HttpServletRequest对象，用于获取请求信息
     * @param response HttpServletResponse对象，用于发送响应信息
     * @param handler  请求处理者对象，通常是一个处理器方法
     * @return boolean 返回值决定是否继续执行后续的请求处理方法
     * @throws Exception 如果预处理过程中发生异常，则抛出此异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理跨域预检请求
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return true;
        }

        // 尝试从请求头中获取访问令牌
        String accessToken = request.getHeader("token");
        // 如果请求头中没有令牌，尝试从请求参数中获取
        if (StringUtils.isBlank(accessToken)) {
            accessToken = request.getParameter("token");
        }

        // 如果获取到了访问令牌
        if (StringUtils.isNotBlank(accessToken)) {
            // 检查token是否在黑名单中
            if (JWTUtil.isInBlacklist(accessToken, redisTemplate)) {
                log.info("Token在黑名单中，拒绝访问");
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }

            // 验证JWT令牌的有效性
            Claims claims = JWTUtil.checkJWT(accessToken);
            if (claims == null) {
                // 如果令牌无效，返回未登录错误信息
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }

            // 从令牌中获取用户信息
            Long accountNo = Long.parseLong(claims.get("account_no").toString());
            String headImg = (String) claims.get("head_img");
            String phone = (String) claims.get("phone");
            String auth = (String) claims.get("auth");

            // 构建登录用户对象 - 移除username属性
            LoginUser loginUser = LoginUser.builder().accountNo(accountNo).headImg(headImg).phone(phone).auth(auth).build();

            // 将登录用户对象存储在ThreadLocal中，以便后续使用
            threadLocal.set(loginUser);
            return true;
        }

        // 如果没有获取到访问令牌，返回false，阻止后续操作
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }

    /**
     * 在请求完成后执行的操作
     *
     * @param request  HTTP请求对象，用于获取请求相关的信息
     * @param response HTTP响应对象，用于获取响应相关的信息
     * @param handler  处理请求的处理器对象，可以是任何对象，取决于具体的实现
     * @param ex       如果在处理请求过程中抛出了异常，则此处为该异常对象，否则为null
     * @throws Exception 如果在执行此方法过程中抛出了异常，则此处为该异常对象
     *                   <p>
     *                   此方法主要用于清理线程局部变量，确保每个请求的处理都是线程安全的
     *                   它在请求处理的所有阶段完成后被调用，无论处理过程中是否发生了异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        threadLocal.remove();
    }
}