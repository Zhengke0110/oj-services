package fun.timu.oj.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfiguration {

    /**
     * 配置RedisTemplate以定义键值对的存储方式及序列化规则
     *
     * @param redisConnectionFactory Redis连接工厂，用于创建Redis连接
     * @return 配置好的RedisTemplate实例，用于操作Redis
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        // 创建RedisTemplate实例
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();

        // 设置Redis连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //配置序列化规则
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化Redis中的对象
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        // 创建并配置ObjectMapper，设置其可见性以自动检测所有属性
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 将配置好的ObjectMapper设置到序列化器中
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        //设置key-value序列化规则
        // 使用StringRedisSerializer来序列化和反序列化键
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 使用配置好的Jackson2JsonRedisSerializer来序列化和反序列化值
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);

        //设置hash-value序列化规则
        // 同样地，为哈希表的键和值设置序列化规则
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 返回配置好的RedisTemplate实例
        return redisTemplate;

    }
}
