# DOJ (Distributed Online Judge) 系统

DOJ是一个完整的分布式在线评测系统，提供代码执行、测评和管理功能，适用于编程教育、算法竞赛和技术面试场景。

## 项目概述

DOJ系统采用前后端分离架构，通过微服务设计提供安全、可靠的代码评测环境，包含以下核心组件：

- **oj-sandbox**: 基于Docker的安全代码执行沙箱，支持多种编程语言
- **oj-judge**: 评测核心引擎，负责测试用例比对和结果评分
- **oj-api**: 后端RESTful API服务，提供系统业务逻辑
- **oj-frontend**: 前端用户界面，提供Web交互环境

## 系统架构

### 前端技术栈 (oj-frontend)

- **核心框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **状态管理**: Pinia + pinia-plugin-persistedstate
- **路由**: Vue Router 4
- **UI组件库**: 
  - Arco Design Vue
  - Headless UI
  - UnoCSS (原子化CSS)
- **编辑器**:
  - CodeMirror (代码编辑器)
  - ByteMD (Markdown编辑器)
- **HTTP客户端**: Axios
- **工具库**: VueUse, dayjs (日期处理)

### 后端技术栈 (oj-api)

- **主框架**: Spring Boot 3.x
- **Web框架**: Spring MVC
- **ORM框架**: MyBatis Plus
- **分布式会话**: Spring Session
- **切面编程**: Spring AOP
- **数据存储**:
  - MySQL - 关系型数据库
  - Redis - 缓存与会话存储
- **工具类库**:
  - Hutool - Java工具集
  - Apache Commons Lang3
  - Lombok
  - Jackson - JSON处理

### 沙箱技术栈 (oj-sandbox)

- **容器技术**: Docker
- **编程语言支持**: Java, JavaScript(Node.js), Python
- **Docker Java API**: 容器管理与执行
- **资源限制机制**: 内存、CPU时间和执行超时控制

## 核心功能模块

### 1. 用户系统
- 用户注册、登录、注销
- 基于Spring Session Redis的分布式登录
- 用户权限管理 (普通用户/管理员/封禁用户)
- 用户信息管理

### 2. 题目系统
- 题目管理与浏览
- 题目详情查看
- 代码提交与判题
- 题目收藏功能

### 3. 代码执行沙箱
- **多语言支持**: Java、JavaScript和Python
- **安全隔离**: Docker容器技术隔离执行环境
- **资源限制**: 内存使用、CPU使用和执行时间限制
- **多种输入模式**: 普通执行、命令行参数、测试文件输入
- **详细指标收集**: 执行时间、内存使用、执行状态和输出结果

### 4. 在线判题系统
- 支持多种评测模式
- 精确的结果对比
- 提交历史查询
- 测试用例管理

## 系统特性

### 安全特性
- 代码沙箱隔离执行
- 网络隔离 (`--network=none`)
- 资源限制与防护
- 自定义权限注解 `@AuthCheck`
- 基于AOP的权限拦截器

### 性能优化
- MyBatis Plus分页插件
- Redis缓存支持
- Long类型精度丢失处理
- 容器资源动态分配

### 前端用户体验
- 响应式设计
- 代码编辑器支持语法高亮、自动补全
- 多主题支持
- 自适应布局

## 项目结构

```
oj-services/
├── oj-sandbox/          # 代码执行沙箱模块
│   ├── src/             # 源代码
│   └── README.md        # 沙箱模块说明
├── oj-judge/            # 评测核心引擎(待实现)
│   └── src/             # 源代码
├── oj-api/              # 后端API服务(待实现)
│   ├── src/
│   │   ├── annotation/  # 自定义注解
│   │   ├── aop/         # AOP切面
│   │   ├── common/      # 通用类
│   │   ├── config/      # 配置类
│   │   ├── constant/    # 常量定义
│   │   ├── controller/  # 控制器
│   │   ├── exception/   # 异常处理
│   │   ├── mapper/      # MyBatis接口
│   │   ├── model/       # 数据模型
│   │   ├── service/     # 业务逻辑
│   │   └── utils/       # 工具类
│   └── README.md        # 后端模块说明
└── oj-frontend/         # 前端项目(待实现)
    ├── src/             
    │   ├── access/      # 权限控制
    │   ├── assets/      # 静态资源
    │   ├── components/  # 公共组件
    │   ├── config/      # 配置文件
    │   ├── generated/   # 自动生成的API代码
    │   ├── layouts/     # 布局组件
    │   ├── router/      # 路由配置
    │   ├── store/       # 状态管理
    │   └── views/       # 页面组件
    └── README.md        # 前端模块说明
```

## 快速开始

### 系统要求
- Java 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 6.0+
- Node.js >= 16
- npm >= 7
- Docker引擎 (已安装并正在运行)

### 安装步骤

1. 克隆仓库
   ```bash
   git clone https://github.com/Zhengke0110/oj-services.git
   cd oj-services
   ```

2. 构建沙箱模块
   ```bash
   cd oj-sandbox
   mvn clean package
   ```

3. 启动后端服务 (完成开发后)
   ```bash
   cd oj-api
   mvn spring-boot:run
   ```

4. 前端开发与构建 (完成开发后)
   ```bash
   cd oj-frontend
   npm install
   npm run dev   # 开发服务器
   npm run build # 构建生产版本
   ```

5. Docker Compose部署 (可选)
   ```bash
   docker-compose up -d
   ```

## 沙箱系统使用示例

```java
// 创建沙箱执行器
PythonDockerExecutor executor = new PythonDockerExecutor(false);

// 要执行的Python代码
String pythonCode = "print('Hello, DOJ!')";

// 执行代码
PythonDockerExecutor.ExecutionResult result = 
    executor.executePythonCode(pythonCode, "Hello, DOJ!", 1);

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
