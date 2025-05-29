package fun.timu.oj.shandbox.docker.pool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 长期运行容器管理器
 * 为每种编程语言维护一个长期运行的容器，通过文件系统隔离实现代码执行复用
 */
public class LongRunningContainerManager {
    private static final Logger logger = Logger.getLogger(LongRunningContainerManager.class.getName());

    // 单例实例
    private static volatile LongRunningContainerManager instance;
    private static final Object lock = new Object();

    // Docker客户端
    private final DockerClient dockerClient;

    // 存储每种语言对应的容器信息
    private final Map<String, ContainerInfo> languageContainers = new ConcurrentHashMap<>();

    // 管理器是否已关闭
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    // 容器配置常量
    private static final String WORK_DIR = "/code";
    private static final int MEMORY_LIMIT = 512 * 1024 * 1024; // 512MB，比原来增加一些
    private static final int CPU_LIMIT = 1;
    private static final int CONTAINER_CHECK_INTERVAL = 30; // 秒

    /**
     * 容器信息内部类
     */
    public static class ContainerInfo {
        private final String containerId;
        private final String dockerImage;
        private final Path hostWorkDir;
        private volatile long lastUsedTime;
        private final AtomicBoolean healthy = new AtomicBoolean(true);

        public ContainerInfo(String containerId, String dockerImage, Path hostWorkDir) {
            this.containerId = containerId;
            this.dockerImage = dockerImage;
            this.hostWorkDir = hostWorkDir;
            this.lastUsedTime = System.currentTimeMillis();
        }

        // Getters
        public String getContainerId() {
            return containerId;
        }

        public String getDockerImage() {
            return dockerImage;
        }

        public Path getHostWorkDir() {
            return hostWorkDir;
        }

        public long getLastUsedTime() {
            return lastUsedTime;
        }

        public boolean isHealthy() {
            return healthy.get();
        }

        public void updateLastUsedTime() {
            this.lastUsedTime = System.currentTimeMillis();
        }

        public void markUnhealthy() {
            this.healthy.set(false);
        }

        public void markHealthy() {
            this.healthy.set(true);
        }
    }

    /**
     * 私有构造函数
     */
    private LongRunningContainerManager() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        this.dockerClient = DockerClientImpl.getInstance(config,
                new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .sslConfig(config.getSSLConfig())
                        .maxConnections(100)
                        .connectionTimeout(Duration.ofSeconds(30))
                        .responseTimeout(Duration.ofSeconds(45))
                        .build());

        // 添加JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "ContainerManager-Shutdown"));

