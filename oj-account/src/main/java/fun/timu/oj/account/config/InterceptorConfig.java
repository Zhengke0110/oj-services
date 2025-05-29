package fun.timu.oj.account.config;


import fun.timu.oj.common.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    private final RedisTemplate<Object, Object> redisTemplate;

    public InterceptorConfig(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    LoginInterceptor loginInterceptor() {
        return new LoginInterceptor(redisTemplate);
    }

    /**
     * 添加拦截器配置
     * <p>
     * 该方法用于向应用程序添加拦截器，以在请求处理之前或之后执行特定逻辑
     * 主要用于配置登录拦截器，以确保只有授权的用户可以访问受保护的API端点
     *
     * @param registry InterceptorRegistry的实例，用于注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 注册登录拦截器
        // 该拦截器用于在处理请求前检查用户是否已登录
        registry.addInterceptor(loginInterceptor())
                // 添加拦截的路径
                // 指定需要进行登录验证的API路径
                .addPathPatterns("/api/account/*/**")
                // 排除不拦截的路径
                // 列出不需要登录验证即可访问的API路径，例如注册、上传和登录等操作
                //排除不拦截
                .excludePathPatterns("/api/account/*/register", "/api/account/*/upload", "/api/account/*/login", "/api/notify/v1/captcha", "/api/notify/*/send_code", "/api/traffic/*/reduce");

    }
}
