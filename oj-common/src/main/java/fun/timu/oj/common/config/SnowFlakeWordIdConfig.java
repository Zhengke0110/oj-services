package fun.timu.oj.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Configuration
public class SnowFlakeWordIdConfig {
    /**
     * 动态指定sharding jdbc 的雪花算法中的属性work.id属性
     * 通过调用System.setProperty()的方式实现,可用容器的 id 或者机器标识位
     * workId最大值 1L << 100，就是1024，即 0<= workId < 1024
     * {@link SnowflakeShardingKeyGenerator#getWorkerId()}
     *
     */
    static {
        try {
            // 获取本地IPv4地址
            InetAddress inetAddress = Inet4Address.getLocalHost();

            // 将获取到的地址转换为字符串形式
            String hostAddressIp = inetAddress.getHostAddress();

            // 计算workId：取IP地址的哈希值的绝对值模1024，确保workId在有效范围内
            String workId = Math.abs(hostAddressIp.hashCode()) % 1024 + "";

            // 设置系统属性workId，供Sharding Jedis使用
            System.setProperty("workId", workId);

            // 日志输出当前的workId值
            log.info("workId:{}", workId);

        } catch (UnknownHostException e) {
            // 捕获异常并打印堆栈信息
            e.printStackTrace();
        }
    }

}
