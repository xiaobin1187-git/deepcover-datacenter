<p align="center"><img src="docs/assets/logo.svg" alt="DeepCover" width="96" height="96"></p>
# DeepCover Data Center - Precision Analysis Data Center

**[中文](../README.md)** | **English** | [日本語](README_ja.md) | [Francais](README_fr.md) | [Portugues](README_pt.md) | [Русский](README_ru.md)

<div align="center">

![CI](https://img.shields.io/github/actions/workflow/status/xiaobin1187-git/deepcover-datacenter/ci.yml?branch=main)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-green)
![Maven](https://img.shields.io/badge/Maven-3.5-blue)
![Tests](https://img.shields.io/badge/Tests-58_passed-brightgreen)

</div>

> A data storage, processing, and analysis center for precision analysis, working with DeepCover Agent to provide a complete precision analysis collection pipeline

## Introduction

DeepCover Data Center is the data center module of the [DeepCover](https://github.com/xiaobin1187-git/deepcover) precision analysis collection system. It receives precision analysis data collected by the Agent and performs scenario modeling, trace analysis, and persistent storage.

### Key Features

- **Multi-Datasource Architecture** -- Neo4j graph database + MySQL + HBase, each serving its strengths
- **Scenario Modeling** -- Automatic construction of code invocation scenario models based on graph database
- **Trace Analysis** -- Code execution trace tracking and visual analysis
- **Multi-Message Queue** -- Supports both Kafka and RocketMQ dual-channel data reception
- **API Heat Analysis** -- Statistical analysis of API and method-level invocation frequency
- **Swagger Documentation** -- Built-in API documentation, ready to use out of the box
- **Scheduled Tasks** -- Automatic cleanup of expired data and risk rating updates

### System Architecture

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

## Tech Stack

| Component | Version | Description |
|------|------|------|
| Spring Boot | 2.6.13 | Application framework |
| Neo4j | 4.4.x | Graph database, core storage for scenario modeling |
| HBase | 2.1.x | Large-scale trace data storage |
| MySQL | 8.0 | Structured data storage |
| Kafka | 2.8.x | Message queue |
| RocketMQ | 2.2.x | Message queue |
| Caffeine | 2.9.x | Local cache |
| Swagger | 2.9.2 | API documentation |

## Quick Start

### Prerequisites

- JDK 17 LTS
- Maven 3.5+
- Neo4j 4.4+
- MySQL 8.0+
- HBase 2.1+ (optional)
- Kafka or RocketMQ (optional)

### Build

```bash
# Build with Maven
mvn clean package -DskipTests

# Build with Maven Wrapper
./mvnw clean package -DskipTests
```

### Configuration

1. Copy the configuration template:
```bash
cp src/main/resources/application.properties src/main/resources/application.properties
```

2. Modify the datasource connection information in `application.properties`:
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

### Run

```bash
mvn spring-boot:run
```

After starting the application, access the Swagger API documentation at: `http://localhost:port/doc.html`

## Testing

```bash
# Run all tests (58 test cases)
mvn clean test

# Run a single test class
mvn test -Dtest=BusinessResultTest
```

## API Documentation

After starting the application, access the API documentation at the following addresses:

- Swagger UI: `http://localhost:port/swagger-ui.html`
- Swagger Bootstrap UI: `http://localhost:port/doc.html`

Main API endpoints:

| Endpoint | Description |
|------|------|
| `/sceneModel/*` | Scenario model management |
| `/linkAnalysisModel/*` | Trace analysis model |
| `/sceneTraceIdModel/*` | Scenario trace ID management |
| `/sceneBranch/*` | Scenario branch management |
| `/sceneModelNode/*` | Scenario node management |
| `/apiHeatModel/*` | API heat model |
| `/traceHbase/*` | HBase trace data query |
| `/aresCollect/*` | Collection data reception |

## Project Structure

```
src/main/java/io/deepcover/datacenter/
├── common/           # Common classes (BusinessResult, ResultEnum, BaseJobHandler)
├── dal/              # Data Access Layer
│   ├── dao/entity/   # Neo4j entities
│   ├── dao/mapper/   # MyBatis Mapper
│   ├── dao/mysqlentity/  # MySQL entities
│   └── dao/repository/   # Spring Data Neo4j Repository
├── deploy/           # Application entry class
└── service/          # Service layer
    ├── config/       # Configuration classes
    ├── controller/   # REST API controllers
    ├── impl/         # Service implementations
    ├── kafka/        # Kafka consumers
    ├── mq/           # RocketMQ consumers
    ├── scheduler/    # Scheduled tasks
    └── utils/        # Utility classes
```

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](../CONTRIBUTING.md) for details.

## License

This project is licensed under the [Apache License 2.0](../LICENSE).
