package fun.timu.oj.shandbox.docker.executor;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import fun.timu.oj.shandbox.docker.entity.ExecutionMetrics;
import fun.timu.oj.shandbox.docker.entity.ExecutionResult;
import fun.timu.oj.shandbox.docker.entity.PythonExecutionMetrics;
import fun.timu.oj.shandbox.docker.pool.LongRunningContainerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Python代码执行器
 * 用于在Docker容器中执行Python代码并验证结果
 */
public class PythonDockerExecutor extends AbstractDockerExecutor<ExecutionResult> {
    private static final String DOCKER_IMAGE = "python:3.9-slim";

    public PythonDockerExecutor() {
        super(DOCKER_IMAGE, PythonDockerExecutor.class.getName());
    }

    public PythonDockerExecutor(boolean pullImageAlways) {
        super(DOCKER_IMAGE, PythonDockerExecutor.class.getName(), pullImageAlways);
    }

    @Override
    protected String getCodeFileName() {
        return "solution.py";
    }

    @Override
    protected String getTempDirPrefix() {
        return "python-sandbox-";
    }

    @Override
    protected String getLanguageIdentifier() {
        return "python";
    }

    @Override
    protected void afterCodeFileWritten(String codePath) throws Exception {
        // Python不需要额外的处理
    }

    @Override
    protected void afterTestFileWritten(String testFilePath) throws Exception {
        // Python不需要额外的处理
    }

    @Override
    protected ExecutionMetrics executeInContainer(String pythonFileName, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);
        boolean usingLongRunningContainer = false;