        logger.info("长期运行容器管理器已初始化");
    }

    /**
     * 获取单例实例
     */
    public static LongRunningContainerManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new LongRunningContainerManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取或创建指定语言的容器
     *
     * @param language    编程语言标识（如 "java", "python", "javascript"）
     * @param dockerImage Docker镜像名称
     * @return 容器信息
     */
    public ContainerInfo getOrCreateContainer(String language, String dockerImage) throws Exception {
        if (shutdown.get()) {
            throw new IllegalStateException("容器管理器已关闭");
        }

        ContainerInfo containerInfo = languageContainers.get(language);

        // 检查现有容器是否健康
        if (containerInfo != null && isContainerHealthy(containerInfo)) {
            containerInfo.updateLastUsedTime();
            logger.fine("复用现有容器: " + language + " -> " + containerInfo.getContainerId());
            return containerInfo;
        }

        // 如果容器不存在或不健康，创建新容器
        synchronized (this) {
            // 双重检查
            containerInfo = languageContainers.get(language);
            if (containerInfo != null && isContainerHealthy(containerInfo)) {
                containerInfo.updateLastUsedTime();
                return containerInfo;
            }

            // 清理旧容器（如果存在）
            if (containerInfo != null) {
                cleanupContainer(containerInfo);
                languageContainers.remove(language);
            }

            // 创建新容器
            containerInfo = createNewContainer(language, dockerImage);
            languageContainers.put(language, containerInfo);

            logger.info("为语言 " + language + " 创建新的长期运行容器: " + containerInfo.getContainerId());
            return containerInfo;
        }
    }

    /**
     * 创建新的长期运行容器
     */
    private ContainerInfo createNewContainer(String language, String dockerImage) throws Exception {
        // 确保镜像存在
        ensureDockerImage(dockerImage);

        // 创建宿主机工作目录
        Path hostWorkDir = Files.createTempDirectory("container-" + language + "-");
        logger.info("为 " + language + " 创建工作目录: " + hostWorkDir);

        // 准备卷绑定
        Volume codeVolume = new Volume(WORK_DIR);
        Bind bind = new Bind(hostWorkDir.toAbsolutePath().toString(), codeVolume);

        // 配置容器
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(bind)
                .withMemory((long) MEMORY_LIMIT)
                .withCpuCount((long) CPU_LIMIT)
                .withNetworkMode("none") // 网络隔离
                .withReadonlyRootfs(false) // 允许写入临时文件
                .withTmpFs(Map.of("/tmp", "rw,noexec,nosuid,size=100m")); // 临时文件系统

        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                .withHostConfig(hostConfig)
                .withWorkingDir(WORK_DIR)
                .withCmd("tail", "-f", "/dev/null") // 保持容器运行
                .withEnv("DEBIAN_FRONTEND=noninteractive") // 避免交互式安装
                .exec();

        String containerId = container.getId();

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 等待容器启动完成
        Thread.sleep(2000);

        // 验证容器状态
        if (!isContainerRunning(containerId)) {
            throw new RuntimeException("容器启动失败: " + containerId);
        }

        logger.info("成功创建并启动长期运行容器: " + language + " -> " + containerId);

        return new ContainerInfo(containerId, dockerImage, hostWorkDir);
    }

    /**
     * 检查容器是否健康
     */
    private boolean isContainerHealthy(ContainerInfo containerInfo) {
        if (!containerInfo.isHealthy()) {
            return false;
        }

        try {
            return isContainerRunning(containerInfo.getContainerId());
        } catch (Exception e) {
            logger.warning("检查容器健康状态失败: " + containerInfo.getContainerId() + ", 错误: " + e.getMessage());
            containerInfo.markUnhealthy();
            return false;
        }
    }

    /**
     * 检查容器是否正在运行
     */
    private boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            return Boolean.TRUE.equals(response.getState().getRunning());
        } catch (Exception e) {
            logger.fine("容器 " + containerId + " 状态检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 确保Docker镜像存在
     */
    private void ensureDockerImage(String dockerImage) throws Exception {
        // 使用更精确的镜像检查方法
        if (isImageAvailable(dockerImage)) {
            logger.info("镜像已存在，跳过拉取: " + dockerImage);
            return;
        }

        try {
            // 拉取镜像
            logger.info("开始拉取Docker镜像: " + dockerImage + "，请稍候...");
            dockerClient.pullImageCmd(dockerImage)
                    .start()
                    .awaitCompletion(120, java.util.concurrent.TimeUnit.SECONDS);
            logger.info("镜像拉取成功: " + dockerImage);

            // 验证镜像已成功拉取
            if (!isImageAvailable(dockerImage)) {
                throw new RuntimeException("镜像拉取后验证失败: " + dockerImage);
            }
            logger.fine("镜像拉取验证成功: " + dockerImage);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("镜像拉取被中断: " + dockerImage, e);
        } catch (Exception e) {
            throw new RuntimeException("拉取Docker镜像失败: " + dockerImage + "，错误: " + e.getMessage(), e);
        }
    }

    /**
     * 检查镜像是否已存在于本地
     */
    public boolean isImageAvailable(String dockerImage) {
        try {
            dockerClient.inspectImageCmd(dockerImage).exec();
            return true;
        } catch (NotFoundException e) {
            return false;
        } catch (Exception e) {
            logger.warning("检查镜像可用性时发生异常: " + dockerImage + ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 重启指定语言的容器
     */
    public void restartContainer(String language) throws Exception {
        ContainerInfo containerInfo = languageContainers.get(language);
        if (containerInfo != null) {
            synchronized (this) {
                logger.info("重启容器: " + language + " -> " + containerInfo.getContainerId());
                cleanupContainer(containerInfo);
                languageContainers.remove(language);

                // 重新创建容器将在下次调用 getOrCreateContainer 时进行
            }
        }
    }

    /**
     * 获取容器数量统计
     */
    public int getContainerCount() {
        return languageContainers.size();
    }

    /**
     * 获取所有容器信息
     */
    public Map<String, String> getContainerStatus() {
        Map<String, String> status = new ConcurrentHashMap<>();
        languageContainers.forEach((language, info) -> {
            boolean running = isContainerRunning(info.getContainerId());
            status.put(language, String.format("容器ID: %s, 状态: %s, 最后使用: %d秒前",
                    info.getContainerId().substring(0, 12),
                    running ? "运行中" : "已停止",
                    (System.currentTimeMillis() - info.getLastUsedTime()) / 1000));
        });
        return status;
    }

    /**
     * 将代码文件复制到容器的工作目录
     *
     * @param containerInfo   容器信息
     * @param sourceDirectory 源代码目录
     */
    public void copyCodeToContainer(ContainerInfo containerInfo, Path sourceDirectory) throws IOException {
        Path hostWorkDir = containerInfo.getHostWorkDir();

        // 首先清理目标目录
        if (Files.exists(hostWorkDir)) {
            Files.walk(hostWorkDir)
                    .sorted((a, b) -> b.compareTo(a)) // 先删除子文件
                    .forEach(path -> {
                        try {
                            if (!path.equals(hostWorkDir)) { // 不删除工作目录本身
                                Files.deleteIfExists(path);
                            }
                        } catch (IOException e) {
                            logger.warning("删除文件失败: " + path + ", 错误: " + e.getMessage());
                        }
                    });
        } else {
            Files.createDirectories(hostWorkDir);
        }

        // 复制所有文件到容器工作目录
        if (Files.exists(sourceDirectory)) {
            Files.walk(sourceDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(sourceFile -> {
                        try {
                            Path relativePath = sourceDirectory.relativize(sourceFile);
                            Path targetFile = hostWorkDir.resolve(relativePath);

                            // 确保目标目录存在
                            Files.createDirectories(targetFile.getParent());

                            // 复制文件
                            Files.copy(sourceFile, targetFile,
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                            logger.fine("复制文件: " + sourceFile + " -> " + targetFile);
                        } catch (IOException e) {
                            logger.warning("复制文件失败: " + sourceFile + ", 错误: " + e.getMessage());
                            throw new RuntimeException("复制代码文件到容器失败", e);
                        }
                    });
        }

        logger.info("成功将代码文件复制到容器工作目录: " + hostWorkDir);
    }

    /**
     * 清理单个容器
     */
    private void cleanupContainer(ContainerInfo containerInfo) {
        try {
            String containerId = containerInfo.getContainerId();

            // 停止容器
            try {
                if (isContainerRunning(containerId)) {
                    dockerClient.stopContainerCmd(containerId).withTimeout(5).exec();
                    logger.fine("已停止容器: " + containerId);
                }
            } catch (Exception e) {
                logger.warning("停止容器失败: " + containerId + ", 错误: " + e.getMessage());
            }

            // 删除容器
            try {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                logger.fine("已删除容器: " + containerId);
            } catch (Exception e) {
                logger.warning("删除容器失败: " + containerId + ", 错误: " + e.getMessage());
            }

            // 清理工作目录
            try {
                Path workDir = containerInfo.getHostWorkDir();
                if (Files.exists(workDir)) {
                    Files.walk(workDir)
                            .sorted((a, b) -> b.compareTo(a)) // 先删除子文件
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    logger.warning("删除文件失败: " + path + ", 错误: " + e.getMessage());
                                }
                            });
                    logger.fine("已清理工作目录: " + workDir);
                }
            } catch (Exception e) {
                logger.warning("清理工作目录失败: " + e.getMessage());
            }

        } catch (Exception e) {
            logger.warning("清理容器时发生异常: " + e.getMessage());
        }
    }

    /**
     * 关闭管理器，清理所有容器
     */
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            logger.info("开始关闭长期运行容器管理器...");

            // 清理所有容器
            languageContainers.forEach((language, containerInfo) -> {
                logger.info("清理容器: " + language + " -> " + containerInfo.getContainerId());
                cleanupContainer(containerInfo);
            });
            languageContainers.clear();

            // 关闭Docker客户端
            try {
                if (dockerClient != null) {
                    dockerClient.close();
                    logger.info("Docker客户端已关闭");
                }
            } catch (Exception e) {
                logger.warning("关闭Docker客户端失败: " + e.getMessage());
            }

            logger.info("长期运行容器管理器已关闭");
        }
    }
}
