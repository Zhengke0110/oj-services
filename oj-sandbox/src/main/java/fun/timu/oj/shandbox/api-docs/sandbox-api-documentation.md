# 在线判题沙箱 API 文档

本文档详细介绍了沙箱服务的各个API接口，包括请求方法、参数格式以及响应格式，同时提供了不同语言和输入方式的调用示例。

## 目录

- [接口概述](#接口概述)
- [认证方式](#认证方式)
- [接口详情](#接口详情)
  - [服务健康检查](#1-服务健康检查)
  - [统一代码执行接口](#2-统一代码执行接口)
  - [Java 代码执行接口](#3-java-代码执行接口)
  - [JavaScript 代码执行接口](#4-javascript-代码执行接口)
  - [Python 代码执行接口](#5-python-代码执行接口)
- [输入方式说明](#输入方式说明)
  - [命令行参数 (PARAMS)](#命令行参数-params)
  - [测试文件 (FILE)](#测试文件-file)
- [调用示例](#调用示例)
  - [Java 示例](#java-示例)
  - [JavaScript 示例](#javascript-示例)
  - [Python 示例](#python-示例)
- [常见问题](#常见问题)

## 接口概述

沙箱服务提供了多个API接口，用于安全地执行和评估不同编程语言的代码。目前支持的编程语言包括Java、JavaScript和Python。每种语言都有专门的接口，同时也提供了一个统一的接口根据指定的语言类型执行代码。

所有接口均支持以下功能：
- 代码执行
- 命令行参数输入
- 文件输入
- 执行结果获取（包括输出、执行时间、内存使用等）

## 认证方式

所有API请求需要在请求头中添加认证信息：

```
auth: secretKey
```

若未提供认证信息或认证失败，接口将返回`401 Unauthorized`状态码。

## 接口详情

### 1. 服务健康检查

**请求方式**: GET

**URL**: `/api/sandbox/health`

**描述**: 检查服务是否正常运行

**认证**: 无需认证

**响应示例**:
```
ok
```

### 2. 统一代码执行接口

**请求方式**: POST

**URL**: `/api/sandbox/execute`

**描述**: 根据指定的编程语言执行代码

**认证**: 需要提供认证头

**请求体**:
```json
{
  "language": "JAVA",       // 必须，编程语言类型 (JAVA, JAVASCRIPT, PYTHON)
  "code": "String code",    // 必须，要执行的源代码
  "inputs": ["input1", "input2"], // 可选，输入参数/内容列表
  "inputType": "PARAMS",    // 可选，输入类型 (PARAMS 或 FILE)，默认为 PARAMS
  "executionCount": 1       // 可选，执行次数，默认为1
}
```

**响应体**:
```json
{
  "status": "SUCCEED",      // 执行状态: SUCCEED 或 FAILED
  "output": ["输出内容"],    // 执行输出列表
  "judgeInfo": {
    "message": "成功",      // 执行信息
    "time": 100,           // 执行耗时(毫秒)
    "memory": 10240        // 内存占用(KB)
  }
}
```

### 3. Java 代码执行接口

**请求方式**: POST

**URL**: `/api/sandbox/java`

**描述**: 专门用于执行Java代码

**认证**: 需要提供认证头

**请求体**:
```json
{
  "code": "public class Solution {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, World!\");\n  }\n}",
  "inputs": ["arg1", "arg2"],  // 可选
  "inputType": "PARAMS",       // 可选，默认为 PARAMS
  "executionCount": 1          // 可选，默认为1
}
```

**响应体**: 与统一接口相同

### 4. JavaScript 代码执行接口

**请求方式**: POST

**URL**: `/api/sandbox/javascript`

**描述**: 专门用于执行JavaScript代码

**认证**: 需要提供认证头

**请求体**:
```json
{
  "code": "console.log('Hello, World!');",
  "inputs": ["arg1", "arg2"],  // 可选
  "inputType": "PARAMS",       // 可选，默认为 PARAMS
  "executionCount": 1          // 可选，默认为1
}
```

**响应体**: 与统一接口相同

### 5. Python 代码执行接口

**请求方式**: POST

**URL**: `/api/sandbox/python`

**描述**: 专门用于执行Python代码

**认证**: 需要提供认证头

**请求体**:
```json
{
  "code": "print('Hello, World!')",
  "inputs": ["arg1", "arg2"],  // 可选
  "inputType": "PARAMS",       // 可选，默认为 PARAMS
  "executionCount": 1          // 可选，默认为1
}
```

**响应体**: 与统一接口相同

## 输入方式说明

沙箱支持两种代码输入方式：命令行参数和测试文件输入。

### 命令行参数 (PARAMS)

通过命令行参数方式，可以将参数直接传递给程序的入口函数。

各语言获取命令行参数的方式：
- Java: 通过`main`方法的`args`数组
- JavaScript: 通过`process.argv.slice(2)`
- Python: 通过`sys.argv[1:]`

示例代码会获取这些参数并进行相应处理。

### 测试文件 (FILE)

通过测试文件方式，可以将输入内容作为文件内容提供给程序，程序通过标准输入流读取。

各语言读取标准输入的方式：
- Java: 通过`Scanner scanner = new Scanner(System.in)`
- JavaScript: 通过`process.stdin`
- Python: 通过`input()`函数

在这种模式下，`inputs`数组中的每个字符串元素会以换行符连接，作为标准输入提供给程序。

## 调用示例

以下是各种语言和输入方式的详细调用示例。

### Java 示例

#### 示例1：无输入参数

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/java \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
    "code": "public class Solution {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from Docker Sandbox!\");\n        // Calculate Fibonacci number\n        int n = 20;\n        long result = fibonacci(n);\n        System.out.println(\"Fibonacci(\" + n + \") = \" + result);\n    }\n\n    public static long fibonacci(int n) {\n        if (n <= 1) return n;\n        return fibonacci(n-1) + fibonacci(n-2);\n    }\n}"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["Hello from Docker Sandbox!\nFibonacci(20) = 6765"],
  "judgeInfo": {
    "message": "成功",
    "time": 156,
    "memory": 15360
  }
}
```

#### 示例2：命令行参数 (PARAMS)

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/java \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
    "code": "public class Solution {\n    public static void main(String[] args) {\n        System.out.println(\"通过命令行参数传入测试用例\");\n        \n        // 检查参数数量\n        if (args.length < 2) {\n            System.out.println(\"请提供两个整数参数\");\n            return;\n        }\n        \n        try {\n            // 将命令行参数转换为整数\n            int a = Integer.parseInt(args[0]);\n            int b = Integer.parseInt(args[1]);\n            \n            // 计算结果\n            System.out.println(\"输入的数字: a = \" + a + \", b = \" + b);\n            int sum = a + b;\n            System.out.println(\"a + b = \" + sum);\n        } catch (NumberFormatException e) {\n            System.out.println(\"请确保输入的参数是有效的整数\");\n        }\n    }\n}",
    "inputs": ["5", "7"],
    "inputType": "PARAMS"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["通过命令行参数传入测试用例\n输入的数字: a = 5, b = 7\na + b = 12"],
  "judgeInfo": {
    "message": "成功",
    "time": 95,
    "memory": 12288
  }
}
```

#### 示例3：测试文件输入 (FILE)

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/java \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
 "code": "import java.io.BufferedReader;\nimport java.io.FileReader;\nimport java.io.IOException;\n\npublic class Solution {\n    public static void main(String[] args) {\n        System.out.println(\"通过文件读取测试用例\");\n        \n        // 检查参数\n        if (args.length < 1) {\n            System.out.println(\"请提供测试文件路径\");\n            return;\n        }\n        \n        String testFilePath = args[0];\n        try (BufferedReader reader = new BufferedReader(new FileReader(testFilePath))) {\n            // 读取第一行：操作数数量\n            int n = Integer.parseInt(reader.readLine().trim());\n            System.out.println(\"读取了 \" + n + \" 个数字\");\n            \n            // 读取第二行：操作数列表\n            String[] numbers = reader.readLine().trim().split(\"\\\\s+\");\n            if (numbers.length < n) {\n                System.out.println(\"输入数字不足\");\n                return;\n            }\n            \n            // 计算总和\n            int sum = 0;\n            System.out.print(\"输入的数字: \");\n            for (int i = 0; i < n; i++) {\n                int num = Integer.parseInt(numbers[i]);\n                System.out.print(num + (i < n-1 ? \", \" : \"\"));\n                sum += num;\n            }\n            System.out.println();\n            System.out.println(\"总和 = \" + sum);\n            System.out.println(\"平均值 = \" + (double)sum / n);\n            \n        } catch (IOException e) {\n            System.out.println(\"读取文件时出错: \" + e.getMessage());\n        } catch (NumberFormatException e) {\n            System.out.println(\"解析数字时出错: \" + e.getMessage());\n        }\n    }\n}",
    "inputs": [
        "5",
        "10 20 30 40 50"
    ],
    "inputType": "FILE"
  }'
```

**重要说明**: 在FILE模式下，Java代码需要通过`System.in`读取输入，而不是通过参数或文件路径。系统会将`inputs`数组中的每一项作为单独的一行通过标准输入传递给程序。系统会自动处理标准输入的重定向，无需在代码中手动实现这一部分。

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["通过文件读取测试用例\n读取了 5 个数字\n输入的数字: 10, 20, 30, 40, 50\n总和 = 150\n平均值 = 30.0"],
  "judgeInfo": {
    "message": "成功",
    "time": 110,
    "memory": 12800
  }
}
```

### JavaScript 示例

#### 示例1：无输入参数

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/javascript \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
    "code": "// 简单示例\nconsole.log(\"Hello from JavaScript Docker Sandbox!\");\nconsole.log(\"Simple calculation: 2 + 3 =\", 2 + 3);"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["Hello from JavaScript Docker Sandbox!\nSimple calculation: 2 + 3 = 5"],
  "judgeInfo": {
    "message": "成功",
    "time": 45,
    "memory": 8192
  }
}
```

#### 示例2：命令行参数 (PARAMS)

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/javascript \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
    "code": "// 计算两数之和的示例，使用命令行参数\nconsole.log(\"通过命令行参数传入测试用例\");\n\n// 检查参数数量\nif (process.argv.length < 4) {\n  console.log(\"请提供两个整数参数\");\n  process.exit(1);\n}\n\n// Node.js中，process.argv[0]是node，process.argv[1]是脚本文件名\nconst a = parseInt(process.argv[2]);\nconst b = parseInt(process.argv[3]);\n\n// 检查参数是否为有效数字\nif (isNaN(a) || isNaN(b)) {\n  console.log(\"请确保提供的是有效的整数\");\n  process.exit(1);\n}\n\n// 计算并输出结果\nconsole.log(`输入的数字: a = ${a}, b = ${b}`);\nconst sum = a + b;\nconsole.log(`a + b = ${sum}`);",
    "inputs": ["5", "7"],
    "inputType": "PARAMS"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["通过命令行参数传入测试用例\n输入的数字: a = 5, b = 7\na + b = 12"],
  "judgeInfo": {
    "message": "成功",
    "time": 50,
    "memory": 8500
  }
}
```

#### 示例3：测试文件输入 (FILE)

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/javascript \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
  "code": "// 从文件读取测试用例进行处理的示例\nconst fs = require('fs');\n\nconsole.log('通过文件读取测试用例');\n\n// 检查参数\nif (process.argv.length < 3) {\n  console.log('请提供测试文件路径');\n  process.exit(1);\n}\n\n// 获取测试文件路径\nconst testFilePath = process.argv[2];\n\ntry {\n  // 读取文件内容\n  const data = fs.readFileSync(testFilePath, 'utf8');\n  const lines = data.trim().split('\\n');\n  \n  // 第一行：数字的数量\n  const n = parseInt(lines[0]);\n  console.log(`读取了 ${n} 个数字`);\n  \n  // 第二行：数字列表\n  const numbers = lines[1].split(/\\s+/).map(Number);\n  \n  if (numbers.length < n) {\n    console.log('输入数字不足');\n    process.exit(1);\n  }\n  \n  // 计算总和\n  let sum = 0;\n  process.stdout.write('输入的数字: ');\n  \n  for (let i = 0; i < n; i++) {\n    process.stdout.write(numbers[i] + (i < n-1 ? ', ' : ''));\n    sum += numbers[i];\n  }\n  \n  console.log();\n  console.log(`总和 = ${sum}`);\n  console.log(`平均值 = ${sum / n}`);\n  \n} catch (err) {\n  console.log(`读取或处理文件时出错: ${err.message}`);\n  process.exit(1);\n}\n",
    "inputs": [
        "5",
        "10 20 30 40 50"
    ],
    "inputType": "FILE"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["通过文件读取测试用例\n读取了 5 个数字\n输入的数字: 10, 20, 30, 40, 50\n总和 = 150\n平均值 = 30"],
  "judgeInfo": {
    "message": "成功",
    "time": 55,
    "memory": 8800
  }
}
```

### Python 示例

#### 示例1：无输入参数

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/python \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
    "code": "# 计算斐波那契数列\ndef fibonacci(n):\n    if n <= 1:\n        return n\n    return fibonacci(n-1) + fibonacci(n-2)\n\nprint(\"Hello from Python Docker Sandbox!\")\nn = 20\nresult = fibonacci(n)\nprint(f\"Fibonacci({n}) = {result}\")"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["Hello from Python Docker Sandbox!\nFibonacci(20) = 6765"],
  "judgeInfo": {
    "message": "成功",
    "time": 180,
    "memory": 7168
  }
}
```

#### 示例2：命令行参数 (PARAMS)

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/python \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
    "code": "# 计算两数之和的示例，使用命令行参数\nimport sys\n\nprint(\"通过命令行参数传入测试用例\")\n\n# 检查参数数量\nif len(sys.argv) < 3:\n    print(\"请提供两个整数参数\")\n    sys.exit(1)\n\n# 在Python中，sys.argv[0]是脚本名称\ntry:\n    a = int(sys.argv[1])\n    b = int(sys.argv[2])\nexcept ValueError:\n    print(\"请确保提供的是有效的整数\")\n    sys.exit(1)\n\n# 计算并输出结果\nprint(f\"输入的数字: a = {a}, b = {b}\")\nsum_result = a + b\nprint(f\"a + b = {sum_result}\")",
    "inputs": ["5", "7"],
    "inputType": "PARAMS"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": ["通过命令行参数传入测试用例\n输入的数字: a = 5, b = 7\na + b = 12"],
  "judgeInfo": {
    "message": "成功",
    "time": 30,
    "memory": 6400
  }
}
```

#### 示例3：测试文件输入 (FILE)

**请求**:
```bash
curl -X POST http://your-host/api/sandbox/python \
  -H "Content-Type: application/json" \
  -H "auth: secretKey" \
  -d '{
     "code": "# 从文件读取测试用例进行处理的示例\nimport sys\n\nprint('通过文件读取测试用例')\n\n# 检查参数\nif len(sys.argv) < 2:\n    print('请提供测试文件路径')\n    sys.exit(1)\n\n# 获取测试文件路径\ntest_file_path = sys.argv[1]\n\ntry:\n    # 读取文件内容\n    with open(test_file_path, 'r') as file:\n        lines = file.readlines()\n        \n    # 第一行：数字的数量\n    n = int(lines[0].strip())\n    print(f'读取了 {n} 个数字')\n    \n    # 第二行：数字列表\n    numbers = [int(x) for x in lines[1].strip().split()]\n    \n    if len(numbers) < n:\n        print('输入数字不足')\n        sys.exit(1)\n    \n    # 计算总和\n    total_sum = 0\n    print('输入的数字:', end=' ')\n    \n    for i in range(n):\n        print(numbers[i], end=', ' if i < n-1 else '')\n        total_sum += numbers[i]\n    \n    print('\\n总和 =', total_sum)\n    print('平均值 =', total_sum / n)\n    \nexcept Exception as e:\n    print(f'读取或处理文件时出错: {e}')\n    sys.exit(1)",
    "inputs": [
        "5",
        "10 20 30 40 50"
    ],
    "inputType": "FILE"
  }'
```

**响应**:
```json
{
  "status": "SUCCEED",
  "output": [
    "通过文件读取测试用例\n读取了 5 个数字\n输入的数字: 10, 20, 30, 40, 50\n总和 = 150\n平均值 = 30.0"
  ],
  "judgeInfo": {
    "message": "成功",
    "time": 35,
    "memory": 6500
  }
}
```

## 常见问题

### Q1: 为什么我的代码执行失败？
**A**: 代码执行失败可能有多种原因，包括但不限于：
- 代码语法错误
- 运行时错误
- 内存超限
- 执行时间超限
- 沙箱环境限制

查看响应中的`judgeInfo.message`字段可获取更具体的错误信息。

### Q2: 代码执行时间和内存使用量是如何计算的？
**A**: 沙箱系统会测量代码的实际执行时间(毫秒)和内存占用(KB)。对于多次执行的情况，响应中会返回最大值。

### Q3: 有哪些限制？
**A**: 出于安全考虑，沙箱环境对代码执行有一定的限制：
- 禁止访问文件系统(除指定的测试文件外)
- 禁止网络访问
- 内存使用限制
- 执行时间限制
- 禁止执行系统命令

### Q4: 如何处理需要大量输入数据的代码？
**A**: 对于需要处理大量输入数据的代码，建议使用"FILE"输入类型，并将输入数据按行组织在`inputs`数组中。

### Q5: 统一接口和语言特定接口有什么区别？
**A**: 统一接口(`/api/sandbox/execute`)需要在请求中指定语言类型，而语言特定接口(`/api/sandbox/java`, `/api/sandbox/javascript`, `/api/sandbox/python`)不需要。功能上二者是等价的。
