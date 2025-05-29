package fun.timu.oj.shandbox.docker.executor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 抽象Docker代码执行器
 * 实现Docker容器内执行代码的模板方法模式
 */
public abstract class AbstractDockerExecutor<T extends AbstractDockerExecutor.ExecutionResult> {
    protected final Logger logger;
    protected final String dockerImage;
    protected static final String WORK_DIR = "/code";
    protected static final int MEMORY_LIMIT = 256 * 1024 * 1024; // 256MB
    protected static final int CPU_LIMIT = 1; // 1 CPU
    protected static final int EXECUTION_TIMEOUT = 10; // 默认执行超时时间（秒）
    protected static final int CONTAINER_WAIT_TIME = 2; // 默认容器启动等待时间（秒）

    protected boolean pullImageAlways = false; // 是否每次都拉取镜像，默认为否

    protected DockerClient dockerClient;
    protected Path tempDirectory;

    // 跟踪当前执行器创建的容器ID列表
    protected List<String> createdContainers = new ArrayList<>();

    // 添加静态变量跟踪所有执行器实例
    private static final List<AbstractDockerExecutor<?>> ALL_EXECUTORS = new ArrayList<>();
    private static boolean shutdownHookAdded = false;

    // 添加线程池用于并行清理
    private static final ExecutorService CLEANUP_EXECUTOR = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

    /**
     * 构造函数，初始化Docker客户端
     *
     * @param dockerImage Docker镜像名称
     * @param loggerName  日志记录器名称
     */
    protected AbstractDockerExecutor(String dockerImage, String loggerName) {
        this.dockerImage = dockerImage;
        this.logger = Logger.getLogger(loggerName);

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        dockerClient = DockerClientImpl.getInstance(config, new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig()).maxConnections(100).connectionTimeout(Duration.ofSeconds(30)).responseTimeout(Duration.ofSeconds(45)).build());

