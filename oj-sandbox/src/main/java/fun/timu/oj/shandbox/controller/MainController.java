package fun.timu.oj.shandbox.controller;

import fun.timu.oj.shandbox.docker.executor.AbstractDockerExecutor;
import fun.timu.oj.shandbox.docker.executor.DockerCodeExecutor;
import fun.timu.oj.shandbox.docker.executor.JavaScriptDockerExecutor;
import fun.timu.oj.shandbox.docker.executor.PythonDockerExecutor;
import fun.timu.oj.shandbox.interfaces.ExecuteCodeRequest;
import fun.timu.oj.shandbox.interfaces.ExecuteCodeResponse;
import fun.timu.oj.shandbox.interfaces.JudgeInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 沙箱控制器(内网调用)
 */
@RestController
@RequestMapping("/api/sandbox")
public class MainController {
    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    private final Logger logger = Logger.getLogger(MainController.class.getName());

    // 执行器实例，避免重复创建
    private final DockerCodeExecutor javaExecutor = new DockerCodeExecutor(false);
    private final PythonDockerExecutor pythonExecutor = new PythonDockerExecutor(false);
    private final JavaScriptDockerExecutor jsExecutor = new JavaScriptDockerExecutor(false);

    /**
     * 检查请求头中的鉴权信息
     */
    private boolean authenticateRequest(String auth) {
        return AUTH_REQUEST_SECRET.equals(auth);
    }

    /**
     * 执行健康检查的端点
     */
    @GetMapping("/health")
    public String HealthCheck() {
        return "ok";
    }

