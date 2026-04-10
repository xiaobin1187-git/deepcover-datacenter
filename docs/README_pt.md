# DeepCover Data Center - Centro de analise de precisao
<img src="docs/assets/logo.svg" alt="DeepCover Logo" width="128" height="128" align="right">

**[中文](../README.md)** | [English](README_en.md) | [日本語](README_ja.md) | [Francais](README_fr.md) | **Portugues** | [Русский](README_ru.md)

<div align="center">

![CI](https://img.shields.io/github/actions/workflow/status/xiaobin1187-git/deepcover-datacenter/ci.yml?branch=main)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-green)
![Maven](https://img.shields.io/badge/Maven-3.5-blue)
![Tests](https://img.shields.io/badge/Tests-58_passed-brightgreen)

</div>

> Centro de armazenamento, processamento e analise de dados de analise de precisao, trabalhando com o DeepCover Agent para fornecer um pipeline completo de coleta de analise de precisao

## Introducao

O DeepCover Data Center e o modulo de centro de dados do sistema de coleta de analise de precisao [DeepCover](https://github.com/xiaobin1187-git/deepcover). Ele recebe os dados de analise de precisao coletados pelo Agent e realiza modelagem de cenarios, analise de rastreamento e armazenamento persistente.

### Principais recursos

- **Arquitetura multi-fonte de dados** -- Banco de dados de grafos Neo4j + MySQL + HBase, cada um aproveitando seus pontos fortes
- **Modelagem de cenarios** -- Construcao automatica de modelos de cenarios de invocacao de codigo baseados em banco de dados de grafos
- **Analise de rastreamento** -- Rastreamento e analise visual da execucao de codigo
- **Multi-fila de mensagens** -- Suporte para recebimento de dados de duplo canal via Kafka e RocketMQ
- **Analise de calor de API** -- Analise estatistica da frequencia de invocacao no nivel de API e metodos
- **Documentacao Swagger** -- Documentacao de API integrada, pronta para uso imediato
- **Tarefas agendadas** -- Limpeza automatica de dados expirados e atualizacao de classificacoes de risco

### Arquitetura do sistema

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

## Stack tecnologica

| Componente | Versao | Descricao |
|------|------|------|
| Spring Boot | 2.6.13 | Framework de aplicacao |
| Neo4j | 4.4.x | Banco de dados de grafos, armazenamento principal para modelagem de cenarios |
| HBase | 2.1.x | Armazenamento de dados de rastreamento em grande escala |
| MySQL | 8.0 | Armazenamento de dados estruturados |
| Kafka | 2.8.x | Fila de mensagens |
| RocketMQ | 2.2.x | Fila de mensagens |
| Caffeine | 2.9.x | Cache local |
| Swagger | 2.9.2 | Documentacao de API |

## Inicio rapido

### Requisitos

- JDK 17 LTS
- Maven 3.5+
- Neo4j 4.4+
- MySQL 8.0+
- HBase 2.1+ (opcional)
- Kafka ou RocketMQ (opcional)

### Compilacao

```bash
# Compilar com Maven
mvn clean package -DskipTests

# Compilar com Maven Wrapper
./mvnw clean package -DskipTests
```

### Configuracao

1. Copie o modelo de configuracao:
```bash
cp src/main/resources/application.properties src/main/resources/application.properties
```

2. Modifique as informacoes de conexao das fontes de dados em `application.properties`:
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

### Execucao

```bash
mvn spring-boot:run
```

Apos iniciar a aplicacao, acesse a documentacao Swagger API em: `http://localhost:port/doc.html`

## Testes

```bash
# Executar todos os testes (58 casos de teste)
mvn clean test

# Executar uma unica classe de teste
mvn test -Dtest=BusinessResultTest
```

## Documentacao da API

Apos iniciar a aplicacao, acesse a documentacao da API nos seguintes enderecos:

- Swagger UI: `http://localhost:port/swagger-ui.html`
- Swagger Bootstrap UI: `http://localhost:port/doc.html`

Principais endpoints da API:

| Endpoint | Descricao |
|------|------|
| `/sceneModel/*` | Gerenciamento de modelos de cenarios |
| `/linkAnalysisModel/*` | Modelo de analise de rastreamento |
| `/sceneTraceIdModel/*` | Gerenciamento de IDs de rastreamento de cenarios |
| `/sceneBranch/*` | Gerenciamento de branches de cenarios |
| `/sceneModelNode/*` | Gerenciamento de nos de cenarios |
| `/apiHeatModel/*` | Modelo de calor de API |
| `/traceHbase/*` | Consulta de dados de rastreamento HBase |
| `/aresCollect/*` | Recebimento de dados coletados |

## Estrutura do projeto

```
src/main/java/io/deepcover/datacenter/
├── common/           # Classes comuns (BusinessResult, ResultEnum, BaseJobHandler)
├── dal/              # Camada de acesso a dados
│   ├── dao/entity/   # Entidades Neo4j
│   ├── dao/mapper/   # MyBatis Mapper
│   ├── dao/mysqlentity/  # Entidades MySQL
│   └── dao/repository/   # Spring Data Neo4j Repository
├── deploy/           # Classe de inicializacao da aplicacao
└── service/          # Camada de servico
    ├── config/       # Classes de configuracao
    ├── controller/   # Controladores REST API
    ├── impl/         # Implementacoes de servico
    ├── kafka/        # Consumidores Kafka
    ├── mq/           # Consumidores RocketMQ
    ├── scheduler/    # Tarefas agendadas
    └── utils/        # Classes utilitarias
```

## Contribuicao

Contribuicoes sao bem-vindas! Leia [CONTRIBUTING.md](../CONTRIBUTING.md) para mais detalhes.

## Licenca

Este projeto esta licenciado sob a [Apache License 2.0](../LICENSE).
