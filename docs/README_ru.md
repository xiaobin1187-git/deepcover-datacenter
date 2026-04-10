# DeepCover Data Center - Центр данных покрытия кода

**[中文](../README.md)** | [English](README_en.md) | [日本語](README_ja.md) | [Francais](README_fr.md) | [Portugues](README_pt.md) | **Русский**

<div align="center">

![CI](https://img.shields.io/github/actions/workflow/status/xiaobin1187-git/deepcover-datacenter/ci.yml?branch=main)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-green)
![Maven](https://img.shields.io/badge/Maven-3.5-blue)
![Tests](https://img.shields.io/badge/Tests-58_passed-brightgreen)

</div>

> Центр хранения, обработки и анализа данных покрытия кода, работающий с DeepCover Agent для обеспечения полного конвейера сбора покрытия кода

## Введение

DeepCover Data Center -- это модуль центра данных системы сбора покрытия кода [DeepCover](https://github.com/xiaobin1187-git/deepcover). Он получает данные о покрытии кода, собранные Agent, и выполняет моделирование сценариев, анализ трассировок и постоянное хранение.

### Основные возможности

- **Мультиисточниковая архитектура данных** -- Графовая база данных Neo4j + MySQL + HBase, каждая использующая свои сильные стороны
- **Моделирование сценариев** -- Автоматическое построение моделей сценариев вызовов кода на основе графовой базы данных
- **Анализ трассировок** -- Отслеживание и визуальный анализ выполнения кода
- **Поддержка нескольких очередей сообщений** -- Прием данных через два канала: Kafka и RocketMQ
- **Анализ активности API** -- Статистический анализ частоты вызовов на уровне API и методов
- **Документация Swagger** -- Встроенная документация API, готовая к использованию
- **Планировщик задач** -- Автоматическая очистка устаревших данных и обновление оценок рисков

### Архитектура системы

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

## Технологический стек

| Компонент | Версия | Описание |
|------|------|------|
| Spring Boot | 2.6.13 | Фреймворк приложений |
| Neo4j | 4.4.x | Графовая база данных, основное хранилище для моделирования сценариев |
| HBase | 2.1.x | Хранение данных трассировок в больших масштабах |
| MySQL | 8.0 | Хранение структурированных данных |
| Kafka | 2.8.x | Очередь сообщений |
| RocketMQ | 2.2.x | Очередь сообщений |
| Caffeine | 2.9.x | Локальный кэш |
| Swagger | 2.9.2 | Документация API |

## Быстрый старт

### Требования

- JDK 17 LTS
- Maven 3.5+
- Neo4j 4.4+
- MySQL 8.0+
- HBase 2.1+ (опционально)
- Kafka или RocketMQ (опционально)

### Сборка

```bash
# Сборка с помощью Maven
mvn clean package -DskipTests

# Сборка с помощью Maven Wrapper
./mvnw clean package -DskipTests
```

### Конфигурация

1. Скопируйте шаблон конфигурации:
```bash
cp src/main/resources/application.properties src/main/resources/application.properties
```

2. Измените информацию о подключении к источникам данных в `application.properties`:
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

### Запуск

```bash
mvn spring-boot:run
```

После запуска приложения документация Swagger API доступна по адресу: `http://localhost:port/doc.html`

## Тестирование

```bash
# Запустить все тесты (58 тестовых случаев)
mvn clean test

# Запустить отдельный тестовый класс
mvn test -Dtest=BusinessResultTest
```

## Документация API

После запуска приложения документация API доступна по следующим адресам:

- Swagger UI: `http://localhost:port/swagger-ui.html`
- Swagger Bootstrap UI: `http://localhost:port/doc.html`

Основные конечные точки API:

| Конечная точка | Описание |
|------|------|
| `/sceneModel/*` | Управление моделями сценариев |
| `/linkAnalysisModel/*` | Модель анализа трассировок |
| `/sceneTraceIdModel/*` | Управление идентификаторами трассировок сценариев |
| `/sceneBranch/*` | Управление ветвями сценариев |
| `/sceneModelNode/*` | Управление узлами сценариев |
| `/apiHeatModel/*` | Модель активности API |
| `/traceHbase/*` | Запрос данных трассировок HBase |
| `/aresCollect/*` | Прием собранных данных |

## Структура проекта

```
src/main/java/io/deepcover/datacenter/
├── common/           # Общие классы (BusinessResult, ResultEnum, BaseJobHandler)
├── dal/              # Уровень доступа к данным
│   ├── dao/entity/   # Сущности Neo4j
│   ├── dao/mapper/   # MyBatis Mapper
│   ├── dao/mysqlentity/  # Сущности MySQL
│   └── dao/repository/   # Spring Data Neo4j Repository
├── deploy/           # Класс запуска приложения
└── service/          # Уровень сервисов
    ├── config/       # Классы конфигурации
    ├── controller/   # Контроллеры REST API
    ├── impl/         # Реализации сервисов
    ├── kafka/        # Потребители Kafka
    ├── mq/           # Потребители RocketMQ
    ├── scheduler/    # Планировщик задач
    └── utils/        # Утилитарные классы
```

## Участие в разработке

Приветствуются любые вклады! Пожалуйста, прочитайте [CONTRIBUTING.md](../CONTRIBUTING.md) для получения подробной информации.

## Лицензия

Этот проект распространяется под лицензией [Apache License 2.0](../LICENSE).
