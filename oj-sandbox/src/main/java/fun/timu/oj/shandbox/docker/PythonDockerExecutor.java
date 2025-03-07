package fun.timu.oj.shandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.Volume;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Python代码执行器
 * 用于在Docker容器中执行Python代码并验证结果
 */
public class PythonDockerExecutor {
    private static final Logger logger = Logger.getLogger(PythonDockerExecutor.class.getName());
    private static final String DOCKER_IMAGE = "python:3.9-slim"; // 使用Python镜像
    private static final String WORK_DIR = "/code";
    private static final int MEMORY_LIMIT = 256 * 1024 * 1024; // 256MB
    private static final int CPU_LIMIT = 1; // 1 CPU
    private static final int EXECUTION_TIMEOUT = 10; // 秒

    private boolean pullImageAlways = false; // 是否每次都拉取镜像，默认为否

    private DockerClient dockerClient;
    private Path tempDirectory;

    /**
     * 构造函数，初始化Docker客户端
     */
    public PythonDockerExecutor() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        dockerClient = DockerClientImpl.getInstance(config,
                new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .sslConfig(config.getSSLConfig())
                        .maxConnections(100)
                        .connectionTimeout(Duration.ofSeconds(30))
                        .responseTimeout(Duration.ofSeconds(45))
                        .build());
    }

    /**
     * 构造函数，允许配置是否每次都拉取镜像
     * @param pullImageAlways 如果为true，则每次执行都会尝试拉取最新的镜像
     */
    public PythonDockerExecutor(boolean pullImageAlways) {
        this();
        this.pullImageAlways = pullImageAlways;
    }

    /**
     * 执行Python代码
     * @param pythonCode Python代码
     * @param expectedOutput 预期输出
     * @param executionCount 执行次数
     * @return 执行结果
     */
    public ExecutionResult executePythonCode(String pythonCode, String expectedOutput, int executionCount) throws Exception {
        return executePythonCode(pythonCode, expectedOutput, executionCount, pullImageAlways);
    }

    /**
     * 执行Python代码
     * @param pythonCode Python代码
     * @param expectedOutput 预期输出
     * @param executionCount 执行次数
     * @param forcePull 是否强制拉取镜像
     * @return 执行结果
     */
    public ExecutionResult executePythonCode(String pythonCode, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        try {
            // 创建临时目录
            createTempDirectory();

            // 将Python代码写入文件
            String pythonFileName = "solution.py";
            String pythonFilePath = tempDirectory.resolve(pythonFileName).toString();
            writeToFile(pythonFilePath, pythonCode);

            // 检查本地是否有镜像，如果没有或强制拉取，则拉取镜像
            ensureDockerImage(forcePull);

            // 指标收集
            List<ExecutionMetrics> metrics = new ArrayList<>();
            boolean outputMatched = true;

            for (int i = 0; i < executionCount; i++) {
                logger.info("开始第 " + (i+1) + "/" + executionCount + " 次执行");
                try {
                    ExecutionMetrics executionMetrics = executeInContainer(pythonFileName, expectedOutput);
                    metrics.add(executionMetrics);

                    if (!executionMetrics.isOutputMatched()) {
                        outputMatched = false;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "执行第 " + (i+1) + " 次时发生错误: " + e.getMessage(), e);
                    metrics.add(new ExecutionMetrics(
                            "EXECUTION_ERROR",
                            "执行错误: " + e.getMessage(),
                            0,
                            0,
                            false));
                    outputMatched = false;
                }
            }

            // 计算平均指标
            ExecutionResult result = calculateAverageMetrics(metrics);
            result.setOutputMatched(outputMatched);
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "执行Python代码时发生错误", e);
            throw new Exception("执行Python代码时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理临时目录
            cleanupTempDirectory();
        }
    }

    /**
     * 使用命令行参数执行Python代码
     * @param pythonCode Python代码
     * @param args 传递给脚本的参数数组
     * @param expectedOutput 期望的输出结果
     * @param executionCount 执行次数
     * @return 执行结果
     */
    public ExecutionResult executePythonCodeWithArgs(String pythonCode, String[] args, String expectedOutput, int executionCount) throws Exception {
        return executePythonCodeWithArgs(pythonCode, args, expectedOutput, executionCount, pullImageAlways);
    }

    /**
     * 使用命令行参数执行Python代码
     * @param pythonCode Python代码
     * @param args 传递给脚本的参数数组
     * @param expectedOutput 期望的输出结果
     * @param executionCount 执行次数
     * @param forcePull 是否强制拉取镜像
     * @return 执行结果
     */
    public ExecutionResult executePythonCodeWithArgs(String pythonCode, String[] args, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        try {
            // 创建临时目录
            createTempDirectory();

            // 将Python代码写入文件
            String pythonFileName = "solution.py";
            String pythonFilePath = tempDirectory.resolve(pythonFileName).toString();
            writeToFile(pythonFilePath, pythonCode);

            // 检查本地是否有镜像，如果没有或强制拉取，则拉取镜像
            ensureDockerImage(forcePull);

            // 指标收集
            List<ExecutionMetrics> metrics = new ArrayList<>();
            boolean outputMatched = true;

            for (int i = 0; i < executionCount; i++) {
                logger.info("开始第 " + (i+1) + "/" + executionCount + " 次执行(带参数)");
                try {
                    ExecutionMetrics executionMetrics = executeInContainerWithArgs(pythonFileName, args, expectedOutput);
                    metrics.add(executionMetrics);

                    if (!executionMetrics.isOutputMatched()) {
                        outputMatched = false;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "执行第 " + (i+1) + " 次时发生错误: " + e.getMessage(), e);
                    metrics.add(new ExecutionMetrics(
                            "EXECUTION_ERROR",
                            "执行错误: " + e.getMessage(),
                            0,
                            0,
                            false));
                    outputMatched = false;
                }
            }

            // 计算平均指标
            ExecutionResult result = calculateAverageMetrics(metrics);
            result.setOutputMatched(outputMatched);
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "执行Python代码(带参数)时发生错误", e);
            throw new Exception("执行Python代码(带参数)时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理临时目录
            cleanupTempDirectory();
        }
    }

    /**
     * 通过文件读取测试用例执行Python代码
     * @param pythonCode Python代码
     * @param testCaseContent 测试用例内容，将被写入文件
     * @param expectedOutput 期望的输出结果
     * @param executionCount 执行次数
     * @return 执行结果
     */
    public ExecutionResult executePythonCodeWithTestFile(String pythonCode, String testCaseContent, String expectedOutput, int executionCount) throws Exception {
        return executePythonCodeWithTestFile(pythonCode, testCaseContent, expectedOutput, executionCount, pullImageAlways);
    }

    /**
     * 通过文件读取测试用例执行Python代码
     * @param pythonCode Python代码
     * @param testCaseContent 测试用例内容，将被写入文件
     * @param expectedOutput 期望的输出结果
     * @param executionCount 执行次数
     * @param forcePull 是否强制拉取镜像
     * @return 执行结果
     */
    public ExecutionResult executePythonCodeWithTestFile(String pythonCode, String testCaseContent, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        try {
            // 创建临时目录
            createTempDirectory();

            // 将Python代码写入文件
            String pythonFileName = "solution.py";
            String pythonFilePath = tempDirectory.resolve(pythonFileName).toString();
            writeToFile(pythonFilePath, pythonCode);

            // 写入测试用例文件
            String testCaseFilePath = tempDirectory.resolve("testcase.txt").toString();
            writeToFile(testCaseFilePath, testCaseContent);

            // 检查本地是否有镜像，如果没有或强制拉取，则拉取镜像
            ensureDockerImage(forcePull);

            // 指标收集
            List<ExecutionMetrics> metrics = new ArrayList<>();
            boolean outputMatched = true;

            for (int i = 0; i < executionCount; i++) {
                logger.info("开始第 " + (i+1) + "/" + executionCount + " 次执行(带测试文件)");
                try {
                    ExecutionMetrics executionMetrics = executeInContainerWithTestFile(pythonFileName, "testcase.txt", expectedOutput);
                    metrics.add(executionMetrics);

                    if (!executionMetrics.isOutputMatched()) {
                        outputMatched = false;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "执行第 " + (i+1) + " 次时发生错误: " + e.getMessage(), e);
                    metrics.add(new ExecutionMetrics(
                            "EXECUTION_ERROR",
                            "执行错误: " + e.getMessage(),
                            0,
                            0,
                            false));
                    outputMatched = false;
                }
            }

            // 计算平均指标
            ExecutionResult result = calculateAverageMetrics(metrics);
            result.setOutputMatched(outputMatched);
            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "执行Python代码(带测试文件)时发生错误", e);
            throw new Exception("执行Python代码(带测试文件)时发生错误: " + e.getMessage(), e);
        } finally {
            // 清理临时目录
            cleanupTempDirectory();
        }
    }

    /**
     * 确保Docker镜像存在，不存在则拉取
     * @param forcePull 是否强制拉取镜像
     */
    private void ensureDockerImage(boolean forcePull) {
        try {
            boolean shouldPull = forcePull;

            if (!forcePull) {
                // 使用更可靠的方式检查镜像是否存在
                logger.info("检查本地是否有镜像: " + DOCKER_IMAGE);

                boolean imageExists = false;
                try {
                    // 通过镜像ID检查镜像是否存在，更准确
                    imageExists = !dockerClient.inspectImageCmd(DOCKER_IMAGE).exec().getId().isEmpty();
                    logger.info("镜像检查结果: " + (imageExists ? "存在" : "不存在"));
                } catch (Exception e) {
                    logger.info("镜像不存在或无法获取信息: " + e.getMessage());
                    imageExists = false;
                }

                shouldPull = !imageExists;
            }

            if (shouldPull) {
                logger.info("开始拉取Docker镜像: " + DOCKER_IMAGE);
                try {
                    // 添加超时限制，避免无限等待
                    dockerClient.pullImageCmd(DOCKER_IMAGE).start().awaitCompletion(60, TimeUnit.SECONDS);
                    logger.info("Docker镜像拉取完成");

                    // 验证镜像是否成功拉取
                    String imageId = dockerClient.inspectImageCmd(DOCKER_IMAGE).exec().getId();
                    logger.info("成功拉取镜像，ID: " + imageId);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "拉取镜像失败: " + e.getMessage(), e);
                    throw new RuntimeException("无法拉取Python镜像，请检查网络连接和Docker服务: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "镜像操作发生严重异常: " + e.getMessage(), e);
            throw new RuntimeException("镜像操作失败: " + e.getMessage());
        }
    }

    /**
     * 拉取Python镜像
     * @param forcePull 是否强制拉取
     * @deprecated 使用 ensureDockerImage 方法代替
     */
    @Deprecated
    private void pullPythonImage(boolean forcePull) {
        ensureDockerImage(forcePull);
    }

    /**
     * 检查本地是否有镜像，如果没有或配置了总是拉取，则拉取镜像
     * @deprecated 使用 ensureDockerImage 方法代替
     */
    @Deprecated
    private void checkAndPullImage() {
        ensureDockerImage(pullImageAlways);
    }

    /**
     * 在容器中执行Python代码
     */
    private ExecutionMetrics executeInContainer(String pythonFileName, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);

        try {
            // 准备卷绑定用于代码目录
            Volume codeVolume = new Volume(WORK_DIR);
            Bind bind = new Bind(tempDirectory.toAbsolutePath().toString(), codeVolume);

            // 创建容器
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withBinds(bind)
                    .withMemory((long) MEMORY_LIMIT)
                    .withCpuCount((long) CPU_LIMIT)
                    .withNetworkMode("none"); // 隔离网络

            logger.info("创建容器...");
            CreateContainerResponse container = dockerClient.createContainerCmd(DOCKER_IMAGE)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(WORK_DIR)
                    // 使用命令保持容器运行
                    .withCmd("tail", "-f", "/dev/null")
                    .exec();

            containerId = container.getId();
            logger.info("容器创建成功: " + containerId);

            // 启动容器
            logger.info("启动容器...");
            dockerClient.startContainerCmd(containerId).exec();

            // 等待一小段时间确保容器启动完成
            Thread.sleep(1000);

            // 检查容器是否在运行
            boolean isRunning = dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning();
            if (!isRunning) {
                throw new RuntimeException("容器未能成功启动，请检查Docker服务和镜像是否正常");
            }

            logger.info("容器已启动并正在运行");

            // 检查Python版本（确认环境正确）
            ExecCreateCmdResponse versionCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("python", "--version")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution versionExec = executeCommand(versionCmd.getId());
            logger.info("Python版本: " + versionExec.getOutput().trim());

            if (versionExec.getExitCode() != 0) {
                throw new RuntimeException("Python环境异常，无法获取Python版本: " + versionExec.getOutput());
            }

            // 执行Python程序
            logger.info("执行Python代码: " + pythonFileName);
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("python", WORK_DIR + "/" + pythonFileName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();
            logger.info("代码执行完成，输出: " + output);
            logger.info("退出代码: " + exec.getExitCode());

            // 获取容器统计信息以获取内存使用情况
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
                        ExecCreateCmdResponse psCmd = dockerClient.execCreateCmd(containerId)
                                .withCmd("sh", "-c", "ps -o rss= -p 1")
                                .withAttachStdout(true)
                                .exec();

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

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new ExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "容器执行过程中发生错误: " + e.getMessage(), e);
            throw e; // 重新抛出异常以便上层处理
        } finally {
            // 清理容器
            if (containerId != null) {
                try {
                    logger.info("清理容器: " + containerId);
                    dockerClient.stopContainerCmd(containerId).withTimeout(2).exec();
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    logger.info("容器已清理");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "清理容器时出错: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 在容器中执行Python代码(带命令行参数)
     */
    private ExecutionMetrics executeInContainerWithArgs(String pythonFileName, String[] args, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);

        try {
            // 准备卷绑定用于代码目录
            Volume codeVolume = new Volume(WORK_DIR);
            Bind bind = new Bind(tempDirectory.toAbsolutePath().toString(), codeVolume);

            // 创建容器
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withBinds(bind)
                    .withMemory((long) MEMORY_LIMIT)
                    .withCpuCount((long) CPU_LIMIT)
                    .withNetworkMode("none"); // 隔离网络

            logger.info("创建Docker容器(带参数)...");
            CreateContainerResponse container = dockerClient.createContainerCmd(DOCKER_IMAGE)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(WORK_DIR)
                    // 使用命令保持容器运行
                    .withCmd("tail", "-f", "/dev/null")
                    .exec();

            containerId = container.getId();
            logger.info("容器创建成功: " + containerId);

            // 启动容器
            logger.info("启动容器...");
            dockerClient.startContainerCmd(containerId).exec();

            // 等待一小段时间确保容器启动完成
            Thread.sleep(1000);

            // 检查容器是否在运行
            boolean isRunning = dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning();
            if (!isRunning) {
                throw new RuntimeException("容器未能成功启动，请检查Docker服务和镜像是否正常");
            }

            logger.info("容器已启动并正在运行");

            // 检查Python版本（确认环境正确）
            ExecCreateCmdResponse versionCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("python", "--version")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution versionExec = executeCommand(versionCmd.getId());
            logger.info("Python版本: " + versionExec.getOutput().trim());

            if (versionExec.getExitCode() != 0) {
                throw new RuntimeException("Python环境异常，无法获取Python版本: " + versionExec.getOutput());
            }

            // 构建命令行参数列表
            List<String> cmdList = new ArrayList<>();
            cmdList.add("python");
            cmdList.add(WORK_DIR + "/" + pythonFileName);

            // 添加所有参数
            if (args != null) {
                for (String arg : args) {
                    cmdList.add(arg);
                }
            }

            logger.info("执行命令: " + String.join(" ", cmdList));

            // 执行Python程序(带参数)
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdList.toArray(new String[0]))
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();
            logger.info("代码执行完成，输出: " + output);
            logger.info("退出代码: " + exec.getExitCode());

            // 获取容器统计信息以获取内存使用情况
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
                        ExecCreateCmdResponse psCmd = dockerClient.execCreateCmd(containerId)
                                .withCmd("sh", "-c", "ps -o rss= -p 1")
                                .withAttachStdout(true)
                                .exec();

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

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new ExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "容器执行过程中发生错误: " + e.getMessage(), e);
            throw e; // 重新抛出异常以便上层处理
        } finally {
            // 清理容器
            if (containerId != null) {
                try {
                    logger.info("清理容器: " + containerId);
                    dockerClient.stopContainerCmd(containerId).withTimeout(2).exec();
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    logger.info("容器已清理");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "清理容器时出错: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 在容器中执行Python代码(带测试文件)
     */
    private ExecutionMetrics executeInContainerWithTestFile(String pythonFileName, String testFileName, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);

        try {
            // 准备卷绑定用于代码目录
            Volume codeVolume = new Volume(WORK_DIR);
            Bind bind = new Bind(tempDirectory.toAbsolutePath().toString(), codeVolume);

            // 创建容器
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withBinds(bind)
                    .withMemory((long) MEMORY_LIMIT)
                    .withCpuCount((long) CPU_LIMIT)
                    .withNetworkMode("none"); // 隔离网络

            logger.info("创建Docker容器(带测试文件)...");
            CreateContainerResponse container = dockerClient.createContainerCmd(DOCKER_IMAGE)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(WORK_DIR)
                    // 使用命令保持容器运行
                    .withCmd("tail", "-f", "/dev/null")
                    .exec();

            containerId = container.getId();
            logger.info("容器创建成功: " + containerId);

            // 启动容器
            logger.info("启动容器...");
            dockerClient.startContainerCmd(containerId).exec();

            // 等待一小段时间确保容器启动完成
            Thread.sleep(1000);

            // 检查容器是否在运行
            boolean isRunning = dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning();
            if (!isRunning) {
                throw new RuntimeException("容器未能成功启动，请检查Docker服务和镜像是否正常");
            }

            logger.info("容器已启动并正在运行");

            // 检查Python版本（确认环境正确）
            ExecCreateCmdResponse versionCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("python", "--version")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution versionExec = executeCommand(versionCmd.getId());
            logger.info("Python版本: " + versionExec.getOutput().trim());

            if (versionExec.getExitCode() != 0) {
                throw new RuntimeException("Python环境异常，无法获取Python版本: " + versionExec.getOutput());
            }

            // 检查测试文件是否存在
            ExecCreateCmdResponse checkFileCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("ls", "-la", WORK_DIR)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution checkFileExec = executeCommand(checkFileCmd.getId());
            logger.info("目录内容: " + checkFileExec.getOutput().trim());

            // 验证测试文件是否存在和可读
            ExecCreateCmdResponse catFileCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("cat", WORK_DIR + "/" + testFileName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution catFileExec = executeCommand(catFileCmd.getId());
            logger.info("测试文件内容可读性检查: " + (catFileExec.getExitCode() == 0 ? "成功" : "失败"));

            if (catFileExec.getExitCode() != 0) {
                logger.severe("测试文件无法读取: " + catFileExec.getOutput());
                return new ExecutionMetrics(
                        "FILE_ERROR",
                        "测试文件无法读取: " + catFileExec.getOutput(),
                        System.currentTimeMillis() - startTime,
                        0,
                        false);
            }

            // 执行Python代码(传递测试文件路径作为参数)
            logger.info("执行Python代码(带测试文件): " + pythonFileName);
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("python", WORK_DIR + "/" + pythonFileName, WORK_DIR + "/" + testFileName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();
            logger.info("代码执行完成，输出: " + output);
            logger.info("退出代码: " + exec.getExitCode());

            // 获取容器统计信息以获取内存使用情况
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
                        ExecCreateCmdResponse psCmd = dockerClient.execCreateCmd(containerId)
                                .withCmd("sh", "-c", "ps -o rss= -p 1")
                                .withAttachStdout(true)
                                .exec();

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

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new ExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "容器执行过程中发生错误: " + e.getMessage(), e);
            throw e; // 重新抛出异常以便上层处理
        } finally {
            // 清理容器
            if (containerId != null) {
                try {
                    logger.info("清理容器: " + containerId);
                    dockerClient.stopContainerCmd(containerId).withTimeout(2).exec();
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    logger.info("容器已清理");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "清理容器时出错: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 执行命令并获取其输出
     */
    private CompletedExecution executeCommand(String execId) throws InterruptedException {
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
    private void createTempDirectory() throws IOException {
        tempDirectory = Files.createTempDirectory("python-sandbox-");
        logger.info("创建临时目录: " + tempDirectory);
    }

    /**
     * 将内容写入文件
     */
    private void writeToFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
        logger.info("代码已写入文件: " + filePath);
    }

    /**
     * 清理临时目录
     */
    private void cleanupTempDirectory() {
        if (tempDirectory != null) {
            try {
                Files.walk(tempDirectory)
                        .sorted((a, b) -> b.compareTo(a)) // 反序以先删除子文件
                        .map(Path::toFile)
                        .forEach(file -> {
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
     * 从多次执行中计算平均指标
     */
    private ExecutionResult calculateAverageMetrics(List<ExecutionMetrics> metrics) {
        long totalExecutionTime = 0;
        long totalMemoryUsed = 0;
        long maxExecutionTime = 0;
        long maxMemoryUsed = 0;

        for (ExecutionMetrics metric : metrics) {
            totalExecutionTime += metric.getExecutionTime();
            totalMemoryUsed += metric.getMemoryUsed();

            maxExecutionTime = Math.max(maxExecutionTime, metric.getExecutionTime());
            maxMemoryUsed = Math.max(maxMemoryUsed, metric.getMemoryUsed());
        }

        int size = metrics.size();
        ExecutionResult result = new ExecutionResult();
        result.setExecutionResults(metrics);
        result.setAverageExecutionTime(size > 0 ? totalExecutionTime / size : 0);
        result.setAverageMemoryUsed(size > 0 ? totalMemoryUsed / size : 0);
        result.setMaxExecutionTime(maxExecutionTime);
        result.setMaxMemoryUsed(maxMemoryUsed);
        result.setSuccess(true);

        return result;
    }

    /**
     * 执行指标的内部类
     */
    public static class ExecutionMetrics {
        private String status;
        private String output;
        private long executionTime; // 毫秒
        private long memoryUsed; // 字节
        private boolean outputMatched;

        public ExecutionMetrics(String status, String output, long executionTime, long memoryUsed, boolean outputMatched) {
            this.status = status;
            this.output = output;
            this.executionTime = executionTime;
            this.memoryUsed = memoryUsed;
            this.outputMatched = outputMatched;
        }

        public String getStatus() { return status; }
        public String getOutput() { return output; }
        public long getExecutionTime() { return executionTime; }
        public long getMemoryUsed() { return memoryUsed; }
        public boolean isOutputMatched() { return outputMatched; }
    }

    /**
     * 执行结果的内部类
     */
    public static class ExecutionResult {
        private boolean success;
        private boolean outputMatched;
        private List<ExecutionMetrics> executionResults;
        private long averageExecutionTime;
        private long averageMemoryUsed;
        private long maxExecutionTime;
        private long maxMemoryUsed;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public boolean isOutputMatched() { return outputMatched; }
        public void setOutputMatched(boolean outputMatched) { this.outputMatched = outputMatched; }
        public List<ExecutionMetrics> getExecutionResults() { return executionResults; }
        public void setExecutionResults(List<ExecutionMetrics> executionResults) { this.executionResults = executionResults; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(long averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }
        public long getAverageMemoryUsed() { return averageMemoryUsed; }
        public void setAverageMemoryUsed(long averageMemoryUsed) { this.averageMemoryUsed = averageMemoryUsed; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
        public void setMaxExecutionTime(long maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }
        public long getMaxMemoryUsed() { return maxMemoryUsed; }
        public void setMaxMemoryUsed(long maxMemoryUsed) { this.maxMemoryUsed = maxMemoryUsed; }
    }

    /**
     * 命令执行完成的内部类
     */
    private static class CompletedExecution {
        private final int exitCode;
        private final String output;

        public CompletedExecution(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() { return exitCode; }
        public String getOutput() { return output; }
    }
}
