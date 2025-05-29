package fun.timu.oj.shandbox.docker.executor;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import fun.timu.oj.shandbox.docker.entity.ExecutionMetrics;
import fun.timu.oj.shandbox.docker.entity.ExecutionResult;
import fun.timu.oj.shandbox.docker.entity.JavaScriptExecutionMetrics;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * JavaScript代码执行器
 * 用于在Docker容器中执行JavaScript代码并验证结果
 */
public class JavaScriptDockerExecutor extends AbstractDockerExecutor<ExecutionResult> {
    private static final String DOCKER_IMAGE = "node:18-alpine";
    private static final int CONTAINER_WAIT_TIME = 2; // 容器启动等待秒数

    public JavaScriptDockerExecutor() {
        super(DOCKER_IMAGE, JavaScriptDockerExecutor.class.getName());
    }

    public JavaScriptDockerExecutor(boolean pullImageAlways) {
        super(DOCKER_IMAGE, JavaScriptDockerExecutor.class.getName(), pullImageAlways);
    }

    @Override
    protected String getCodeFileName() {
        return "solution.js";
    }

    @Override
    protected String getTempDirPrefix() {
        return "js-sandbox-";
    }

    @Override
    protected void afterCodeFileWritten(String codePath) throws Exception {
        // 设置文件权限，确保可读可执行
        setExecutablePermissions(codePath);
    }

    @Override
    protected void afterTestFileWritten(String testFilePath) throws Exception {
        // 设置测试文件权限，确保可读
        setExecutablePermissions(testFilePath);
    }

    @Override
    protected ExecutionMetrics executeInContainer(String jsFileName, String expectedOutput) throws Exception {
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

            logger.info("创建Docker容器");
            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(WORK_DIR)
                    // 使用sleep infinity保持容器运行
                    .withCmd("/bin/sh", "-c", "sleep infinity")
                    .exec();

            containerId = container.getId();
            createdContainers.add(containerId);
            logger.info("容器创建成功，ID: " + containerId);

            // 启动容器
            logger.info("启动容器");
            dockerClient.startContainerCmd(containerId).exec();

            // 等待容器启动完成
            logger.info("等待容器启动完成 " + (CONTAINER_WAIT_TIME + 1) + " 秒");
            Thread.sleep((CONTAINER_WAIT_TIME + 1) * 1000);

            // 验证容器是否正在运行
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            logger.info("容器当前状态: " + containerInfo.getState().getStatus());

            if (!containerInfo.getState().getRunning()) {
                throw new RuntimeException("容器未处于运行状态，当前状态: " + containerInfo.getState().getStatus());
            }

            logger.info("容器启动成功，正在运行");

            // 检查Node.js是否可用
            logger.info("检查Node.js可用性");
            ExecCreateCmdResponse checkNodeCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("node", "--version")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution checkNodeExec = executeCommand(checkNodeCmd.getId());
            logger.info("Node.js版本: " + checkNodeExec.getOutput().trim());

            if (checkNodeExec.getExitCode() != 0) {
                logger.severe("Node.js不可用，退出代码: " + checkNodeExec.getExitCode());
                return new JavaScriptExecutionMetrics(
                        "ENVIRONMENT_ERROR",
                        "Node.js不可用: " + checkNodeExec.getOutput(),
                        System.currentTimeMillis() - startTime,
                        0,
                        false);
            }

            // 检查JavaScript文件是否存在
            logger.info("检查代码文件存在性");
            ExecCreateCmdResponse checkFileCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("ls", "-la", WORK_DIR)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution checkFileExec = executeCommand(checkFileCmd.getId());
            logger.info("目录内容: " + checkFileExec.getOutput().trim());

            // 执行JavaScript代码
            logger.info("执行JavaScript代码");
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("node", WORK_DIR + "/" + jsFileName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();
            logger.info("代码执行完成，输出: " + output);
            logger.info("退出代码: " + exec.getExitCode());

            // 收集内存使用情况
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new JavaScriptExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } finally {
            // 注释掉：不再每次执行后清理容器，提升性能
            // cleanupContainer(containerId);
        }
    }

    @Override
    protected ExecutionMetrics executeInContainerWithArgs(String jsFileName, String[] args, String expectedOutput) throws Exception {
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

            logger.info("创建Docker容器(带参数)");
            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(WORK_DIR)
                    // 使用sleep infinity保持容器运行
                    .withCmd("/bin/sh", "-c", "sleep infinity")
                    .exec();

            containerId = container.getId();
            createdContainers.add(containerId);
            logger.info("容器创建成功，ID: " + containerId);

            // 启动容器
            logger.info("启动容器");
            dockerClient.startContainerCmd(containerId).exec();

            // 等待容器启动完成
            logger.info("等待容器启动完成 " + (CONTAINER_WAIT_TIME + 1) + " 秒");
            Thread.sleep((CONTAINER_WAIT_TIME + 1) * 1000);

            // 验证容器是否正在运行
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            if (!containerInfo.getState().getRunning()) {
                throw new RuntimeException("容器未处于运行状态，当前状态: " + containerInfo.getState().getStatus());
            }

            // 执行JavaScript代码(带参数)
            List<String> cmdList = new ArrayList<>();
            cmdList.add("node");
            cmdList.add(WORK_DIR + "/" + jsFileName);

            // 添加所有参数
            if (args != null) {
                for (String arg : args) {
                    cmdList.add(arg);
                }
            }

            logger.info("执行命令: " + String.join(" ", cmdList));
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdList.toArray(new String[0]))
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();
            logger.info("代码执行完成，输出: " + output);
            logger.info("退出代码: " + exec.getExitCode());

