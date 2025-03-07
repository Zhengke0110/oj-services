# Online Judge Services (OJ-Services)

一个完整的在线评测系统(Online Judge)服务集合，提供代码执行、测评和管理功能。

## 项目概述

OJ-Services 是一个模块化的在线评测系统，旨在为编程教育、算法竞赛和技术面试提供可靠、安全的代码评测环境。系统通过微服务架构设计，提供代码沙箱执行、测评调度、用户管理、问题管理等核心功能。

## 系统架构

本项目包含以下主要组件：

- **oj-sandbox**: 基于Docker的安全代码执行沙箱，支持多种编程语言
- **oj-judge**: 评测核心引擎，负责测试用例比对和结果评分
- **oj-api**: RESTful API服务，提供系统对外接口
- **oj-frontend**: 用户界面，提供Web交互环境

## 关键特性

- **多语言支持**：目前支持Java、JavaScript(Node.js)和Python代码评测
- **安全执行环境**：使用Docker容器技术隔离用户代码执行
- **精确评测**：支持多种评测模式，包括标准输入/输出、函数调用测试等
- **资源监控**：严格控制内存使用、CPU时间和执行超时
- **可扩展架构**：模块化设计，便于添加新功能和支持更多编程语言

## 技术栈

- **后端**：Spring Boot, Docker API
- **前端**：Vue.js/React
- **数据库**：MySQL/PostgreSQL
- **中间件**：Redis, RabbitMQ
- **部署**：Docker Compose, Kubernetes(可选)

## 快速开始

### 系统要求

- Java 8 或更高版本
- Docker 引擎 (已安装并正在运行)
- Maven 或 Gradle (用于构建)
- Node.js 和 npm (用于前端开发)

### 安装步骤

1. 克隆仓库
   ```bash
   git clone https://github.com/Zhengke0110/oj-services.git
   cd oj-services
   ```

2. 构建各模块
   ```bash
   # 构建沙箱模块
   cd oj-sandbox
   mvn clean package
   
   # 构建其他模块
   cd ../oj-judge
   mvn clean package
   # ...
   ```

3. 启动服务
   ```bash
   docker-compose up -d
   ```

## 模块说明

### oj-sandbox

基于Docker容器的多语言代码执行沙箱系统，支持Java、JavaScript和Python代码的安全隔离执行。详细信息请查看[oj-sandbox README](./oj-sandbox/README.md)。

### oj-judge (待实现)

代码评测核心引擎，负责测试用例执行和结果评判，支持多种评测模式。

### oj-api (待实现)

RESTful API服务，提供用户认证、问题管理、提交处理等功能。

### oj-frontend (待实现)

用户界面，提供代码编辑、提交、查看结果等功能。

## 使用示例

```java
// 创建沙箱执行器
PythonDockerExecutor executor = new PythonDockerExecutor(false);

// 要执行的Python代码
String pythonCode = "print('Hello, OJ Services!')";

// 执行代码
PythonDockerExecutor.ExecutionResult result = 
    executor.executePythonCode(pythonCode, "Hello, OJ Services!", 1);

// 输出结果
System.out.println("执行成功: " + result.isSuccess());
System.out.println("输出匹配: " + result.isOutputMatched());
System.out.println("执行时间: " + result.getAverageExecutionTime() + " ms");
```

## 开发路线图

- [x] 实现基础代码沙箱执行功能
- [ ] 完善评测引擎，支持更多测试模式
- [ ] 开发RESTful API服务
- [ ] 实现前端界面
- [ ] 添加用户和权限管理
- [ ] 支持更多编程语言
- [ ] 实现集群部署方案

## 贡献指南

欢迎参与项目开发，您可以通过以下方式贡献：
- 提交Bug报告
- 贡献功能代码
- 完善文档
- 提供测试用例

## 许可证

MIT License
