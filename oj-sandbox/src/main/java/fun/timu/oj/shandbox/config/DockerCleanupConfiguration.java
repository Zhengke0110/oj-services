package fun.timu.oj.shandbox.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Docker容器清理配置
 * 确保在Spring应用上下文关闭时清理所有Docker容器
 */
@Component
public class DockerCleanupConfiguration implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = Logger.getLogger(DockerCleanupConfiguration.class.getName());

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("=== Spring应用上下文关闭事件触发，执行Docker容器清理 ===");

        try {
            // 这里可以添加额外的清理逻辑
            // MainController的@PreDestroy方法会被自动调用
            logger.info("Spring容器清理监听器已触发");
        } catch (Exception e) {
            logger.severe("Spring容器清理监听器执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