    /**
     * 统一的代码执行接口 - 根据language选择执行器
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecuteCodeResponse> executeCode(@RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth, @RequestBody ExecuteCodeRequest request) {
        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse("未授权的访问"));
        }

        try {
            // 根据语言选择不同的执行器
            switch (request.getLanguage()) {
                case JAVA:
                    return ResponseEntity.ok(executeJavaCodeInternal(request));
                case JAVASCRIPT:
                    return ResponseEntity.ok(executeJavaScriptCodeInternal(request));
                case PYTHON:
                    return ResponseEntity.ok(executePythonCodeInternal(request));
                default:
                    return ResponseEntity.badRequest().body(buildErrorResponse("不支持的编程语言: " + request.getLanguage()));
            }
        } catch (Exception e) {
            logger.severe("执行代码时出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse("执行出错: " + e.getMessage()));
        }
    }

    /**
     * 执行Java代码 (接口模型版)
     */
    @PostMapping("/java")
    public ResponseEntity<ExecuteCodeResponse> executeJava(@RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth, @RequestBody ExecuteCodeRequest request) {
        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse("未授权的访问"));
        }

        try {
            return ResponseEntity.ok(executeJavaCodeInternal(request));
        } catch (Exception e) {
            logger.severe("执行Java代码时出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse("执行出错: " + e.getMessage()));
        }
    }

    /**
     * 执行JavaScript代码 (接口模型版)
     */
    @PostMapping("/javascript")
    public ResponseEntity<ExecuteCodeResponse> executeJavaScript(@RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth, @RequestBody ExecuteCodeRequest request) {
        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse("未授权的访问"));
        }

        try {
            return ResponseEntity.ok(executeJavaScriptCodeInternal(request));
        } catch (Exception e) {
            logger.severe("执行JavaScript代码时出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse("执行出错: " + e.getMessage()));
        }
    }

    /**
     * 执行Python代码 (接口模型版)
     */
    @PostMapping("/python")
    public ResponseEntity<ExecuteCodeResponse> executePython(@RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth, @RequestBody ExecuteCodeRequest request) {
        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse("未授权的访问"));
        }

        try {
            return ResponseEntity.ok(executePythonCodeInternal(request));
        } catch (Exception e) {
            logger.severe("执行Python代码时出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse("执行出错: " + e.getMessage()));
        }
    }

    /**
     * 内部方法：执行Java代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executeJavaCodeInternal(ExecuteCodeRequest request) throws Exception {
        DockerCodeExecutor.JavaExecutionResult result;

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 仅支持命令行参数模式
            String[] args = request.getInputs().toArray(new String[0]);
            result = javaExecutor.executeJavaCodeWithArgs(request.getCode(), args, null, Math.max(1, request.getExecutionCount()));
        } else {
            // 无输入的代码执行
            result = javaExecutor.executeJavaCode(request.getCode(), null, Math.max(1, request.getExecutionCount()));
        }

        return convertToExecuteCodeResponse(result);
    }

    /**
     * 内部方法：执行JavaScript代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executeJavaScriptCodeInternal(ExecuteCodeRequest request) throws Exception {
        JavaScriptDockerExecutor.JSExecutionResult result;

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 仅支持命令行参数模式
            String[] args = request.getInputs().toArray(new String[0]);
            result = jsExecutor.executeJavaScriptCodeWithArgs(request.getCode(), args, null, Math.max(1, request.getExecutionCount()));
        } else {
            // 无输入的代码执行
            result = jsExecutor.executeJavaScriptCode(request.getCode(), null, Math.max(1, request.getExecutionCount()));
        }

        return convertToExecuteCodeResponse(result);
    }

    /**
     * 内部方法：执行Python代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executePythonCodeInternal(ExecuteCodeRequest request) throws Exception {
        PythonDockerExecutor.PythonExecutionResult result;

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 仅支持命令行参数模式
            String[] args = request.getInputs().toArray(new String[0]);
            result = pythonExecutor.executePythonCodeWithArgs(request.getCode(), args, null, Math.max(1, request.getExecutionCount()));
        } else {
            // 无输入的代码执行
            result = pythonExecutor.executePythonCode(request.getCode(), null, Math.max(1, request.getExecutionCount()));
        }

        return convertToExecuteCodeResponse(result);
    }

    /**
     * 转换Docker执行结果为接口响应模型
     */
    private ExecuteCodeResponse convertToExecuteCodeResponse(AbstractDockerExecutor.ExecutionResult result) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();

        // 设置执行状态
        response.setStatus(result.isSuccess() ? ExecuteCodeResponse.ExecuteStatus.SUCCEED : ExecuteCodeResponse.ExecuteStatus.FAILED);

        // 设置输出信息
        List<String> outputs = new ArrayList<>();
        for (AbstractDockerExecutor.ExecutionMetrics metric : result.getExecutionResults()) {
            outputs.add(metric.getOutput());
        }
        response.setOutput(outputs);

        // 设置执行信息
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(result.isSuccess() ? "成功" : "执行错误");
        judgeInfo.setMemory(result.getMaxMemoryUsed() / 1024); // 转为KB
        judgeInfo.setTime(result.getMaxExecutionTime());

        response.setJudgeInfo(judgeInfo);

        return response;
    }

    /**
     * 构建错误响应
     */
    private ExecuteCodeResponse buildErrorResponse(String errorMessage) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(ExecuteCodeResponse.ExecuteStatus.FAILED);

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(errorMessage);
        response.setJudgeInfo(judgeInfo);

        return response;
    }

    /**
     * 在应用关闭时，确保资源被释放
     * 现在统一清理所有容器以提升运行期间的性能
     */
    @PreDestroy
    public void cleanup() {
        logger.info("=== SpringBoot @PreDestroy 被调用，开始并行清理容器资源 ===");

        try {
            long startTime = System.currentTimeMillis();

            // 统计容器数量 - 修复访问权限问题
            int javaContainers = javaExecutor.getContainerCount();
            int pythonContainers = pythonExecutor.getContainerCount();
            int jsContainers = jsExecutor.getContainerCount();
            int totalContainers = javaContainers + pythonContainers + jsContainers;

            logger.info("待清理容器详情: Java=" + javaContainers + ", Python=" + pythonContainers + ", JavaScript=" + jsContainers + ", 总计=" + totalContainers);

            if (totalContainers == 0) {
                logger.info("没有需要清理的容器");
                return;
            }

            // 并行清理执行器资源
            logger.info("开始并行清理执行器...");

            // 使用CompletableFuture并行执行清理
            CompletableFuture<Void> javaCleanup = CompletableFuture.runAsync(() -> {
                try {
                    logger.info("清理Java执行器...");
                    javaExecutor.cleanup();
                    logger.info("Java执行器清理完成");
                } catch (Exception e) {
                    logger.severe("Java执行器清理失败: " + e.getMessage());
                }
            });

            CompletableFuture<Void> pythonCleanup = CompletableFuture.runAsync(() -> {
                try {
                    logger.info("清理Python执行器...");
                    pythonExecutor.cleanup();
                    logger.info("Python执行器清理完成");
                } catch (Exception e) {
                    logger.severe("Python执行器清理失败: " + e.getMessage());
                }
            });

            CompletableFuture<Void> jsCleanup = CompletableFuture.runAsync(() -> {
                try {
                    logger.info("清理JavaScript执行器...");
                    jsExecutor.cleanup();
                    logger.info("JavaScript执行器清理完成");
                } catch (Exception e) {
                    logger.severe("JavaScript执行器清理失败: " + e.getMessage());
                }
            });

            // 等待所有清理任务完成
            CompletableFuture.allOf(javaCleanup, pythonCleanup, jsCleanup).get(60, java.util.concurrent.TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("=== 所有容器资源并行清理完成，耗时: " + duration + "ms ===");

        } catch (java.util.concurrent.TimeoutException e) {
            logger.severe("清理任务超时: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("清理资源时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 添加一个手动清理接口，用于调试和紧急清理
     */
    @PostMapping("/cleanup")
    public ResponseEntity<String> manualCleanup(@RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth) {
        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
        }

        try {
            long startTime = System.currentTimeMillis();
            logger.info("手动触发并行容器清理...");

            // 显示清理前的容器状态
            int totalContainers = javaExecutor.getContainerCount() + pythonExecutor.getContainerCount() + jsExecutor.getContainerCount();

            logger.info("清理前容器总数: " + totalContainers);

            cleanup();

            long duration = System.currentTimeMillis() - startTime;
            String result = String.format("并行容器清理完成，清理了 %d 个容器，耗时: %dms", totalContainers, duration);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("手动清理失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("清理失败: " + e.getMessage());
        }
    }

    /**
     * 添加一个容器状态查询接口，用于监控
     */
    @GetMapping("/containers/status")
    public ResponseEntity<String> getContainerStatus(@RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth) {
        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
        }

        try {
            int javaContainers = javaExecutor.getContainerCount();
            int pythonContainers = pythonExecutor.getContainerCount();
            int jsContainers = jsExecutor.getContainerCount();
            int totalContainers = javaContainers + pythonContainers + jsContainers;

            String status = String.format("容器状态统计:\n" + "Java执行器: %d 个容器\n" + "Python执行器: %d 个容器\n" + "JavaScript执行器: %d 个容器\n" + "总计: %d 个容器", javaContainers, pythonContainers, jsContainers, totalContainers);

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.severe("获取容器状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取状态失败: " + e.getMessage());
        }
    }
}