        try {
            // 尝试使用长期运行容器
            if (enableContainerReuse) {
                try {
                    LongRunningContainerManager.ContainerInfo containerInfo = getOrCreateLongRunningContainer(getLanguageIdentifier());
                    if (containerInfo != null) {
                        containerId = containerInfo.getContainerId();
                        usingLongRunningContainer = true;
                        logger.info("使用长期运行容器: " + containerId);
                        
                        // 复制代码文件到容器
                        containerManager.copyCodeToContainer(containerInfo, tempDirectory);
                    }
                } catch (Exception e) {
                    logger.warning("长期运行容器不可用，回退到传统模式: " + e.getMessage());
                    containerId = null;
                }
            }

            // 如果没有使用长期运行容器，使用传统方式
            if (containerId == null) {
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
                CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                        .withHostConfig(hostConfig)
                        .withWorkingDir(WORK_DIR)
                        // 使用命令保持容器运行
                        .withCmd("tail", "-f", "/dev/null")
                        .exec();

                containerId = container.getId();
                createdContainers.add(containerId);
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
            }

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
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new PythonExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } finally {
            if (usingLongRunningContainer && containerId != null) {
                // 清理工作目录而不是删除容器
                try {
                    ExecCreateCmdResponse cleanupCmd = dockerClient.execCreateCmd(containerId)
                            .withCmd("sh", "-c", "rm -rf " + WORK_DIR + "/*")
                            .withAttachStdout(true)
                            .withAttachStderr(true)
                            .exec();
                    executeCommand(cleanupCmd.getId());
                    logger.info("已清理长期运行容器的工作目录");
                } catch (Exception e) {
                    logger.warning("清理长期运行容器工作目录失败: " + e.getMessage());
                }
            }
            // 注释掉：不再每次执行后清理容器，提升性能
            // 容器将在应用结束时统一清理
            // cleanupContainer(containerId);
        }
    }

    @Override
    protected ExecutionMetrics executeInContainerWithArgs(String pythonFileName, String[] args, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);
        boolean usingLongRunningContainer = false;

        try {
            // 尝试使用长期运行容器
            if (enableContainerReuse) {
                try {
                    LongRunningContainerManager.ContainerInfo containerInfo = getOrCreateLongRunningContainer(getLanguageIdentifier());
                    if (containerInfo != null) {
                        containerId = containerInfo.getContainerId();
                        usingLongRunningContainer = true;
                        logger.info("使用长期运行容器(带参数): " + containerId);
                        
                        // 复制代码文件到容器
                        containerManager.copyCodeToContainer(containerInfo, tempDirectory);
                    }
                } catch (Exception e) {
                    logger.warning("长期运行容器不可用，回退到传统模式: " + e.getMessage());
                    containerId = null;
                }
            }

            // 如果没有使用长期运行容器，使用传统方式
            if (containerId == null) {
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
                CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                        .withHostConfig(hostConfig)
                        .withWorkingDir(WORK_DIR)
                        // 使用命令保持容器运行
                        .withCmd("tail", "-f", "/dev/null")
                        .exec();

                containerId = container.getId();
                createdContainers.add(containerId);
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
            }

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
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new PythonExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } finally {
            if (usingLongRunningContainer && containerId != null) {
                // 清理工作目录而不是删除容器
                try {
                    ExecCreateCmdResponse cleanupCmd = dockerClient.execCreateCmd(containerId)
                            .withCmd("sh", "-c", "rm -rf " + WORK_DIR + "/*")
                            .withAttachStdout(true)
                            .withAttachStderr(true)
                            .exec();
                    executeCommand(cleanupCmd.getId());
                    logger.info("已清理长期运行容器的工作目录");
                } catch (Exception e) {
                    logger.warning("清理长期运行容器工作目录失败: " + e.getMessage());
                }
            }
            // 注释掉：不再每次执行后清理容器，提升性能
            // 容器将在应用结束时统一清理
            // cleanupContainer(containerId);
        }
    }

    @Override
    protected ExecutionMetrics executeInContainerWithTestFile(String pythonFileName, String testFileName, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);
        boolean usingLongRunningContainer = false;

        try {
            // 尝试使用长期运行容器
            if (enableContainerReuse) {
                try {
                    LongRunningContainerManager.ContainerInfo containerInfo = getOrCreateLongRunningContainer(getLanguageIdentifier());
                    if (containerInfo != null) {
                        containerId = containerInfo.getContainerId();
                        usingLongRunningContainer = true;
                        logger.info("使用长期运行容器(带测试文件): " + containerId);
                        
                        // 复制代码文件到容器
                        containerManager.copyCodeToContainer(containerInfo, tempDirectory);
                    }
                } catch (Exception e) {
                    logger.warning("长期运行容器不可用，回退到传统模式: " + e.getMessage());
                    containerId = null;
                }
            }

            // 如果没有使用长期运行容器，使用传统方式
            if (containerId == null) {
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
                CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                        .withHostConfig(hostConfig)
                        .withWorkingDir(WORK_DIR)
                        // 使用命令保持容器运行
                        .withCmd("tail", "-f", "/dev/null")
                        .exec();

                containerId = container.getId();
                createdContainers.add(containerId);
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
            }

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
                return new PythonExecutionMetrics(
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
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new PythonExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } finally {
            if (usingLongRunningContainer && containerId != null) {
                // 清理工作目录而不是删除容器
                try {
                    ExecCreateCmdResponse cleanupCmd = dockerClient.execCreateCmd(containerId)
                            .withCmd("sh", "-c", "rm -rf " + WORK_DIR + "/*")
                            .withAttachStdout(true)
                            .withAttachStderr(true)
                            .exec();
                    executeCommand(cleanupCmd.getId());
                    logger.info("已清理长期运行容器的工作目录");
                } catch (Exception e) {
                    logger.warning("清理长期运行容器工作目录失败: " + e.getMessage());
                }
            }
            // 注释掉：不再每次执行后清理容器，提升性能
            // 容器将在应用结束时统一清理
            // cleanupContainer(containerId);
        }
    }

    @Override
    protected ExecutionMetrics createErrorExecutionMetrics(String status, String errorMessage) {
        return new PythonExecutionMetrics(status, errorMessage, 0, 0, false);
    }

    @Override
    protected ExecutionResult calculateAverageMetrics(List<ExecutionMetrics> metrics) {
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
     * 为了向后兼容旧版API，添加适配方法
     */
    public ExecutionResult executePythonCode(String pythonCode, String expectedOutput, int executionCount) throws Exception {
        return executeCode(pythonCode, expectedOutput, executionCount);
    }

    public ExecutionResult executePythonCode(String pythonCode, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        return executeCode(pythonCode, expectedOutput, executionCount, forcePull);
    }

    public ExecutionResult executePythonCodeWithArgs(String pythonCode, String[] args, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithArgs(pythonCode, args, expectedOutput, executionCount);
    }

    public ExecutionResult executePythonCodeWithArgs(String pythonCode, String[] args, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        return executeCodeWithArgs(pythonCode, args, expectedOutput, executionCount, forcePull);
    }

    public ExecutionResult executePythonCodeWithTestFile(String pythonCode, String testCaseContent, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithTestFile(pythonCode, testCaseContent, expectedOutput, executionCount);
    }

    public ExecutionResult executePythonCodeWithTestFile(String pythonCode, String testCaseContent, String expectedOutput, int executionCount, boolean forcePull) throws Exception {
        return executeCodeWithTestFile(pythonCode, testCaseContent, expectedOutput, executionCount, forcePull);
    }
}
