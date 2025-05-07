package fun.timu.oj.shandbox.docker.template;

import fun.timu.oj.shandbox.docker.executor.PythonDockerExecutor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class PythonDockerSandboxExample {
    public static void main(String[] args) {
        SpringApplication.run(PythonDockerSandboxExample.class, args);
    }

    //    @Component
    public class SandboxRunner implements CommandLineRunner {
        @Override
        public void run(String... args) throws Exception {
            System.out.println("开始Python Docker沙箱示例...");

            // 创建执行器实例，强制拉取镜像以确保环境正确
            PythonDockerExecutor executor = new PythonDockerExecutor(false);

            // 示例1: 简单执行
            runSimpleExample(executor);

            // 示例2: 通过命令行参数传入测试用例
            runWithArgsExample(executor);

            // 示例3: 通过文件读取测试用例
            runWithTestFileExample(executor);
        }

        private void runSimpleExample(PythonDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例: 斐波那契计算 =====");

            // Python代码示例
            String pythonCode = "# 计算斐波那契数列\n" + "def fibonacci(n):\n" + "    if n <= 1:\n" + "        return n\n" + "    return fibonacci(n-1) + fibonacci(n-2)\n" + "\n" + "print('Hello from Python Docker Sandbox!')\n" + "n = 20\n" + "result = fibonacci(n)\n" + "print(f'Fibonacci({n}) = {result}')";

            String expectedOutput = "Hello from Python Docker Sandbox!\nFibonacci(20) = 6765";
            int executionCount = 2;

            try {
                // 第一次执行时强制拉取镜像，确保环境正确
                PythonDockerExecutor.ExecutionResult result = executor.executePythonCode(pythonCode, expectedOutput, executionCount, true);

                printExecutionResults(result);
            } catch (Exception e) {
                System.err.println("执行代码时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void runWithArgsExample(PythonDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例2: 通过命令行参数传入测试用例 =====");

            // 通过命令行参数计算两数之和的Python代码
            String pythonCode = "# 计算两数之和的示例，使用命令行参数\n" + "import sys\n" + "\n" + "print('通过命令行参数传入测试用例')\n" + "\n" + "# 检查参数数量\n" + "if len(sys.argv) < 3:\n" + "    print('请提供两个整数参数')\n" + "    sys.exit(1)\n" + "\n" + "# 在Python中，sys.argv[0]是脚本名称\n" + "try:\n" + "    a = int(sys.argv[1])\n" + "    b = int(sys.argv[2])\n" + "except ValueError:\n" + "    print('请确保提供的是有效的整数')\n" + "    sys.exit(1)\n" + "\n" + "# 计算并输出结果\n" + "print(f'输入的数字: a = {a}, b = {b}')\n" + "sum_result = a + b\n" + "print(f'a + b = {sum_result}')";

            // 命令行参数
            String[] testArgs = {"5", "7"};

            // 期望的输出
            String expectedOutput = "通过命令行参数传入测试用例\n" + "输入的数字: a = 5, b = 7\n" + "a + b = 12";

            int executionCount = 1;

            try {
                PythonDockerExecutor.ExecutionResult result = executor.executePythonCodeWithArgs(pythonCode, testArgs, expectedOutput, executionCount, true);

                printExecutionResults(result);

                // 测试不同输入
                System.out.println("\n--- 测试不同输入 ---");
                String[] newArgs = {"10", "20"};
                String newExpectedOutput = "通过命令行参数传入测试用例\n" + "输入的数字: a = 10, b = 20\n" + "a + b = 30";

                result = executor.executePythonCodeWithArgs(pythonCode, newArgs, newExpectedOutput, 1);
                printExecutionResults(result);

            } catch (Exception e) {
                System.err.println("执行代码(带参数)时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void runWithTestFileExample(PythonDockerExecutor executor) throws Exception {
            System.out.println("\n\n===== 示例3: 通过文件读取测试用例 =====");

            // 从文件读取输入的Python代码
            String pythonCode = "# 从文件读取测试用例进行处理的示例\n" + "import sys\n" + "\n" + "print('通过文件读取测试用例')\n" + "\n" + "# 检查参数\n" + "if len(sys.argv) < 2:\n" + "    print('请提供测试文件路径')\n" + "    sys.exit(1)\n" + "\n" + "# 获取测试文件路径\n" + "test_file_path = sys.argv[1]\n" + "\n" + "try:\n" + "    # 读取文件内容\n" + "    with open(test_file_path, 'r') as file:\n" + "        lines = file.readlines()\n" + "        \n" + "    # 第一行：数字的数量\n" + "    n = int(lines[0].strip())\n" + "    print(f'读取了 {n} 个数字')\n" + "    \n" + "    # 第二行：数字列表\n" + "    numbers = [int(x) for x in lines[1].strip().split()]\n" + "    \n" + "    if len(numbers) < n:\n" + "        print('输入数字不足')\n" + "        sys.exit(1)\n" + "    \n" + "    # 计算总和\n" + "    total_sum = 0\n" + "    print('输入的数字:', end=' ')\n" + "    \n" + "    for i in range(n):\n" + "        print(numbers[i], end=', ' if i < n-1 else '')\n" + "        total_sum += numbers[i]\n" + "    \n" + "    print('\\n总和 =', total_sum)\n" + "    print('平均值 =', total_sum / n)\n" + "    \n" + "except Exception as e:\n" + "    print(f'读取或处理文件时出错: {e}')\n" + "    sys.exit(1)";

            // 测试文件内容
            String testFileContent = "5\n10 20 30 40 50";

            // 期望的输出
            String expectedOutput = "通过文件读取测试用例\n" + "读取了 5 个数字\n" + "输入的数字: 10, 20, 30, 40, 50\n" + "总和 = 150\n" + "平均值 = 30.0";

            int executionCount = 1;

            try {
                PythonDockerExecutor.ExecutionResult result = executor.executePythonCodeWithTestFile(pythonCode, testFileContent, expectedOutput, executionCount);

                printExecutionResults(result);

                // 尝试不同的测试用例
                System.out.println("\n--- 测试不同输入 ---");
                String newTestFileContent = "3\n100 200 300";
                String newExpectedOutput = "通过文件读取测试用例\n" + "读取了 3 个数字\n" + "输入的数字: 100, 200, 300\n" + "总和 = 600\n" + "平均值 = 200.0";

                result = executor.executePythonCodeWithTestFile(pythonCode, newTestFileContent, newExpectedOutput, 1);
                printExecutionResults(result);

            } catch (Exception e) {
                System.err.println("执行代码(带测试文件)时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void printExecutionResults(PythonDockerExecutor.ExecutionResult result) {
            System.out.println("\n--- 执行结果 ---");
            System.out.println("成功: " + result.isSuccess());
            System.out.println("输出匹配: " + result.isOutputMatched());
            System.out.println("平均执行时间: " + result.getAverageExecutionTime() + " ms");
            System.out.println("平均内存使用: " + (result.getAverageMemoryUsed() / 1024) + " KB");
            System.out.println("最长执行时间: " + result.getMaxExecutionTime() + " ms");
            System.out.println("最大内存使用: " + (result.getMaxMemoryUsed() / 1024) + " KB");

            System.out.println("\n--- 单次执行详情 ---");
            for (int i = 0; i < result.getExecutionResults().size(); i++) {
                PythonDockerExecutor.ExecutionMetrics metrics = result.getExecutionResults().get(i);
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
