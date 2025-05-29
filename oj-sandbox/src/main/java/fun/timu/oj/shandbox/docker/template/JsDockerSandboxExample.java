package fun.timu.oj.shandbox.docker.template;

import fun.timu.oj.shandbox.docker.executor.JavaScriptDockerExecutor;
import fun.timu.oj.shandbox.docker.entity.ExecutionResult;
import fun.timu.oj.shandbox.docker.entity.ExecutionMetrics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class JsDockerSandboxExample {
    public static void main(String[] args) {
        SpringApplication.run(JsDockerSandboxExample.class, args);
    }

    @Component
    public class SandboxRunner implements CommandLineRunner {
        @Override
        public void run(String... args) throws Exception {
            System.out.println("开始JavaScript Docker沙箱示例...");

            // 创建执行器实例，设置为不强制拉取镜像(仅在镜像不存在时才拉取)
            JavaScriptDockerExecutor executor = new JavaScriptDockerExecutor(false);

            // 示例1: 简单执行，无参数
            runSimpleExample(executor);

            // 示例2: 使用命令行参数
            runWithArgsExample(executor);

        }

        private void runSimpleExample(JavaScriptDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例1: 简单控制台输出 =====");

            // 更简单的JavaScript代码示例，减少复杂度
            String jsCode = "// 简单示例\n" + "console.log('Hello from JavaScript Docker Sandbox!');\n" + "console.log('Simple calculation: 2 + 3 =', 2 + 3);\n";

            String expectedOutput = "Hello from JavaScript Docker Sandbox!\nSimple calculation: 2 + 3 = 5";
            int executionCount = 1; // 先只执行一次进行测试

            try {
                ExecutionResult result = executor.executeJavaScriptCode(jsCode, expectedOutput, executionCount);

                printExecutionResults(result);
            } catch (Exception e) {
                System.err.println("执行代码时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void runWithArgsExample(JavaScriptDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例2: 通过命令行参数传入测试用例 =====");

            // 通过命令行参数计算两数之和的JS代码
            String jsCode = "// 计算两数之和的示例，使用命令行参数\n" + "console.log('通过命令行参数传入测试用例');\n" + "\n" + "// 检查参数数量\n" + "if (process.argv.length < 4) {\n" + "  console.log('请提供两个整数参数');\n" + "  process.exit(1);\n" + "}\n" + "\n" + "// Node.js中，process.argv[0]是node，process.argv[1]是脚本文件名\n" + "const a = parseInt(process.argv[2]);\n" + "const b = parseInt(process.argv[3]);\n" + "\n" + "// 检查参数是否为有效数字\n" + "if (isNaN(a) || isNaN(b)) {\n" + "  console.log('请确保提供的是有效的整数');\n" + "  process.exit(1);\n" + "}\n" + "\n" + "// 计算并输出结果\n" + "console.log(`输入的数字: a = ${a}, b = ${b}`);\n" + "const sum = a + b;\n" + "console.log(`a + b = ${sum}`);\n";

            // 命令行参数
            String[] testArgs = {"5", "7"};

            // 期望的输出
            String expectedOutput = "通过命令行参数传入测试用例\n" + "输入的数字: a = 5, b = 7\n" + "a + b = 12";

            int executionCount = 1;

            try {
                ExecutionResult result = executor.executeJavaScriptCodeWithArgs(jsCode, testArgs, expectedOutput, executionCount);

                printExecutionResults(result);

                // 测试不同输入
                System.out.println("\n--- 测试不同输入 ---");
                String[] newArgs = {"10", "20"};
                String newExpectedOutput = "通过命令行参数传入测试用例\n" + "输入的数字: a = 10, b = 20\n" + "a + b = 30";

                result = executor.executeJavaScriptCodeWithArgs(jsCode, newArgs, newExpectedOutput, 1);
                printExecutionResults(result);

            } catch (Exception e) {
                System.err.println("执行代码(带参数)时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void printExecutionResults(ExecutionResult result) {
            System.out.println("\n--- 执行结果 ---");
            System.out.println("成功: " + result.isSuccess());
            System.out.println("输出匹配: " + result.isOutputMatched());
            System.out.println("平均执行时间: " + result.getAverageExecutionTime() + " ms");
            System.out.println("平均内存使用: " + String.format("%.2f", result.getAverageMemoryUsed() / 1024.0) + " KB");
            System.out.println("最长执行时间: " + result.getMaxExecutionTime() + " ms");
            System.out.println("最大内存使用: " + String.format("%.2f", result.getMaxMemoryUsed() / 1024.0) + " KB");
            System.out.println("注意：容器将在应用结束时统一清理以提升性能");

            System.out.println("\n--- 单次执行详情 ---");
            for (int i = 0; i < result.getExecutionResults().size(); i++) {
                ExecutionMetrics metrics = result.getExecutionResults().get(i);
                System.out.println("执行 #" + (i + 1));
                System.out.println("  状态: " + metrics.getStatus());
                System.out.println("  输出: " + metrics.getOutput());
                System.out.println("  执行时间: " + metrics.getExecutionTime() + " ms");
                System.out.println("  内存使用: " + (metrics.getMemoryUsed() / 1024) + " KB");
                System.out.println("  输出匹配: " + metrics.isOutputMatched());
                System.out.println();
            }
        }
    }
}