        // 注册当前实例并确保全局关闭钩子
        registerExecutorInstance();
    }

    /**
     * 构造函数，允许配置是否每次都拉取镜像
     *
     * @param dockerImage     Docker镜像名称
     * @param loggerName      日志记录器名称
     * @param pullImageAlways 如果为true，则每次执行都会尝试拉取最新的镜像
     */
    protected AbstractDockerExecutor(String dockerImage, String loggerName, boolean pullImageAlways) {
        this(dockerImage, loggerName);
        this.pullImageAlways = pullImageAlways;
    }

    /**
     * 执行代码的模板方法
     *
     * @param code           要执行的代码
     * @param expectedOutput 预期输出
     * @param executionCount 执行次数
     * @return 执行结果
     */
    public T executeCode(String code, String expectedOutput, int executionCount) throws Exception {
        return executeCode(code, expectedOutput, executionCount, pullImageAlways);
    }

    /**
     * 执行代码的模板方法（可指定是否强制拉取镜像）
     *
     * @param code           要执行的代码
     * @param expectedOutput 预期输出
     * @param executionCount 执行次数
     * @param forcePull      是否强制拉取镜像
     * @return 执行结果
     */
    public T executeCode(String code, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        try {
            // 创建临时目录
            createTempDirectory();

            // 将代码写入文件
            String fileName = getCodeFileName();
            String filePath = tempDirectory.resolve(fileName).toString();
            writeToFile(filePath, code);

            // 执行代码文件写入后的回调
            afterCodeFileWritten(filePath);

            // 确保Docker镜像存在
            ensureDockerImage(forcePull);

            // 指标收集
            List<ExecutionMetrics> metrics = new ArrayList<>();
            boolean outputMatched = true;

            for (int i = 0; i < executionCount; i++) {
                logger.info("开始第 " + (i + 1) + "/" + executionCount + " 次执行");
                try {
                    // 在容器中执行
                    ExecutionMetrics executionMetrics = executeInContainer(fileName, expectedOutput);
                    metrics.add(executionMetrics);

                    if (!executionMetrics.isOutputMatched()) {
                        outputMatched = false;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "执行第 " + (i + 1) + " 次时发生错误: " + e.getMessage(), e);
                    metrics.add(createErrorExecutionMetrics("EXECUTION_ERROR", "执行错误: " + e.getMessage()));
                    outputMatched = false;
                }
            }

            // 计算平均指标
            T result = calculateAverageMetrics(metrics);
            result.setOutputMatched(outputMatched);
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "执行代码时发生错误", e);
            throw new Exception("执行代码时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理临时目录
            cleanupTempDirectory();

            // 注释掉：不再每次执行后清理容器，提升性能
            // cleanupAllContainers();
        }
    }

    /**
     * 使用命令行参数执行代码的模板方法
     *
     * @param code           要执行的代码
     * @param args           命令行参数
     * @param expectedOutput 预期输出
     * @param executionCount 执行次数
     * @return 执行结果
     */
    public T executeCodeWithArgs(String code, String[] args, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithArgs(code, args, expectedOutput, executionCount, pullImageAlways);
    }

    /**
     * 使用命令行参数执行代码的模板方法（可指定是否强制拉取镜像）
     *
     * @param code           要执行的代码
     * @param args           命令行参数
     * @param expectedOutput 预期输出
     * @param executionCount 执行次数
     * @param forcePull      是否强制拉取镜像
     * @return 执行结果
     */
    public T executeCodeWithArgs(String code, String[] args, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        try {
            // 创建临时目录
            createTempDirectory();

            // 将代码写入文件
            String fileName = getCodeFileName();
            String filePath = tempDirectory.resolve(fileName).toString();
            writeToFile(filePath, code);

            // 执行代码文件写入后的回调
            afterCodeFileWritten(filePath);

            // 确保Docker镜像存在
            ensureDockerImage(forcePull);

            // 指标收集
            List<ExecutionMetrics> metrics = new ArrayList<>();
            boolean outputMatched = true;

            for (int i = 0; i < executionCount; i++) {
                logger.info("开始第 " + (i + 1) + "/" + executionCount + " 次执行(带参数)");
                try {
                    // 在容器中执行（带参数）
                    ExecutionMetrics executionMetrics = executeInContainerWithArgs(fileName, args, expectedOutput);
                    metrics.add(executionMetrics);

                    if (!executionMetrics.isOutputMatched()) {
                        outputMatched = false;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "执行第 " + (i + 1) + " 次时发生错误: " + e.getMessage(), e);
                    metrics.add(createErrorExecutionMetrics("EXECUTION_ERROR", "执行错误: " + e.getMessage()));
                    outputMatched = false;
                }
            }

            // 计算平均指标
            T result = calculateAverageMetrics(metrics);
            result.setOutputMatched(outputMatched);
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "执行代码(带参数)时发生错误", e);
            throw new Exception("执行代码(带参数)时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理临时目录
            cleanupTempDirectory();

            // 注释掉：不再每次执行后清理容器，提升性能
            // cleanupAllContainers();
        }
    }

    /**
     * 通过文件读取测试用例执行代码的模板方法
     *
     * @param code            要执行的代码
     * @param testCaseContent 测试用例内容
     * @param expectedOutput  预期输出
     * @param executionCount  执行次数
     * @return 执行结果
     */
    public T executeCodeWithTestFile(String code, String testCaseContent, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithTestFile(code, testCaseContent, expectedOutput, executionCount, pullImageAlways);
    }

    /**
     * 通过文件读取测试用例执行代码的模板方法（可指定是否强制拉取镜像）
     *
     * @param code            要执行的代码
     * @param testCaseContent 测试用例内容
     * @param expectedOutput  预期输出
     * @param executionCount  执行次数
     * @param forcePull       是否强制拉取镜像
     * @return 执行结果
     */
    public T executeCodeWithTestFile(String code, String testCaseContent, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        try {
            // 创建临时目录
            createTempDirectory();

            // 将代码写入文件
            String fileName = getCodeFileName();
            String filePath = tempDirectory.resolve(fileName).toString();
            writeToFile(filePath, code);

            // 执行代码文件写入后的回调
            afterCodeFileWritten(filePath);

            // 写入测试用例文件
            String testCaseFilePath = tempDirectory.resolve("testcase.txt").toString();
            writeToFile(testCaseFilePath, testCaseContent);

            // 执行测试文件写入后的回调
            afterTestFileWritten(testCaseFilePath);

            // 确保Docker镜像存在
            ensureDockerImage(forcePull);

            // 指标收集
            List<ExecutionMetrics> metrics = new ArrayList<>();
            boolean outputMatched = true;

            for (int i = 0; i < executionCount; i++) {
                logger.info("开始第 " + (i + 1) + "/" + executionCount + " 次执行(带测试文件)");
                try {
                    // 在容器中执行（带测试文件）
                    ExecutionMetrics executionMetrics = executeInContainerWithTestFile(fileName, "testcase.txt", expectedOutput);
                    metrics.add(executionMetrics);

                    if (!executionMetrics.isOutputMatched()) {
                        outputMatched = false;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "执行第 " + (i + 1) + " 次时发生错误: " + e.getMessage(), e);
                    metrics.add(createErrorExecutionMetrics("EXECUTION_ERROR", "执行错误: " + e.getMessage()));
                    outputMatched = false;
                }
            }

            // 计算平均指标
            T result = calculateAverageMetrics(metrics);
            result.setOutputMatched(outputMatched);
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "执行代码(带测试文件)时发生错误", e);
            throw new Exception("执行代码(带测试文件)时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理临时目录
            cleanupTempDirectory();

            // 注释掉：不再每次执行后清理容器，提升性能
            // cleanupAllContainers();
        }
    }

    /**
     * 确保Docker镜像存在，不存在则拉取
     *
     * @param forcePull 是否强制拉取镜像
     */
    protected void ensureDockerImage(boolean forcePull) {
        try {
            boolean shouldPull = forcePull;

            if (!forcePull) {
                // 使用更可靠的方式检查镜像是否存在
                logger.info("检查本地是否有镜像: " + dockerImage);

                boolean imageExists = false;
                try {
                    // 通过镜像ID检查镜像是否存在，更准确
                    imageExists = !dockerClient.inspectImageCmd(dockerImage).exec().getId().isEmpty();
                    logger.info("镜像检查结果: " + (imageExists ? "存在" : "不存在"));
                } catch (Exception e) {
                    logger.info("镜像不存在或无法获取信息: " + e.getMessage());
                    imageExists = false;
                }

                shouldPull = !imageExists;
            }

            if (shouldPull) {
                logger.info("开始拉取Docker镜像: " + dockerImage);
                try {
                    // 添加超时限制，避免无限等待
                    dockerClient.pullImageCmd(dockerImage).start().awaitCompletion(60, TimeUnit.SECONDS);
                    logger.info("Docker镜像拉取完成");

                    // 验证镜像是否成功拉取
                    String imageId = dockerClient.inspectImageCmd(dockerImage).exec().getId();
                    logger.info("成功拉取镜像，ID: " + imageId);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "拉取镜像失败: " + e.getMessage(), e);
                    throw new RuntimeException("无法拉取镜像，请检查网络连接和Docker服务: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "镜像操作发生严重异常: " + e.getMessage(), e);
            throw new RuntimeException("镜像操作失败: " + e.getMessage());
        }
    }

    /**
     * 执行命令并获取其输出
     */
    protected CompletedExecution executeCommand(String execId) throws InterruptedException {
        StringBuilder output = new StringBuilder();
        final int[] exitCode = {-1};

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                if (frame != null && frame.getPayload() != null) {
                    output.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "命令执行过程中出错: " + throwable.getMessage());
            }
        };

        try {
            dockerClient.execStartCmd(execId).exec(callback).awaitCompletion(EXECUTION_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.log(Level.WARNING, "命令执行超时或失败: " + e.getMessage());
            output.append("\n执行超时或被中断。");
        }

        try {
            exitCode[0] = dockerClient.inspectExecCmd(execId).exec().getExitCode();
        } catch (Exception e) {
            logger.log(Level.WARNING, "获取退出代码失败: " + e.getMessage());
        }

        // 如果exitCode仍为-1，命令可能已超时
        if (exitCode[0] == -1) {
            output.append("\n操作在时间限制内未正常完成。");
        }

        return new CompletedExecution(exitCode[0], output.toString());
    }

    /**
     * 创建临时目录
     */
    protected void createTempDirectory() throws IOException {
        tempDirectory = Files.createTempDirectory(getTempDirPrefix());
        logger.info("创建临时目录: " + tempDirectory);
    }

    /**
     * 将内容写入文件
     */
    protected void writeToFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
        logger.info("代码已写入文件: " + filePath);
    }

    /**
     * 清理临时目录
     */
    protected void cleanupTempDirectory() {
        if (tempDirectory != null) {
            try {
                Files.walk(tempDirectory).sorted((a, b) -> b.compareTo(a)) // 反序以先删除子文件
                        .map(Path::toFile).forEach(file -> {
                            if (!file.delete()) {
                                logger.warning("删除文件失败: " + file);
                            }
                        });
                Files.deleteIfExists(tempDirectory);
                logger.info("清理临时目录: " + tempDirectory);
            } catch (IOException e) {
                logger.log(Level.WARNING, "清理临时目录时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 清理单个容器
     */
    protected void cleanupContainer(String containerId) {
        if (containerId != null) {
            try {
                logger.info("清理容器: " + containerId);

                // 检查容器是否存在
                boolean containerExists = false;
                try {
                    dockerClient.inspectContainerCmd(containerId).exec();
                    containerExists = true;
                } catch (Exception e) {
                    logger.info("容器不存在或已被移除: " + containerId);
                }

                if (containerExists) {
                    // 检查容器是否在运行
                    try {
                        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
                        if (containerInfo.getState().getRunning()) {
                            logger.info("停止运行中的容器: " + containerId);
                            dockerClient.stopContainerCmd(containerId).withTimeout(2).exec();
                        } else {
                            logger.info("容器已处于停止状态: " + containerId);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "停止容器时出错，将尝试强制删除: " + e.getMessage());
                    }

                    // 不管容器状态如何，尝试强制删除
                    logger.info("删除容器: " + containerId);
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    logger.info("容器已成功删除: " + containerId);

                    // 从跟踪列表移除
                    createdContainers.remove(containerId);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "清理容器(" + containerId + ")时出错: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 并行清理所有由此执行器创建的容器
     */
    protected void cleanupAllContainers() {
        logger.info("开始并行清理所有容器，当前跟踪容器数量: " + createdContainers.size());

        if (createdContainers.isEmpty()) {
            logger.info("没有需要清理的容器");
            return;
        }

        // 复制列表避免并发修改异常
        List<String> containersToClean = new ArrayList<>(createdContainers);

        // 使用并行清理
        cleanupContainersParallel(containersToClean);
    }

    /**
     * 并行清理容器列表
     */
    private void cleanupContainersParallel(List<String> containerIds) {
        if (containerIds.isEmpty()) {
            return;
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(containerIds.size());

        logger.info("启动并行清理，容器数量: " + containerIds.size());
        long startTime = System.currentTimeMillis();

        // 为每个容器创建清理任务
        List<CompletableFuture<Void>> cleanupTasks = new ArrayList<>();

        for (String containerId : containerIds) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                try {
                    if (cleanupSingleContainer(containerId)) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    logger.warning("并行清理容器异常: " + containerId + ", 错误: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }, CLEANUP_EXECUTOR);

            cleanupTasks.add(task);
        }

        try {
            // 等待所有清理任务完成，最多等待60秒
            boolean completed = latch.await(60, TimeUnit.SECONDS);

            if (!completed) {
                logger.warning("并行清理超时，可能还有容器未完成清理");
                // 取消未完成的任务
                cleanupTasks.forEach(task -> task.cancel(true));
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("并行清理完成 - 耗时: " + duration + "ms, 成功: " + successCount.get() + ", 失败: " + failCount.get());

        } catch (InterruptedException e) {
            logger.warning("并行清理被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 清理单个容器（线程安全版本）
     *
     * @param containerId 容器ID
     * @return 是否清理成功
     */
    private boolean cleanupSingleContainer(String containerId) {
        if (containerId == null) {
            return false;
        }

        try {
            logger.fine("开始清理容器: " + containerId);

            // 检查容器是否存在
            boolean containerExists = false;
            try {
                dockerClient.inspectContainerCmd(containerId).exec();
                containerExists = true;
            } catch (Exception e) {
                logger.fine("容器不存在或已被移除: " + containerId);
                synchronized (createdContainers) {
                    createdContainers.remove(containerId);
                }
                return true;
            }

            if (containerExists) {
                // 尝试停止容器（如果还在运行）
                try {
                    InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
                    if (containerInfo.getState().getRunning()) {
                        logger.fine("停止运行中的容器: " + containerId);
                        dockerClient.stopContainerCmd(containerId).withTimeout(3).exec();
                        Thread.sleep(500); // 短暂等待
                    }
                } catch (Exception e) {
                    logger.fine("停止容器时出错，将尝试强制删除: " + containerId);
                }

                // 移除容器
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    logger.fine("容器已成功移除: " + containerId);
                    synchronized (createdContainers) {
                        createdContainers.remove(containerId);
                    }
                    return true;
                } catch (Exception e) {
                    logger.warning("移除容器失败: " + containerId + ", 错误: " + e.getMessage());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warning("清理容器时发生异常: " + containerId + ", 错误: " + e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * 清理所有资源，包括创建的临时文件和容器
     * 应当在应用关闭时调用此方法
     */
    public void cleanup() {
        try {
            logger.info("开始清理所有Docker容器资源，当前跟踪容器数量: " + createdContainers.size());

            if (createdContainers.isEmpty()) {
                logger.info("没有需要清理的容器");
                return;
            }

            // 使用并行清理
            List<String> containersToClean = new ArrayList<>(createdContainers);
            cleanupContainersParallel(containersToClean);

            // 关闭Docker客户端
            if (dockerClient != null) {
                try {
                    dockerClient.close();
                    logger.info("Docker客户端已关闭");
                } catch (Exception e) {
                    logger.warning("关闭Docker客户端时出错: " + e.getMessage());
                }
            }

            logger.info("Docker资源清理完成");
        } catch (Exception e) {
            logger.severe("清理资源时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 注册执行器实例并添加全局关闭钩子
     */
    private synchronized void registerExecutorInstance() {
        ALL_EXECUTORS.add(this);

        // 只添加一次全局关闭钩子
        if (!shutdownHookAdded) {
            addGlobalShutdownHook();
            shutdownHookAdded = true;
        }
    }

    /**
     * 添加全局JVM关闭钩子，确保在应用意外退出时也能清理所有容器
     */
    private static void addGlobalShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("=== JVM关闭钩子被触发，开始清理所有Docker容器 ===");
            cleanupAllExecutors();
        }, "DockerExecutor-Cleanup-Thread"));
    }

    /**
     * 清理所有执行器的容器
     */
    private static void cleanupAllExecutors() {
//        System.out.println("开始并行清理 " + ALL_EXECUTORS.size() + " 个执行器的容器...");

        if (ALL_EXECUTORS.isEmpty()) {
            System.out.println("没有执行器需要清理");
            return;
        }

        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 并行清理所有执行器
        List<CompletableFuture<Void>> executorCleanupTasks = new ArrayList<>();

        for (AbstractDockerExecutor<?> executor : ALL_EXECUTORS) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                try {
//                    System.out.println("清理执行器: " + executor.getClass().getSimpleName() + ", 容器数量: " + executor.createdContainers.size());
                    executor.cleanup();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("清理执行器失败: " + executor.getClass().getSimpleName() + ", 错误: " + e.getMessage());
                    failCount.incrementAndGet();
                }
            }, CLEANUP_EXECUTOR);

            executorCleanupTasks.add(task);
        }

        try {
            // 等待所有执行器清理完成
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(executorCleanupTasks.toArray(new CompletableFuture[0]));

            // 最多等待120秒
            allTasks.get(120, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("=== 所有Docker容器清理完成 ===");
            System.out.println("清理统计 - 耗时: " + duration + "ms, 成功执行器: " + successCount.get() + ", 失败执行器: " + failCount.get());

        } catch (Exception e) {
            System.err.println("执行器并行清理异常: " + e.getMessage());
        } finally {
            // 关闭清理线程池
            shutdownCleanupExecutor();
        }
    }

    /**
     * 关闭清理线程池
     */
    private static void shutdownCleanupExecutor() {
        try {
            CLEANUP_EXECUTOR.shutdown();
            if (!CLEANUP_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("强制关闭清理线程池...");
                CLEANUP_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("关闭清理线程池被中断");
            CLEANUP_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 收集容器内存使用情况
     */
    protected AtomicLong collectContainerMemoryUsage(String containerId) {
        AtomicLong memoryUsage = new AtomicLong(0);
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicLong maxMemory = new AtomicLong(0);

            ResultCallback.Adapter<Statistics> statsCallback = new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics stats) {
                    if (stats != null && stats.getMemoryStats() != null) {
                        Long usage = stats.getMemoryStats().getUsage();
                        if (usage != null) {
                            maxMemory.set(Math.max(maxMemory.get(), usage));
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.log(Level.WARNING, "统计回调中出错", throwable);
                    latch.countDown();
                }
            };

            // 开始收集统计信息
            dockerClient.statsCmd(containerId).exec(statsCallback);

            // 等待短暂时间以收集统计信息或超时
            if (!latch.await(3, TimeUnit.SECONDS)) {
                logger.warning("统计信息收集超时");
            }

            // 使用收集到的内存值，或回退到基于进程的估计
            if (maxMemory.get() > 0) {
                memoryUsage.set(maxMemory.get());
            } else {
                // 替代方法来估计内存
                try {
                    // 从进程列表获取估计内存
                    ExecCreateCmdResponse psCmd = dockerClient.execCreateCmd(containerId).withCmd("sh", "-c", "ps -o rss= -p 1").withAttachStdout(true).exec();

                    CompletedExecution psExec = executeCommand(psCmd.getId());
                    String psOutput = psExec.getOutput().trim();

                    // 解析RSS值（以KB为单位）并转换为字节
                    try {
                        long rssKb = Long.parseLong(psOutput.trim());
                        memoryUsage.set(rssKb * 1024); // 将KB转换为字节
                    } catch (NumberFormatException nfe) {
                        logger.warning("无法解析内存使用量: " + psOutput);
                    }
                } catch (Exception e) {
                    logger.warning("获取替代内存使用量失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "获取容器统计信息时出错: " + e.getMessage());
        }
        return memoryUsage;
    }

    /**
     * 获取当前跟踪的容器数量
     *
     * @return 容器数量
     */
    public int getContainerCount() {
        return createdContainers.size();
    }

    /**
     * 获取当前跟踪的容器ID列表（只读）
     *
     * @return 容器ID列表的副本
     */
    public List<String> getContainerIds() {
        return new ArrayList<>(createdContainers);
    }

    // 需要子类实现的抽象方法
    protected abstract String getCodeFileName();

    protected abstract String getTempDirPrefix();

    protected abstract void afterCodeFileWritten(String codePath) throws Exception;

    protected abstract void afterTestFileWritten(String testFilePath) throws Exception;

    protected abstract ExecutionMetrics executeInContainer(String codeFileName, String expectedOutput) throws Exception;

    protected abstract ExecutionMetrics executeInContainerWithArgs(String codeFileName, String[] args, String expectedOutput) throws Exception;

    protected abstract ExecutionMetrics executeInContainerWithTestFile(String codeFileName, String testFileName, String expectedOutput) throws Exception;

    protected abstract ExecutionMetrics createErrorExecutionMetrics(String status, String errorMessage);

    protected abstract T calculateAverageMetrics(List<ExecutionMetrics> metrics);

    /**
     * 命令执行完成的内部类
     */
    protected static class CompletedExecution {
        private final int exitCode;
        private final String output;

        public CompletedExecution(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }
    }

    /**
     * 执行指标的接口
     */
    public interface ExecutionMetrics {
        String getStatus();

        String getOutput();

        long getExecutionTime();

        long getMemoryUsed();

        boolean isOutputMatched();
    }

    /**
     * 执行结果的抽象类
     */
    public static abstract class ExecutionResult {
        protected boolean success;
        protected boolean outputMatched;
        protected List<? extends ExecutionMetrics> executionResults;
        protected long averageExecutionTime;
        protected long averageMemoryUsed;
        protected long maxExecutionTime;
        protected long maxMemoryUsed;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean isOutputMatched() {
            return outputMatched;
        }

        public void setOutputMatched(boolean outputMatched) {
            this.outputMatched = outputMatched;
        }

        public List<? extends ExecutionMetrics> getExecutionResults() {
            return executionResults;
        }

        public void setExecutionResults(List<? extends ExecutionMetrics> executionResults) {
            this.executionResults = executionResults;
        }

        public long getAverageExecutionTime() {
            return averageExecutionTime;
        }

        public void setAverageExecutionTime(long averageExecutionTime) {
            this.averageExecutionTime = averageExecutionTime;
        }

        public long getAverageMemoryUsed() {
            return averageMemoryUsed;
        }

        public void setAverageMemoryUsed(long averageMemoryUsed) {
            this.averageMemoryUsed = averageMemoryUsed;
        }

        public long getMaxExecutionTime() {
            return maxExecutionTime;
        }

        public void setMaxExecutionTime(long maxExecutionTime) {
            this.maxExecutionTime = maxExecutionTime;
        }

        public long getMaxMemoryUsed() {
            return maxMemoryUsed;
        }

        public void setMaxMemoryUsed(long maxMemoryUsed) {
            this.maxMemoryUsed = maxMemoryUsed;
        }
    }
}
