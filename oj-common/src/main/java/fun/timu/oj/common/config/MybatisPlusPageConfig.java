package fun.timu.oj.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusPageConfig {

    /**
     * 配置MyBatis-Plus的拦截器
     * <p>
     * 该方法主要用于创建和配置MyBatis-Plus拦截器实例，以实现全局的分页功能
     *
     * @return 返回配置好的MybatisPlusInterceptor实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建MybatisPlusInterceptor实例
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 添加内置的分页拦截器，用于自动识别和处理分页逻辑
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 返回配置好的拦截器实例
        return mybatisPlusInterceptor;
    }

}