            // 收集内存使用情况
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new JavaScriptExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } finally {
            // 注释掉：不再每次执行后清理容器，提升性能
            // cleanupContainer(containerId);
        }
    }

    @Override
    protected ExecutionMetrics executeInContainerWithTestFile(String jsFileName, String testFileName, String expectedOutput) throws Exception {
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

            logger.info("创建Docker容器(带测试文件)");
            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(WORK_DIR)
                    // 使用sleep infinity保持容器运行
                    .withCmd("/bin/sh", "-c", "sleep infinity")
                    .exec();

            containerId = container.getId();
            createdContainers.add(containerId);
            logger.info("容器创建成功，ID: " + containerId);

            // 启动容器
            logger.info("启动容器");
            dockerClient.startContainerCmd(containerId).exec();

            // 等待容器启动完成
            logger.info("等待容器启动完成 " + (CONTAINER_WAIT_TIME + 1) + " 秒");
            Thread.sleep((CONTAINER_WAIT_TIME + 1) * 1000);

            // 验证容器是否正在运行
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            if (!containerInfo.getState().getRunning()) {
                throw new RuntimeException("容器未处于运行状态，当前状态: " + containerInfo.getState().getStatus());
            }

            // 检查测试文件是否存在
            ExecCreateCmdResponse catFileCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("cat", WORK_DIR + "/" + testFileName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution catFileExec = executeCommand(catFileCmd.getId());
            if (catFileExec.getExitCode() != 0) {
                logger.severe("测试文件无法读取: " + catFileExec.getOutput());
                return new JavaScriptExecutionMetrics(
                        "FILE_ERROR",
                        "测试文件无法读取: " + catFileExec.getOutput(),
                        System.currentTimeMillis() - startTime,
                        0,
                        false);
            }

            // 执行JavaScript代码(带测试文件)
            logger.info("执行JavaScript代码(带测试文件)");
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("node", WORK_DIR + "/" + jsFileName, WORK_DIR + "/" + testFileName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();
            logger.info("代码执行完成，输出: " + output);
            logger.info("退出代码: " + exec.getExitCode());

            // 收集内存使用情况
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new JavaScriptExecutionMetrics(
                    exec.getExitCode() == 0 ? "COMPLETED" : "RUNTIME_ERROR",
                    output,
                    System.currentTimeMillis() - startTime,
                    memoryUsage.get(),
                    matched);

        } finally {
            // 注释掉：不再每次执行后清理容器，提升性能
            // cleanupContainer(containerId);
        }
    }

    @Override
    protected ExecutionMetrics createErrorExecutionMetrics(String status, String errorMessage) {
        return new JavaScriptExecutionMetrics(status, errorMessage, 0, 0, false);
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
     * 设置文件可执行权限
     *
     * @param filePath 需要设置权限的文件路径
     */
    private void setExecutablePermissions(String filePath) {
        try {
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

            Files.setPosixFilePermissions(Paths.get(filePath), perms);
            logger.info("已设置文件权限: " + filePath);
        } catch (Exception e) {
            logger.log(Level.WARNING, "设置文件权限时出错，将继续尝试执行: " + e.getMessage());
        }
    }

    /**
     * JavaScript执行指标的实现类
     */
    public static class JSExecutionMetricsImpl implements ExecutionMetrics {
        private final String status;
        private final String output;
        private final long executionTime; // 毫秒
        private final long memoryUsed; // 字节
        private final boolean outputMatched;

        public JSExecutionMetricsImpl(String status, String output, long executionTime, long memoryUsed, boolean outputMatched) {
            this.status = status;
            this.output = output;
            this.executionTime = executionTime;
            this.memoryUsed = memoryUsed;
            this.outputMatched = outputMatched;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public String getOutput() {
            return output;
        }

        @Override
        public long getExecutionTime() {
            return executionTime;
        }

        @Override
        public long getMemoryUsed() {
            return memoryUsed;
        }

        @Override
        public boolean isOutputMatched() {
            return outputMatched;
        }
    }

    /**
     * 为了向后兼容旧版API，添加适配方法
     */
    public ExecutionResult executeJavaScriptCode(String jsCode, String expectedOutput, int executionCount) throws Exception {
        return executeCode(jsCode, expectedOutput, executionCount);
    }

    public ExecutionResult executeJavaScriptCodeWithArgs(String jsCode, String[] args, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithArgs(jsCode, args, expectedOutput, executionCount);
    }

    public ExecutionResult executeJavaScriptCodeWithTestFile(String jsCode, String testCaseContent, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithTestFile(jsCode, testCaseContent, expectedOutput, executionCount);
    }
}