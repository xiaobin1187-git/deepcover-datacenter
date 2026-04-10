# DeepCover Data Center - コードカバレッジデータセンター

**[中文](../README.md)** | [English](README_en.md) | **日本語** | [Francais](README_fr.md) | [Portugues](README_pt.md) | [Русский](README_ru.md)

<div align="center">

![CI](https://img.shields.io/github/actions/workflow/status/xiaobin1187-git/deepcover-datacenter/ci.yml?branch=main)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-green)
![Maven](https://img.shields.io/badge/Maven-3.5-blue)
![Tests](https://img.shields.io/badge/Tests-58_passed-brightgreen)

</div>

> コードカバレッジデータの保存、処理、分析センター。DeepCover Agentと連携し、完全なコードカバレッジ収集パイプラインを実現

## 概要

DeepCover Data Center は [DeepCover](https://github.com/xiaobin1187-git/deepcover) コードカバレッジ収集システムのデータセンターモジュールです。Agentが収集したコードカバレッジデータを受信し、シナリオモデリング、トレース分析、永続ストレージを行います。

### 主な特徴

- **マルチデータソースアーキテクチャ** -- Neo4jグラフデータベース + MySQL + HBase、それぞれの強みを活用
- **シナリオモデリング** -- グラフデータベースに基づくコード呼び出しシナリオモデルの自動構築
- **トレース分析** -- コード実行トレースの追跡と可視化分析
- **マルチメッセージキュー** -- KafkaおよびRocketMQのデュアルチャネルデータ受信をサポート
- **APIヒートマップ分析** -- APIおよびメソッドレベルの呼び出し頻度の統計分析
- **Swaggerドキュメント** -- 内蔵APIドキュメント、すぐに使用可能
- **スケジュールタスク** -- 期限切れデータの自動クリーニング、リスク評価の自動更新

### システムアーキテクチャ

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

## 技術スタック

| コンポーネント | バージョン | 説明 |
|------|------|------|
| Spring Boot | 2.6.13 | アプリケーションフレームワーク |
| Neo4j | 4.4.x | グラフデータベース、シナリオモデリングのコアストレージ |
| HBase | 2.1.x | 大規模トレースデータストレージ |
| MySQL | 8.0 | 構造化データストレージ |
| Kafka | 2.8.x | メッセージキュー |
| RocketMQ | 2.2.x | メッセージキュー |
| Caffeine | 2.9.x | ローカルキャッシュ |
| Swagger | 2.9.2 | APIドキュメント |

## クイックスタート

### 前提条件

- JDK 17 LTS
- Maven 3.5+
- Neo4j 4.4+
- MySQL 8.0+
- HBase 2.1+（オプション）
- Kafka または RocketMQ（オプション）

### ビルド

```bash
# Mavenでビルド
mvn clean package -DskipTests

# Maven Wrapperでビルド
./mvnw clean package -DskipTests
```

### 設定

1. 設定テンプレートをコピー：
```bash
cp src/main/resources/application.properties src/main/resources/application.properties
```

2. `application.properties`のデータソース接続情報を変更：
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

### 実行

```bash
mvn spring-boot:run
```

アプリケーション起動後、Swagger APIドキュメントにアクセス：`http://localhost:port/doc.html`

## テスト

```bash
# 全テストを実行（58テストケース）
mvn clean test

# 単一テストクラスを実行
mvn test -Dtest=BusinessResultTest
```

## APIドキュメント

アプリケーション起動後、以下のアドレスでAPIドキュメントにアクセスできます：

- Swagger UI: `http://localhost:port/swagger-ui.html`
- Swagger Bootstrap UI: `http://localhost:port/doc.html`

主なAPIエンドポイント：

| エンドポイント | 説明 |
|------|------|
| `/sceneModel/*` | シナリオモデル管理 |
| `/linkAnalysisModel/*` | トレース分析モデル |
| `/sceneTraceIdModel/*` | シナリオトレースID管理 |
| `/sceneBranch/*` | シナリオブランチ管理 |
| `/sceneModelNode/*` | シナリオノード管理 |
| `/apiHeatModel/*` | APIヒートマップモデル |
| `/traceHbase/*` | HBaseトレースデータ照会 |
| `/aresCollect/*` | 収集データ受信 |

## プロジェクト構成

```
src/main/java/io/deepcover/datacenter/
├── common/           # 共通クラス（BusinessResult、ResultEnum、BaseJobHandler）
├── dal/              # データアクセス層
│   ├── dao/entity/   # Neo4jエンティティ
│   ├── dao/mapper/   # MyBatis Mapper
│   ├── dao/mysqlentity/  # MySQLエンティティ
│   └── dao/repository/   # Spring Data Neo4j Repository
├── deploy/           # アプリケーション起動クラス
└── service/          # サービス層
    ├── config/       # 設定クラス
    ├── controller/   # REST APIコントローラー
    ├── impl/         # サービス実装
    ├── kafka/        # Kafkaコンシューマー
    ├── mq/           # RocketMQコンシューマー
    ├── scheduler/    # スケジュールタスク
    └── utils/        # ユーティリティクラス
```

## コントリビューション

コントリビューションを歓迎します！詳細は [CONTRIBUTING.md](../CONTRIBUTING.md) をお読みください。

## ライセンス

このプロジェクトは [Apache License 2.0](../LICENSE) の下でライセンスされています。
