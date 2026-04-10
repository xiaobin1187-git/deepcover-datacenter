# DeepCover Data Center - 代码覆盖率数据中心

**[中文](README.md)** | [English](docs/README_en.md) | [日本語](docs/README_ja.md) | [Francais](docs/README_fr.md) | [Portugues](docs/README_pt.md) | [Русский](docs/README_ru.md)

<div align="center">

![CI](https://img.shields.io/github/actions/workflow/status/xiaobin1187-git/deepcover-datacenter/ci.yml?branch=main)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-green)
![Maven](https://img.shields.io/badge/Maven-3.5-blue)
![Tests](https://img.shields.io/badge/Tests-58_passed-brightgreen)

</div>

> 代码覆盖率数据存储、处理与分析中心，配合 DeepCover Agent 实现完整的代码覆盖率采集链路

## 简介

DeepCover Data Center 是 [DeepCover](https://github.com/xiaobin1187-git/deepcover) 代码覆盖率采集系统的数据中心模块，负责接收 Agent 采集的代码覆盖率数据，进行场景建模、链路分析和持久化存储。

### 主要特性

- **多数据源架构** -- Neo4j 图数据库 + MySQL + HBase，各取所长
- **场景建模** -- 基于图数据库自动构建代码调用场景模型
- **链路分析** -- 代码执行链路追踪与可视化分析
- **多消息队列** -- 支持 Kafka 和 RocketMQ 双通道数据接收
- **API 热度分析** -- 统计 API 和方法级别的调用热度
- **Swagger 文档** -- 内置 API 文档，开箱即用
- **定时任务** -- 自动清理过期数据、更新风险评级

### 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     目标应用 JVM                                 │
│                                                                  │
│   ┌──────────────────┐    ┌───────────────────┐                 │
│   │  DeepCover Agent  │───>│  HTTP / Kafka     │                 │
│   │  (代码行级采集)    │    │  数据导出          │                 │
│   └──────────────────┘    └────────┬──────────┘                 │
│                                    │                             │
└────────────────────────────────────┼─────────────────────────────┘
                                     │
                    ┌────────────────┴────────────────┐
                    │                                  │
           ┌────────┴────────┐              ┌─────────┴─────────┐
           │  HTTP API       │              │  Kafka / RocketMQ  │
           │  /api/collect   │              │  Topic 消费        │
           └────────┬────────┘              └─────────┬─────────┘
                    │                                  │
                    └────────────┬─────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │    DeepCover Data Center │
                    │                         │
                    │  ┌──────────────────┐   │
                    │  │  数据接收与处理    │   │
                    │  └────────┬─────────┘   │
                    │           │              │
                    │  ┌────────┴─────────┐   │
                    │  │  场景建模引擎      │   │
                    │  │  链路分析引擎      │   │
                    │  │  热度计算引擎      │   │
                    │  └────────┬─────────┘   │
                    │           │              │
                    │  ┌────────┴─────────┐   │
                    │  │  多数据源存储      │   │
                    │  │  Neo4j / HBase    │   │
                    │  │  MySQL / Redis    │   │
                    │  └──────────────────┘   │
                    └─────────────────────────┘
```

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.6.13 | 应用框架 |
| Neo4j | 4.4.x | 图数据库，场景建模核心存储 |
| HBase | 2.1.x | 大规模追踪数据存储 |
| MySQL | 8.0 | 结构化数据存储 |
| Kafka | 2.8.x | 消息队列 |
| RocketMQ | 2.2.x | 消息队列 |
| Caffeine | 2.9.x | 本地缓存 |
| Swagger | 2.9.2 | API 文档 |

## 快速开始

### 环境要求

- JDK 17 LTS
- Maven 3.5+
- Neo4j 4.4+
- MySQL 8.0+
- HBase 2.1+ (可选)
- Kafka 或 RocketMQ (可选)

### 构建

```bash
# 使用 Maven 构建
mvn clean package -DskipTests

# 使用 Maven Wrapper
./mvnw clean package -DskipTests
```

### 配置

1. 复制配置模板：
```bash
cp src/main/resources/application.properties src/main/resources/application.properties
```

2. 修改 `application.properties` 中的数据源连接信息：
```properties
# Neo4j
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=YOUR_NEO4J_PASSWORD

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/deepcover
spring.datasource.username=root
spring.datasource.password=YOUR_DB_PASSWORD
```

### 运行

```bash
mvn spring-boot:run
```

应用启动后访问 Swagger API 文档：`http://localhost:port/doc.html`

## 测试

```bash
# 运行全部测试 (58 个用例)
mvn clean test

# 运行单个测试类
mvn test -Dtest=BusinessResultTest
```

## API 文档

启动应用后，通过以下地址访问 API 文档：

- Swagger UI: `http://localhost:port/swagger-ui.html`
- Swagger Bootstrap UI: `http://localhost:port/doc.html`

主要 API 端点：

| 端点 | 说明 |
|------|------|
| `/sceneModel/*` | 场景模型管理 |
| `/linkAnalysisModel/*` | 链路分析模型 |
| `/sceneTraceIdModel/*` | 场景追踪 ID 管理 |
| `/sceneBranch/*` | 场景分支管理 |
| `/sceneModelNode/*` | 场景节点管理 |
| `/apiHeatModel/*` | API 热度模型 |
| `/traceHbase/*` | HBase 追踪数据查询 |
| `/aresCollect/*` | 采集数据接收 |

## 项目结构

```
src/main/java/io/deepcover/datacenter/
├── common/           # 通用类 (BusinessResult, ResultEnum, BaseJobHandler)
├── dal/              # 数据访问层
│   ├── dao/entity/   # Neo4j 实体
│   ├── dao/mapper/   # MyBatis Mapper
│   ├── dao/mysqlentity/  # MySQL 实体
│   └── dao/repository/   # Spring Data Neo4j Repository
├── deploy/           # 应用启动类
└── service/          # 业务层
    ├── config/       # 配置类
    ├── controller/   # REST API 控制器
    ├── impl/         # 服务实现
    ├── kafka/        # Kafka 消费者
    ├── mq/           # RocketMQ 消费者
    ├── scheduler/    # 定时任务
    └── utils/        # 工具类
```

## 贡献

欢迎贡献代码！请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详情。

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 许可证开源。
