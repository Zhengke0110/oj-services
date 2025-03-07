package fun.timu.oj.shandbox.docker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class JsDockerSandboxExample {
    public static void main(String[] args) {
        SpringApplication.run(JsDockerSandboxExample.class, args);
    }

    //    @Component
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

            // 示例3: 使用文件输入
            runWithFileExample(executor);
        }

        private void runSimpleExample(JavaScriptDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例1: 简单控制台输出 =====");

            // 更简单的JavaScript代码示例，减少复杂度
            String jsCode = "// 简单示例\n" +
                    "console.log('Hello from JavaScript Docker Sandbox!');\n" +
                    "console.log('Simple calculation: 2 + 3 =', 2 + 3);\n";

            String expectedOutput = "Hello from JavaScript Docker Sandbox!\nSimple calculation: 2 + 3 = 5";
            int executionCount = 1; // 先只执行一次进行测试

            try {
                JavaScriptDockerExecutor.ExecutionResult result = executor.executeJavaScriptCode(jsCode, expectedOutput, executionCount);

                printExecutionResults(result);
            } catch (Exception e) {
                System.err.println("执行代码时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void runWithArgsExample(JavaScriptDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例2: 通过命令行参数传入测试用例 =====");

            // 通过命令行参数计算两数之和的JS代码
            String jsCode = "// 计算两数之和的示例，使用命令行参数\n" +
                    "console.log('通过命令行参数传入测试用例');\n" +
                    "\n" +
                    "// 检查参数数量\n" +
                    "if (process.argv.length < 4) {\n" +
                    "  console.log('请提供两个整数参数');\n" +
                    "  process.exit(1);\n" +
                    "}\n" +
                    "\n" +
                    "// Node.js中，process.argv[0]是node，process.argv[1]是脚本文件名\n" +
                    "const a = parseInt(process.argv[2]);\n" +
                    "const b = parseInt(process.argv[3]);\n" +
                    "\n" +
                    "// 检查参数是否为有效数字\n" +
                    "if (isNaN(a) || isNaN(b)) {\n" +
                    "  console.log('请确保提供的是有效的整数');\n" +
                    "  process.exit(1);\n" +
                    "}\n" +
                    "\n" +
                    "// 计算并输出结果\n" +
                    "console.log(`输入的数字: a = ${a}, b = ${b}`);\n" +
                    "const sum = a + b;\n" +
                    "console.log(`a + b = ${sum}`);\n";

            // 命令行参数
            String[] testArgs = {"5", "7"};

            // 期望的输出
            String expectedOutput = "通过命令行参数传入测试用例\n" +
                    "输入的数字: a = 5, b = 7\n" +
                    "a + b = 12";

            int executionCount = 1;

            try {
                JavaScriptDockerExecutor.ExecutionResult result = executor.executeJavaScriptCodeWithArgs(
                        jsCode, testArgs, expectedOutput, executionCount);

                printExecutionResults(result);

                // 测试不同输入
                System.out.println("\n--- 测试不同输入 ---");
                String[] newArgs = {"10", "20"};
                String newExpectedOutput = "通过命令行参数传入测试用例\n" +
                        "输入的数字: a = 10, b = 20\n" +
                        "a + b = 30";

                result = executor.executeJavaScriptCodeWithArgs(jsCode, newArgs, newExpectedOutput, 1);
                printExecutionResults(result);

            } catch (Exception e) {
                System.err.println("执行代码(带参数)时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void runWithFileExample(JavaScriptDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例3: 通过文件读取测试用例 =====");

            // 从文件读取输入的JavaScript代码
            String jsCode = "// 从文件读取测试用例进行处理的示例\n" +
                    "const fs = require('fs');\n" +
                    "\n" +
                    "console.log('通过文件读取测试用例');\n" +
                    "\n" +
                    "// 检查参数\n" +
                    "if (process.argv.length < 3) {\n" +
                    "  console.log('请提供测试文件路径');\n" +
                    "  process.exit(1);\n" +
                    "}\n" +
                    "\n" +
                    "// 获取测试文件路径\n" +
                    "const testFilePath = process.argv[2];\n" +
                    "\n" +
                    "try {\n" +
                    "  // 读取文件内容\n" +
                    "  const data = fs.readFileSync(testFilePath, 'utf8');\n" +
                    "  const lines = data.trim().split('\\n');\n" +
                    "  \n" +
                    "  // 第一行：数字的数量\n" +
                    "  const n = parseInt(lines[0]);\n" +
                    "  console.log(`读取了 ${n} 个数字`);\n" +
                    "  \n" +
                    "  // 第二行：数字列表\n" +
                    "  const numbers = lines[1].split(/\\s+/).map(Number);\n" +
                    "  \n" +
                    "  if (numbers.length < n) {\n" +
                    "    console.log('输入数字不足');\n" +
                    "    process.exit(1);\n" +
                    "  }\n" +
                    "  \n" +
                    "  // 计算总和\n" +
                    "  let sum = 0;\n" +
                    "  process.stdout.write('输入的数字: ');\n" +
                    "  \n" +
                    "  for (let i = 0; i < n; i++) {\n" +
                    "    process.stdout.write(numbers[i] + (i < n-1 ? ', ' : ''));\n" +
                    "    sum += numbers[i];\n" +
                    "  }\n" +
                    "  \n" +
                    "  console.log();\n" +
                    "  console.log(`总和 = ${sum}`);\n" +
                    "  console.log(`平均值 = ${sum / n}`);\n" +
                    "  \n" +
                    "} catch (err) {\n" +
                    "  console.log(`读取或处理文件时出错: ${err.message}`);\n" +
                    "  process.exit(1);\n" +
                    "}\n";

            // 测试文件内容
            String testFileContent = "5\n10 20 30 40 50";

            // 期望的输出
            String expectedOutput = "通过文件读取测试用例\n" +
                    "读取了 5 个数字\n" +
                    "输入的数字: 10, 20, 30, 40, 50\n" +
                    "总和 = 150\n" +
                    "平均值 = 30";

            int executionCount = 1;

            try {
                JavaScriptDockerExecutor.ExecutionResult result = executor.executeJavaScriptCodeWithTestFile(
                        jsCode, testFileContent, expectedOutput, executionCount);

                printExecutionResults(result);

                // 尝试不同的测试用例
                System.out.println("\n--- 测试不同输入 ---");
                String newTestFileContent = "3\n100 200 300";
                String newExpectedOutput = "通过文件读取测试用例\n" +
                        "读取了 3 个数字\n" +
                        "输入的数字: 100, 200, 300\n" +
                        "总和 = 600\n" +
                        "平均值 = 200";

                result = executor.executeJavaScriptCodeWithTestFile(jsCode, newTestFileContent, newExpectedOutput, executionCount);
                printExecutionResults(result);

            } catch (Exception e) {
                System.err.println("执行代码(带测试文件)时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void printExecutionResults(JavaScriptDockerExecutor.ExecutionResult result) {
            System.out.println("\n--- 执行结果 ---");
            System.out.println("成功: " + result.isSuccess());
            System.out.println("输出匹配: " + result.isOutputMatched());
            System.out.println("平均执行时间: " + result.getAverageExecutionTime() + " ms");
            System.out.println("平均内存使用: " + (result.getAverageMemoryUsed() / 1024) + " KB");
            System.out.println("最长执行时间: " + result.getMaxExecutionTime() + " ms");
            System.out.println("最大内存使用: " + (result.getMaxMemoryUsed() / 1024) + " KB");

            System.out.println("\n--- 单次执行详情 ---");
            for (int i = 0; i < result.getExecutionResults().size(); i++) {
                JavaScriptDockerExecutor.ExecutionMetrics metrics = result.getExecutionResults().get(i);
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
