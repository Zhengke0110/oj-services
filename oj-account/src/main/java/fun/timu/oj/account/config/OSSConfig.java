package fun.timu.oj.account.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于加载阿里云OSS（对象存储服务）的相关配置
 * 该类通过@ConfigurationProperties注解指定配置前缀，自动绑定配置文件中以"aliyun.oss"开头的属性
 * 通过@Configuration注解声明这是一个配置类，可以被Spring容器管理
 */
@ConfigurationProperties(prefix = "aliyun.oss")
@Configuration
@Data
public class OSSConfig {
    // OSS服务的Endpoint（接入点），用于指定OSS服务的访问地址
    private String endpoint;
    // 访问OSS服务的密钥ID，用于身份验证
    private String accessKeyId;
    // 访问OSS服务的密钥，与accessKeyId一起用于身份验证
    private String accessKeySecret;
    // OSS服务中的存储空间名称，用于指定文件存储的具体位置
    private String bucketname;
}
