package fun.timu.oj.shandbox.docker.executor;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import fun.timu.oj.shandbox.docker.entity.ExecutionMetrics;
import fun.timu.oj.shandbox.docker.entity.ExecutionResult;
import fun.timu.oj.shandbox.docker.entity.JavaExecutionMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Java代码执行器
 * 用于在Docker容器中执行Java代码并验证结果
 */
public class DockerCodeExecutor extends AbstractDockerExecutor<ExecutionResult> {
    private static final String DOCKER_IMAGE = "openjdk:11";

    public DockerCodeExecutor() {
        super(DOCKER_IMAGE, DockerCodeExecutor.class.getName());
    }

    public DockerCodeExecutor(boolean pullImageAlways) {
        super(DOCKER_IMAGE, DockerCodeExecutor.class.getName(), pullImageAlways);
    }

    @Override
    protected String getCodeFileName() {
        return "Solution.java";
    }

    @Override
    protected String getTempDirPrefix() {
        return "java-sandbox-";
    }

    @Override
    protected void afterCodeFileWritten(String codePath) {
    }

    @Override
    protected void afterTestFileWritten(String testFilePath) {
    }

    @Override
    protected ExecutionMetrics executeInContainer(String codeFileName, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);

        try {
            String mainClassName = codeFileName.replace(".java", "");

            // 准备卷绑定用于代码目录
            Volume codeVolume = new Volume(WORK_DIR);
            Bind bind = new Bind(tempDirectory.toAbsolutePath().toString(), codeVolume);

            // 创建容器
            HostConfig hostConfig = HostConfig.newHostConfig().withBinds(bind).withMemory((long) MEMORY_LIMIT).withCpuCount((long) CPU_LIMIT).withNetworkMode("none"); // 隔离网络

            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage).withHostConfig(hostConfig).withWorkingDir(WORK_DIR).exec();

            containerId = container.getId();
            createdContainers.add(containerId);

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();

            // 编译Java文件
            ExecCreateCmdResponse compileCmd = dockerClient.execCreateCmd(containerId).withCmd("javac", mainClassName + ".java").withAttachStdout(true).withAttachStderr(true).exec();

            CompletedExecution compileExec = executeCommand(compileCmd.getId());
            if (compileExec.getExitCode() != 0) {
                return new JavaExecutionMetrics("COMPILATION_ERROR", compileExec.getOutput(), System.currentTimeMillis() - startTime, 0, false);
            }

            // 执行Java程序
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId).withCmd("java", mainClassName).withAttachStdout(true).withAttachStderr(true).exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();

            // 收集内存使用情况
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new JavaExecutionMetrics(
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
    protected ExecutionMetrics executeInContainerWithArgs(String codeFileName, String[] args, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);

        try {
            String mainClassName = codeFileName.replace(".java", "");

            // 准备卷绑定用于代码目录
            Volume codeVolume = new Volume(WORK_DIR);
            Bind bind = new Bind(tempDirectory.toAbsolutePath().toString(), codeVolume);

            // 创建容器
            HostConfig hostConfig = HostConfig.newHostConfig().withBinds(bind).withMemory((long) MEMORY_LIMIT).withCpuCount((long) CPU_LIMIT).withNetworkMode("none"); // 隔离网络

            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage).withHostConfig(hostConfig).withWorkingDir(WORK_DIR).exec();

            containerId = container.getId();
            createdContainers.add(containerId);

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();

            // 编译Java文件
            ExecCreateCmdResponse compileCmd = dockerClient.execCreateCmd(containerId).withCmd("javac", mainClassName + ".java").withAttachStdout(true).withAttachStderr(true).exec();

            CompletedExecution compileExec = executeCommand(compileCmd.getId());
            if (compileExec.getExitCode() != 0) {
                return new JavaExecutionMetrics("COMPILATION_ERROR", compileExec.getOutput(), System.currentTimeMillis() - startTime, 0, false);
            }

            // 执行Java程序（带参数）
            List<String> cmdList = new ArrayList<>();
            cmdList.add("java");
            cmdList.add(mainClassName);

            // 添加所有参数
            if (args != null) {
                for (String arg : args) {
                    cmdList.add(arg);
                }
            }

            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId).withCmd(cmdList.toArray(new String[0])).withAttachStdout(true).withAttachStderr(true).exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();

            // 收集内存使用情况
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new JavaExecutionMetrics(
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
    protected ExecutionMetrics executeInContainerWithTestFile(String codeFileName, String testFileName, String expectedOutput) throws Exception {
        String containerId = null;
        long startTime = System.currentTimeMillis();
        AtomicLong memoryUsage = new AtomicLong(0);

        try {
            String mainClassName = codeFileName.replace(".java", "");

            // 准备卷绑定用于代码目录
            Volume codeVolume = new Volume(WORK_DIR);
            Bind bind = new Bind(tempDirectory.toAbsolutePath().toString(), codeVolume);

            // 创建容器
            HostConfig hostConfig = HostConfig.newHostConfig().withBinds(bind).withMemory((long) MEMORY_LIMIT).withCpuCount((long) CPU_LIMIT).withNetworkMode("none"); // 隔离网络

            CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage).withHostConfig(hostConfig).withWorkingDir(WORK_DIR).exec();

            containerId = container.getId();
            createdContainers.add(containerId);

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();

            // 编译Java文件
            ExecCreateCmdResponse compileCmd = dockerClient.execCreateCmd(containerId).withCmd("javac", mainClassName + ".java").withAttachStdout(true).withAttachStderr(true).exec();

            CompletedExecution compileExec = executeCommand(compileCmd.getId());
            if (compileExec.getExitCode() != 0) {
                return new JavaExecutionMetrics("COMPILATION_ERROR", compileExec.getOutput(), System.currentTimeMillis() - startTime, 0, false);
            }

            // 执行Java程序（带测试文件）
            ExecCreateCmdResponse execCmd = dockerClient.execCreateCmd(containerId).withCmd("java", mainClassName, testFileName).withAttachStdout(true).withAttachStderr(true).exec();

            CompletedExecution exec = executeCommand(execCmd.getId());
            String output = exec.getOutput().trim();

            // 收集内存使用情况
            memoryUsage = collectContainerMemoryUsage(containerId);

            boolean matched = expectedOutput != null && output.equals(expectedOutput.trim());
            return new JavaExecutionMetrics(
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
        return new JavaExecutionMetrics(status, errorMessage, 0, 0, false);
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
    public ExecutionResult executeJavaCode(String javaCode, String expectedOutput, int executionCount) throws Exception {
        return executeCode(javaCode, expectedOutput, executionCount);
    }

    public ExecutionResult executeJavaCodeWithArgs(String javaCode, String[] args, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithArgs(javaCode, args, expectedOutput, executionCount);
    }

    public ExecutionResult executeJavaCodeWithTestFile(String javaCode, String testCaseContent, String expectedOutput, int executionCount) throws Exception {
        return executeCodeWithTestFile(javaCode, testCaseContent, expectedOutput, executionCount);
    }
}
