# Docker 代码执行沙箱

基于Docker容器的多语言代码执行沙箱系统，支持Java、JavaScript和Python代码的安全隔离执行。

## 项目概述

本项目提供一个安全的代码执行环境，通过Docker容器技术隔离待执行的代码，防止恶意代码对宿主系统造成损害。系统支持多种代码执行模式，包括基本代码执行、命令行参数传递和测试文件输入，适用于在线评测系统(OJ)、编程教育平台和代码测试环境等场景。

## 系统要求

- Java 8 或更高版本
- Docker 引擎 (已安装并正在运行)
- Maven 或 Gradle (用于构建)
- Docker Java API 依赖

## 核心功能

- **多语言支持**：目前支持Java、JavaScript(Node.js)和Python语言的代码执行
- **资源限制**：限制内存使用、CPU使用和执行时间
- **安全隔离**：使用Docker容器技术，提供隔离的执行环境
- **多种输入模式**：
    - 无参数代码执行
    - 命令行参数传递
    - 测试用例文件读取
- **详细指标收集**：
    - 执行时间
    - 内存使用量
    - 执行状态
    - 输出结果
- **自动镜像管理**：
    - 检查本地是否存在需要的Docker镜像
    - 自动拉取缺失的镜像
    - 支持强制刷新镜像

## 模块结构

该项目包含三个主要执行器类，它们遵循相同的设计模式但针对不同的编程语言进行了优化：

### DockerCodeExecutor (Java)

用于在Docker容器中执行Java代码。使用OpenJDK镜像，支持编译和执行Java程序。

### JavaScriptDockerExecutor (Node.js)

用于在Docker容器中执行JavaScript代码。使用Node.js轻量级镜像，提供JavaScript运行环境。

### PythonDockerExecutor (Python)

用于在Docker容器中执行Python代码。使用Python官方镜像，提供Python解释器环境。

## 使用指南

### 基本代码执行

```java
// 创建执行器实例
PythonDockerExecutor executor = new PythonDockerExecutor(false);

// Python代码示例
String pythonCode = "print('Hello, Docker Sandbox!')";

// 执行代码
PythonDockerExecutor.ExecutionResult result = 
    executor.executePythonCode(pythonCode, "Hello, Docker Sandbox!", 1);

// 输出结果
System.out.println("执行成功: " + result.isSuccess());
System.out.println("输出匹配: " + result.isOutputMatched());
System.out.println("执行时间: " + result.getAverageExecutionTime() + " ms");
System.out.println("内存使用: " + result.getAverageMemoryUsed() + " bytes");
```

### 使用命令行参数

```java
// JavaScript代码示例 (接收两个数字并计算和)
String jsCode = "const a = parseInt(process.argv[2]);\n" +
               "const b = parseInt(process.argv[3]);\n" +
               "console.log(`Sum: ${a + b}`);\n";

// 命令行参数
String[] args = {"5", "7"};

// 执行代码
JavaScriptDockerExecutor executor = new JavaScriptDockerExecutor(false);
JavaScriptDockerExecutor.ExecutionResult result = 
    executor.executeJavaScriptCodeWithArgs(jsCode, args, "Sum: 12", 1);
```

### 使用测试文件

```java
// Java代码示例 (从文件读取数字并计算总和)
String javaCode = "import java.io.File;\n" +
                 "import java.util.Scanner;\n\n" +
                 "public class Solution {\n" +
                 "    public static void main(String[] args) throws Exception {\n" +
                 "        File file = new File(args[0]);\n" +
                 "        Scanner sc = new Scanner(file);\n" +
                 "        int sum = 0;\n" +
                 "        while (sc.hasNextInt()) {\n" +
                 "            sum += sc.nextInt();\n" +
                 "        }\n" +
                 "        System.out.println(\"Sum: \" + sum);\n" +
                 "    }\n" +
                 "}";

// 测试文件内容
String testContent = "10 20 30 40 50";

// 执行代码
DockerCodeExecutor executor = new DockerCodeExecutor(false);
DockerCodeExecutor.ExecutionResult result = 
    executor.executeJavaCodeWithTestFile(javaCode, testContent, "Sum: 150", 1);
```

## 安全特性

- **网络隔离**：容器使用 `--network=none` 运行，防止网络访问
- **资源限制**：限制内存和CPU使用，防止资源耗尽攻击
- **执行超时**：设置最大执行时间，防止无限循环
- **文件系统隔离**：使用Docker卷挂载，限制文件系统访问
- **容器自动清理**：执行完成后自动清理容器，防止资源泄漏

## 高级配置

### 构造器选项

所有执行器都支持配置是否每次执行都强制拉取镜像：

```java
// 默认不强制拉取镜像
PythonDockerExecutor executor = new PythonDockerExecutor(false);

// 每次执行都强制拉取镜像
PythonDockerExecutor executorWithPull = new PythonDockerExecutor(true);
```

### 执行方法选项

执行方法支持指定是否强制拉取镜像：

```java
// 使用构造器中的默认设置
executor.executePythonCode(code, expectedOutput, executionCount);

// 覆盖默认设置，强制拉取镜像
executor.executePythonCode(code, expectedOutput, executionCount, true);
```

## 常见问题

### 镜像拉取失败

如果出现镜像拉取失败，请检查：

1. Docker服务是否正在运行
2. 网络连接是否正常
3. 是否有足够的存储空间
4. 是否有正确的权限访问Docker API

### 执行超时

如果代码执行超时：

1. 检查代码中是否有无限循环
2. 考虑增加执行超时设置
3. 对于复杂任务，可能需要调整资源限制

### 内存限制错误

如果代码执行因内存不足而失败：

1. 检查代码是否有内存泄漏
2. 增加内存限制设置
3. 优化代码降低内存使用

## 许可证

MIT License

## 贡献指南

欢迎提交Pull Request和Issue来完善本项目。
