# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Ares DataCenter 是一个企业级数据中心项目，主要用于场景建模、链路分析和性能监控。该项目采用图数据库为核心存储，结合关系型数据库和NoSQL数据库，提供完整的数据采集、分析和可视化能力。

## 构建和运行

### Maven 构建命令
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 运行单个测试类
mvn test -Dtest=ClassName

# 运行单个测试方法
mvn test -Dtest=ClassName#methodName
```

### 本地 Maven 配置
项目使用本地 Maven 配置文件：`D:\tools\apache-maven-3.5.2\conf\settings.xml`

### 运行应用
```bash
# 使用 Maven 运行
mvn spring-boot:run

# 指定环境参数运行
mvn spring-boot:run -Denv=test

# 或直接运行 JAR
java -jar target/aresdatacenter-0.0.1-SNAPSHOT.jar
```

### 环境配置
项目支持多环境配置，通过 `env` 参数指定：
- `test` - 测试环境
- `pre` - 预发布环境
- `prod` - 生产环境

配置文件位于：
- `src/main/resources/application.properties` - 主配置
- `src/main/resources/application-{env}.properties` - 环境特定配置

## 代码架构

### 分层架构
项目采用经典的三层架构：
```
Controller (表现层)
    ↓
Service (业务层)
    ↓
Repository/DAO (数据访问层)
    ↓
Entity (实体层)
```

### 核心包结构
- `com.timevale.aresdatacenter.dal` - 数据访问层
  - `dao.entity` - JPA实体
  - `dao.mapper` - MyBatis映射器
  - `dao.mysqlentity` - MySQL实体
  - `dao.repository` - Neo4j图数据库仓库
- `com.timevale.aresdatacenter.service` - 业务服务层
  - `controller` - REST API控制器
  - `impl` - 服务实现
  - `config` - 配置类
  - `kafka` - Kafka消息处理
  - `mq` - RocketMQ消息处理
  - `scheduler` - 定时任务
  - `utils` - 工具类
- `com.timevale.aresdatacenter.deploy` - 部署和启动类

### 多数据源架构
项目使用多个数据源，各有不同用途：

1. **Neo4j (图数据库)** - 主要存储
   - 用于存储场景模型、链路分析等图结构数据
   - 使用 Spring Data Neo4j 进行操作
   - 支持APOC库进行图操作优化

2. **MySQL (关系型数据库)**
   - 用于存储结构化数据
   - 使用 Spring Data JPA
   - 实体类在 `dal.dao.mysqlentity` 包

3. **HBase (NoSQL数据库)**
   - 阿里云HBase版本
   - 用于存储大规模日志和追踪数据
   - 主要表：`auditLog`、`trace`

4. **Redis (缓存)**
   - 通过Caffeine实现本地缓存

### 消息队列
项目支持两种消息队列：

1. **RocketMQ** - 主要消息队列
   - 配置在环境文件中
   - 用于数据采集和处理

2. **Kafka** - 备用消息队列
   - 配置在环境文件中
   - 支持批量消费和手动提交

### API 文档
项目使用 Swagger 2.9.2 生成 API 文档：
- 文档地址：`/doc.html` (Swagger Bootstrap UI)
- 备用地址：`/swagger-ui.html`
- 使用 `@ApiOperation` 注解标记API

### 日志系统
使用 Log4j2 作为日志框架，日志分类输出：
- `default.log` - 默认日志
- `sql.log` - SQL日志
- `error.log` - 错误日志
- `service.log` - 业务日志
- `request.log` - 请求日志

同时支持阿里云日志服务 (Aliyun Log)。

### 定时任务
使用 Timevale 内部框架 schedulerT-client：
- 支持分布式任务调度
- 定时任务类在 `service.scheduler` 包
- 示例：`HandleDeleteDataJob` 用于数据清理

### 配置管理
使用 Puppeteer 框架进行配置管理：
- 启用命名空间：`application`、`JSBZ.SOA_PUBLIC`
- 通过 Meta Server 获取配置
- 支持多环境配置中心

### 内部框架依赖
项目深度集成 Timevale 内部框架：
- `schedulerT-client` (v3.0.3) - 定时任务客户端
- `puppeteer-client` (v3.3.6) - 配置管理客户端
- `hydra-core` (v2.0.1) - 平台核心组件
- `esign-mq-core` (v2.11.7) - 电子签名消息

### 核心实体
- `SceneModelEntity` - 场景模型
- `ApiHeatModelEntity` - API热度模型
- `MethodHeatModelEntity` - 方法热度模型
- `LinkAnalysisModelEntity` - 链路分析模型
- `SceneBranchEntity` - 场景分支
- `SceneNodeEntity` - 场景节点
- `SceneTraceIdEntity` - 场景追踪ID

### 核心服务
- `SceneModelService` - 场景模型服务
- `ApiHeatModelService` - API热度服务
- `LinkAnalysisModelService` - 链路分析服务
- `HbaseService` - HBase操作服务
- `AuditLogService` - 审计日志服务
- `MessageHandleService` - 消息处理服务

## 开发注意事项

### 版本信息
- Java: 1.8
- Spring Boot: 2.6.13
- Neo4j Driver: 4.4.11
- Spring Data Neo4j: 6.2.10

### 依赖冲突处理
项目排除了默认的 Logback 日志框架，使用 Log4j2。在添加新依赖时，注意排除 `spring-boot-starter-logging` 和 `logback-classic`。

### 测试覆盖
项目当前测试覆盖较少，主要测试文件：
- `AresdatacenterApplicationTests.java` - 基础测试
- `MessageHandleServiceImplTest.java` - 消息处理服务测试

### Git 提交规范
- 提交信息不允许包含机器人表情等特殊符号
- 不要使用 emoji 表情
- 提交代码请不要生成合并请求 (Pull Request)
