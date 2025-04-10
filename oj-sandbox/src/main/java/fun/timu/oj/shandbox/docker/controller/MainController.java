package fun.timu.oj.shandbox.docker.controller;

import fun.timu.oj.shandbox.docker.executor.AbstractDockerExecutor;
import fun.timu.oj.shandbox.docker.executor.DockerCodeExecutor;
import fun.timu.oj.shandbox.docker.executor.JavaScriptDockerExecutor;
import fun.timu.oj.shandbox.docker.executor.PythonDockerExecutor;
import fun.timu.oj.shandbox.docker.interfaces.ExecuteCodeRequest;
import fun.timu.oj.shandbox.docker.interfaces.ExecuteCodeResponse;
import fun.timu.oj.shandbox.docker.interfaces.JudgeInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
            // 判断输入类型：命令行参数或测试文件
            if (request.getInputType() == null || request.getInputType().equals("PARAMS")) {
                // 使用命令行参数
                String[] args = request.getInputs().toArray(new String[0]);
                result = javaExecutor.executeJavaCodeWithArgs(request.getCode(), args, null, // 不校验期望输出
                        Math.max(1, request.getExecutionCount()));
            } else {
                // 使用测试文件
                String testContent = String.join("\n", request.getInputs());
                result = javaExecutor.executeJavaCodeWithTestFile(request.getCode(), testContent, null, // 不校验期望输出
                        Math.max(1, request.getExecutionCount()));
            }
        } else {
            // 无输入的代码执行
            result = javaExecutor.executeJavaCode(request.getCode(), null, // 不校验期望输出
                    Math.max(1, request.getExecutionCount()));
        }

        return convertToExecuteCodeResponse(result);
    }

    /**
     * 内部方法：执行JavaScript代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executeJavaScriptCodeInternal(ExecuteCodeRequest request) throws Exception {
        JavaScriptDockerExecutor.JSExecutionResult result;

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 判断输入类型：命令行参数或测试文件
            if (request.getInputType() == null || request.getInputType().equals("PARAMS")) {
                // 使用命令行参数
                String[] args = request.getInputs().toArray(new String[0]);
                result = jsExecutor.executeJavaScriptCodeWithArgs(request.getCode(), args, null, // 不校验期望输出
                        Math.max(1, request.getExecutionCount()));
            } else {
                // 使用测试文件
                String testContent = String.join("\n", request.getInputs());
                result = jsExecutor.executeJavaScriptCodeWithTestFile(request.getCode(), testContent, null, // 不校验期望输出
                        Math.max(1, request.getExecutionCount()));
            }
        } else {
            // 无输入的代码执行
            result = jsExecutor.executeJavaScriptCode(request.getCode(), null, // 不校验期望输出
                    Math.max(1, request.getExecutionCount()));
        }

        return convertToExecuteCodeResponse(result);
    }

    /**
     * 内部方法：执行Python代码并返回接口定义的响应对象
     */
    private ExecuteCodeResponse executePythonCodeInternal(ExecuteCodeRequest request) throws Exception {
        PythonDockerExecutor.PythonExecutionResult result;

        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            // 判断输入类型：命令行参数或测试文件
            if (request.getInputType() == null || request.getInputType().equals("PARAMS")) {
                // 使用命令行参数
                String[] args = request.getInputs().toArray(new String[0]);
                result = pythonExecutor.executePythonCodeWithArgs(request.getCode(), args, null, // 不校验期望输出
                        Math.max(1, request.getExecutionCount()));
            } else {
                // 使用测试文件
                String testContent = String.join("\n", request.getInputs());
                result = pythonExecutor.executePythonCodeWithTestFile(request.getCode(), testContent, null, // 不校验期望输出
                        Math.max(1, request.getExecutionCount()));
            }
        } else {
            // 无输入的代码执行
            result = pythonExecutor.executePythonCode(request.getCode(), null, // 不校验期望输出
                    Math.max(1, request.getExecutionCount()));
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
     */
    @PreDestroy
    public void cleanup() {
        try {
            // 清理执行器资源
            javaExecutor.cleanup();
            pythonExecutor.cleanup();
            jsExecutor.cleanup();
        } catch (Exception e) {
            logger.severe("清理资源时出错: " + e.getMessage());
        }
    }
}
