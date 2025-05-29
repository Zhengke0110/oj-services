package fun.timu.oj.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine缓存配置
 * 用于Spring Cloud LoadBalancer缓存优化
 */
@Configuration
@EnableCaching
public class CaffeineConfig {

    /**
     * 配置Caffeine缓存管理器
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 设置最大缓存容量
                .maximumSize(256)
                // 设置写入后过期时间
                .expireAfterWrite(35, TimeUnit.SECONDS)
                // 设置记录统计信息
                .recordStats());
        return cacheManager;
    }
}
