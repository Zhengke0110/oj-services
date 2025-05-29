package fun.timu.oj.shandbox.docker.template;

import fun.timu.oj.shandbox.docker.executor.DockerCodeExecutor;
import fun.timu.oj.shandbox.docker.entity.ExecutionResult;
import fun.timu.oj.shandbox.docker.entity.ExecutionMetrics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class DockerSandboxExample {
    public static void main(String[] args) {
        SpringApplication.run(DockerSandboxExample.class, args);
    }

    @Component
    public class SandboxRunner implements CommandLineRunner {
        @Override
        public void run(String... args) throws Exception {
            System.out.println("Starting Java Docker Code Sandbox Example...");

            DockerCodeExecutor executor = new DockerCodeExecutor();

            // 示例1: 不需要输入的代码
            runFibonacciExample(executor);

            // 示例2: 通过命令行参数传入测试用例
            runCalculatorWithArgsExample(executor);

            // 示例3: 通过文件读取测试用例
//            runCalculatorWithTestFileExample(executor);
        }

        private void runFibonacciExample(DockerCodeExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例1: 斐波那契计算 (无输入) =====");

            // Sample Java code to execute
            String javaCode = "public class Solution {\n" + "    public static void main(String[] args) {\n" + "        System.out.println(\"Hello from Docker Sandbox!\");\n" + "        // Calculate Fibonacci number\n" + "        int n = 20;\n" + "        long result = fibonacci(n);\n" + "        System.out.println(\"Fibonacci(\" + n + \") = \" + result);\n" + "    }\n" + "\n" + "    public static long fibonacci(int n) {\n" + "        if (n <= 1) return n;\n" + "        return fibonacci(n-1) + fibonacci(n-2);\n" + "    }\n" + "}";

            String expectedOutput = "Hello from Docker Sandbox!\nFibonacci(20) = 6765";
            int executionCount = 2;

            try {
                ExecutionResult result = executor.executeJavaCode(javaCode, expectedOutput, executionCount);

                printExecutionResults(result);
            } catch (Exception e) {
                System.err.println("Error executing code: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void runCalculatorWithArgsExample(DockerCodeExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例2: 通过命令行参数传入测试用例 =====");

            // 计算两数之和的代码，从命令行参数获取输入
            String javaCode = "public class Solution {\n" + "    public static void main(String[] args) {\n" + "        System.out.println(\"通过命令行参数传入测试用例\");\n" + "        \n" + "        // 检查参数数量\n" + "        if (args.length < 2) {\n" + "            System.out.println(\"请提供两个整数参数\");\n" + "            return;\n" + "        }\n" + "        \n" + "        try {\n" + "            // 将命令行参数转换为整数\n" + "            int a = Integer.parseInt(args[0]);\n" + "            int b = Integer.parseInt(args[1]);\n" + "            \n" + "            // 计算结果\n" + "            System.out.println(\"输入的数字: a = \" + a + \", b = \" + b);\n" + "            int sum = a + b;\n" + "            System.out.println(\"a + b = \" + sum);\n" + "        } catch (NumberFormatException e) {\n" + "            System.out.println(\"请确保输入的参数是有效的整数\");\n" + "        }\n" + "    }\n" + "}";

            // 命令行参数
            String[] testArgs = {"5", "7"};

            // 期望的输出
            String expectedOutput = "通过命令行参数传入测试用例\n" + "输入的数字: a = 5, b = 7\n" + "a + b = 12";

            int executionCount = 2;

            try {
                ExecutionResult result = executor.executeJavaCodeWithArgs(javaCode, testArgs, expectedOutput, executionCount);

                printExecutionResults(result);

                // 尝试不同的输入参数
                System.out.println("\n--- 测试不同输入 ---");
                String[] newArgs = {"10", "20"};
                String newExpectedOutput = "通过命令行参数传入测试用例\n" + "输入的数字: a = 10, b = 20\n" + "a + b = 30";

                result = executor.executeJavaCodeWithArgs(javaCode, newArgs, newExpectedOutput, 1);
                printExecutionResults(result);

            } catch (Exception e) {
                System.err.println("Error executing code with args: " + e.getMessage());
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
