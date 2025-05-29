package fun.timu.oj.shandbox.controller;

import fun.timu.oj.shandbox.docker.executor.DockerCodeExecutor;
import fun.timu.oj.shandbox.docker.executor.JavaScriptDockerExecutor;
import fun.timu.oj.shandbox.docker.executor.PythonDockerExecutor;
import fun.timu.oj.shandbox.docker.entity.ExecutionResult;
import fun.timu.oj.shandbox.docker.entity.ExecutionMetrics;
import fun.timu.oj.shandbox.interfaces.ExecuteCodeRequest;
import fun.timu.oj.shandbox.interfaces.ExecuteCodeResponse;
import fun.timu.oj.shandbox.interfaces.JudgeInfo;
import fun.timu.oj.shandbox.interfaces.ProgrammingLanguage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
     * 构造函数，初始化执行器并启用容器复用
     */
    public MainController() {
        // 启用容器复用功能
        javaExecutor.setContainerReuse(true);
        pythonExecutor.setContainerReuse(true);
        jsExecutor.setContainerReuse(true);
        logger.info("沙箱控制器初始化完成，已启用容器复用功能");
    }

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
     * 支持三种编程语言：Java、JavaScript、Python
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecuteCodeResponse> executeCode(
            @RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth,
            @RequestBody ExecuteCodeRequest request) {

        // 鉴权检查
        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse("未授权的访问"));
        }

        // 参数验证
        if (request == null || request.getLanguage() == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(buildErrorResponse("请求参数不完整：language和code为必填项"));
        }

        try {
            logger.info("开始执行 " + request.getLanguage() + " 代码，输入参数数量: " +
                    (request.getInputs() != null ? request.getInputs().size() : 0));

            ExecuteCodeResponse response;

            // 根据语言选择不同的执行器
            switch (request.getLanguage()) {
                case JAVA:
                    response = executeJavaCodeInternal(request);
                    break;
                case JAVASCRIPT:
                    response = executeJavaScriptCodeInternal(request);
                    break;
                case PYTHON:
                    response = executePythonCodeInternal(request);
                    break;
                default:
                    logger.warning("不支持的编程语言: " + request.getLanguage());
                    return ResponseEntity.badRequest()
                            .body(buildErrorResponse("不支持的编程语言: " + request.getLanguage() +
                                    "，支持的语言：JAVA、JAVASCRIPT、PYTHON"));
            }

            logger.info(request.getLanguage() + " 代码执行完成，状态: " + response.getStatus());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("执行 " + request.getLanguage() + " 代码时出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("执行出错: " + e.getMessage()));
        }
    }

    /**
     * 执行Java代码 (独立接口)
     */
    @PostMapping("/java")
    public ResponseEntity<ExecuteCodeResponse> executeJava(
            @RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth,
            @RequestBody ExecuteCodeRequest request) {

        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse("未授权的访问"));
        }

        // 强制设置语言为Java
        request.setLanguage(ProgrammingLanguage.JAVA);

        try {
            logger.info("执行Java代码，代码长度: " +
                    (request.getCode() != null ? request.getCode().length() : 0) + " 字符");

            ExecuteCodeResponse response = executeJavaCodeInternal(request);
            logger.info("Java代码执行完成，状态: " + response.getStatus());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("执行Java代码时出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Java代码执行出错: " + e.getMessage()));
        }
    }

    /**
     * 执行JavaScript代码 (独立接口)
     */
    @PostMapping("/javascript")
    public ResponseEntity<ExecuteCodeResponse> executeJavaScript(
            @RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth,
            @RequestBody ExecuteCodeRequest request) {

        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse("未授权的访问"));
        }

        // 强制设置语言为JavaScript
        request.setLanguage(ProgrammingLanguage.JAVASCRIPT);

        try {
            logger.info("执行JavaScript代码，代码长度: " +
                    (request.getCode() != null ? request.getCode().length() : 0) + " 字符");

            ExecuteCodeResponse response = executeJavaScriptCodeInternal(request);
            logger.info("JavaScript代码执行完成，状态: " + response.getStatus());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("执行JavaScript代码时出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("JavaScript代码执行出错: " + e.getMessage()));
        }
    }

    /**
     * 执行Python代码 (独立接口)
     */
    @PostMapping("/python")
    public ResponseEntity<ExecuteCodeResponse> executePython(
            @RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth,
            @RequestBody ExecuteCodeRequest request) {

        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse("未授权的访问"));
        }

        // 强制设置语言为Python
        request.setLanguage(ProgrammingLanguage.PYTHON);

        try {
            logger.info("执行Python代码，代码长度: " +
                    (request.getCode() != null ? request.getCode().length() : 0) + " 字符");

            ExecuteCodeResponse response = executePythonCodeInternal(request);
            logger.info("Python代码执行完成，状态: " + response.getStatus());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("执行Python代码时出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Python代码执行出错: " + e.getMessage()));
        }
    }

    /**
     * 内部方法：执行Java代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executeJavaCodeInternal(ExecuteCodeRequest request) throws Exception {
        // 参数验证
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Java代码不能为空");
        }

        ExecutionResult result;
        int executionCount = Math.max(1, request.getExecutionCount() != null ? request.getExecutionCount() : 1);

        logger.info("开始执行Java代码，执行次数: " + executionCount);

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 命令行参数模式
            String[] args = request.getInputs().toArray(new String[0]);
            logger.info("使用命令行参数模式，参数数量: " + args.length);
            result = javaExecutor.executeJavaCodeWithArgs(request.getCode(), args, null, executionCount);
        } else {
            // 无输入的代码执行
            logger.info("使用无参数模式");
            result = javaExecutor.executeJavaCode(request.getCode(), null, executionCount);
        }

        logger.info("Java代码执行完成，成功: " + result.isSuccess() +
                ", 最大内存: " + result.getMaxMemoryUsed() + "B" +
                ", 最大执行时间: " + result.getMaxExecutionTime() + "ms");

        return convertToExecuteCodeResponse(result, "Java");
    }

    /**
     * 内部方法：执行JavaScript代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executeJavaScriptCodeInternal(ExecuteCodeRequest request) throws Exception {
        // 参数验证
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("JavaScript代码不能为空");
        }

        ExecutionResult result;
        int executionCount = Math.max(1, request.getExecutionCount() != null ? request.getExecutionCount() : 1);

        logger.info("开始执行JavaScript代码，执行次数: " + executionCount);

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 命令行参数模式
            String[] args = request.getInputs().toArray(new String[0]);
            logger.info("使用命令行参数模式，参数数量: " + args.length);
            result = jsExecutor.executeJavaScriptCodeWithArgs(request.getCode(), args, null, executionCount);
        } else {
            // 无输入的代码执行
            logger.info("使用无参数模式");
            result = jsExecutor.executeJavaScriptCode(request.getCode(), null, executionCount);
        }

        logger.info("JavaScript代码执行完成，成功: " + result.isSuccess() +
                ", 最大内存: " + result.getMaxMemoryUsed() + "B" +
                ", 最大执行时间: " + result.getMaxExecutionTime() + "ms");

        return convertToExecuteCodeResponse(result, "JavaScript");
    }

    /**
     * 内部方法：执行Python代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executePythonCodeInternal(ExecuteCodeRequest request) throws Exception {
        // 参数验证
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Python代码不能为空");
        }

        ExecutionResult result;
        int executionCount = Math.max(1, request.getExecutionCount() != null ? request.getExecutionCount() : 1);

        logger.info("开始执行Python代码，执行次数: " + executionCount);

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 命令行参数模式
            String[] args = request.getInputs().toArray(new String[0]);
            logger.info("使用命令行参数模式，参数数量: " + args.length);
            result = pythonExecutor.executePythonCodeWithArgs(request.getCode(), args, null, executionCount);
        } else {
            // 无输入的代码执行
            logger.info("使用无参数模式");
            result = pythonExecutor.executePythonCode(request.getCode(), null, executionCount);
        }

        logger.info("Python代码执行完成，成功: " + result.isSuccess() +
                ", 最大内存: " + result.getMaxMemoryUsed() + "B" +
                ", 最大执行时间: " + result.getMaxExecutionTime() + "ms");

        return convertToExecuteCodeResponse(result, "Python");
    }

    /**
     * 转换Docker执行结果为接口响应模型
     */
    private ExecuteCodeResponse convertToExecuteCodeResponse(ExecutionResult result, String language) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();

        // 设置执行状态
        response.setStatus(result.isSuccess() ?
                ExecuteCodeResponse.ExecuteStatus.SUCCEED :
                ExecuteCodeResponse.ExecuteStatus.FAILED);

        // 设置输出信息
        List<String> outputs = new ArrayList<>();
        if (result.getExecutionResults() != null && !result.getExecutionResults().isEmpty()) {
            for (ExecutionMetrics metric : result.getExecutionResults()) {
                String output = metric.getOutput();
                outputs.add(output != null ? output : "");
            }
        } else {
            outputs.add(""); // 确保至少有一个空字符串输出
        }
        response.setOutput(outputs);

        // 设置执行信息
        JudgeInfo judgeInfo = new JudgeInfo();

        // 设置状态消息
        if (result.isSuccess()) {
            judgeInfo.setMessage(language + "代码执行成功");
        } else {
            judgeInfo.setMessage(language + "代码执行失败");

            // 尝试从执行结果中获取错误信息
            if (result.getExecutionResults() != null && !result.getExecutionResults().isEmpty()) {
                ExecutionMetrics firstResult = result.getExecutionResults().get(0);
                if (firstResult.getOutput() != null && !firstResult.getOutput().trim().isEmpty()) {
                    judgeInfo.setMessage(language + "代码执行失败: " + firstResult.getOutput().trim());
                }
            }
        }

        // 设置内存使用（转换为KB）
        long memoryKB = result.getMaxMemoryUsed() / 1024;
        judgeInfo.setMemory(memoryKB);

        // 设置执行时间
        judgeInfo.setTime(result.getMaxExecutionTime());

        response.setJudgeInfo(judgeInfo);

        logger.info(language + "执行结果转换完成 - 状态: " + response.getStatus() +
                ", 内存: " + memoryKB + "KB, 时间: " + result.getMaxExecutionTime() + "ms");

        return response;
    }

    /**
     * 构建错误响应
     */
    private ExecuteCodeResponse buildErrorResponse(String errorMessage) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(ExecuteCodeResponse.ExecuteStatus.FAILED);

        // 设置空的输出列表
        response.setOutput(new ArrayList<>());

        // 设置错误判断信息
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(errorMessage != null ? errorMessage : "执行失败");
        judgeInfo.setMemory(0L);
        judgeInfo.setTime(0L);
        response.setJudgeInfo(judgeInfo);

        return response;
    }

    /**
     * 获取支持的编程语言列表
     */
    @GetMapping("/languages")
    public ResponseEntity<List<String>> getSupportedLanguages(
            @RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth) {

        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> languages = new ArrayList<>();
        for (ProgrammingLanguage lang : ProgrammingLanguage.values()) {
            languages.add(lang.name());
        }

        logger.info("返回支持的编程语言列表: " + languages);
        return ResponseEntity.ok(languages);
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
            CompletableFuture.allOf(javaCleanup, pythonCleanup, jsCleanup)
                    .get(60, TimeUnit.SECONDS);

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
    public ResponseEntity<String> getContainerStatus(
            @RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth) {

        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
        }

        try {
            int javaContainers = javaExecutor.getContainerCount();
            int pythonContainers = pythonExecutor.getContainerCount();
            int jsContainers = jsExecutor.getContainerCount();
            int totalContainers = javaContainers + pythonContainers + jsContainers;

            String status = String.format(
                    "=== 沙箱容器状态统计 ===\n" +
                            "Java执行器: %d 个容器\n" +
                            "Python执行器: %d 个容器\n" +
                            "JavaScript执行器: %d 个容器\n" +
                            "容器总计: %d 个\n" +
                            "容器复用状态: 已启用\n" +
                            "支持语言: JAVA, PYTHON, JAVASCRIPT",
                    javaContainers, pythonContainers, jsContainers, totalContainers);

            logger.info("容器状态查询完成，总容器数: " + totalContainers);
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.severe("获取容器状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量执行代码接口 - 支持同时执行多种语言的代码
     */
    @PostMapping("/execute/batch")
    public ResponseEntity<List<ExecuteCodeResponse>> executeBatch(
            @RequestHeader(name = AUTH_REQUEST_HEADER, required = false) String auth,
            @RequestBody List<ExecuteCodeRequest> requests) {

        if (!authenticateRequest(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            logger.info("开始批量执行代码，请求数量: " + requests.size());
            List<ExecuteCodeResponse> responses = new ArrayList<>();

            for (int i = 0; i < requests.size(); i++) {
                ExecuteCodeRequest request = requests.get(i);
                try {
                    logger.info("执行第 " + (i + 1) + " 个请求，语言: " + request.getLanguage());

                    ExecuteCodeResponse response;
                    switch (request.getLanguage()) {
                        case JAVA:
                            response = executeJavaCodeInternal(request);
                            break;
                        case JAVASCRIPT:
                            response = executeJavaScriptCodeInternal(request);
                            break;
                        case PYTHON:
                            response = executePythonCodeInternal(request);
                            break;
                        default:
                            response = buildErrorResponse("不支持的编程语言: " + request.getLanguage());
                            break;
                    }

                    responses.add(response);

                } catch (Exception e) {
                    logger.warning("第 " + (i + 1) + " 个请求执行失败: " + e.getMessage());
                    responses.add(buildErrorResponse("执行失败: " + e.getMessage()));
                }
            }

            logger.info("批量执行完成，成功处理 " + responses.size() + " 个请求");
            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            logger.severe("批量执行出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}