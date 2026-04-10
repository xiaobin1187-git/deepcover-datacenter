# DeepCover Data Center - Centre de donnees de couverture de code

**[中文](../README.md)** | [English](README_en.md) | [日本語](README_ja.md) | **Francais** | [Portugues](README_pt.md) | [Русский](README_ru.md)

<div align="center">

![CI](https://img.shields.io/github/actions/workflow/status/xiaobin1187-git/deepcover-datacenter/ci.yml?branch=main)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-green)
![Maven](https://img.shields.io/badge/Maven-3.5-blue)
![Tests](https://img.shields.io/badge/Tests-58_passed-brightgreen)

</div>

> Centre de stockage, de traitement et d'analyse des donnees de couverture de code, fonctionne avec DeepCover Agent pour fournir une chaine complete de collecte de couverture de code

## Presentation

DeepCover Data Center est le module de centre de donnees du systeme de collecte de couverture de code [DeepCover](https://github.com/xiaobin1187-git/deepcover). Il recoit les donnees de couverture de code collectees par l'Agent et effectue la modelisation de scenarios, l'analyse de traces et le stockage persistant.

### Fonctionnalites principales

- **Architecture multi-sources de donnees** -- Base de donnees orientee graphe Neo4j + MySQL + HBase, chacune exploitant ses points forts
- **Modelisation de scenarios** -- Construction automatique de modeles de scenarios d'appel de code bases sur une base de donnees orientee graphe
- **Analyse de traces** -- Suivi et analyse visuelle des traces d'execution de code
- **Multi-file d'attente de messages** -- Support de la reception de donnees double canal via Kafka et RocketMQ
- **Analyse de chaleur API** -- Analyse statistique de la frequence d'appel au niveau des API et des methodes
- **Documentation Swagger** -- Documentation API integree, prete a l'emploi
- **Taches planifiees** -- Nettoyage automatique des donnees expirees et mise a jour des evaluations de risque

### Architecture du systeme

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

## Technologies utilisees

| Composant | Version | Description |
|------|------|------|
| Spring Boot | 2.6.13 | Framework applicatif |
| Neo4j | 4.4.x | Base de donnees orientee graphe, stockage principal pour la modelisation de scenarios |
| HBase | 2.1.x | Stockage de donnees de trace a grande echelle |
| MySQL | 8.0 | Stockage de donnees structurees |
| Kafka | 2.8.x | File d'attente de messages |
| RocketMQ | 2.2.x | File d'attente de messages |
| Caffeine | 2.9.x | Cache local |
| Swagger | 2.9.2 | Documentation API |

## Demarrage rapide

### Prerequis

- JDK 17 LTS
- Maven 3.5+
- Neo4j 4.4+
- MySQL 8.0+
- HBase 2.1+ (optionnel)
- Kafka ou RocketMQ (optionnel)

### Construction

```bash
# Construire avec Maven
mvn clean package -DskipTests

# Construire avec Maven Wrapper
./mvnw clean package -DskipTests
```

### Configuration

1. Copier le modele de configuration :
```bash
cp src/main/resources/application.properties src/main/resources/application.properties
```

2. Modifier les informations de connexion aux sources de donnees dans `application.properties` :
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

### Execution

```bash
mvn spring-boot:run
```

Apres le demarrage de l'application, accedez a la documentation Swagger API a l'adresse : `http://localhost:port/doc.html`

## Tests

```bash
# Executer tous les tests (58 cas de test)
mvn clean test

# Executer une seule classe de test
mvn test -Dtest=BusinessResultTest
```

## Documentation API

Apres le demarrage de l'application, accedez a la documentation API aux adresses suivantes :

- Swagger UI : `http://localhost:port/swagger-ui.html`
- Swagger Bootstrap UI : `http://localhost:port/doc.html`

Principaux points de terminaison API :

| Point de terminaison | Description |
|------|------|
| `/sceneModel/*` | Gestion des modeles de scenarios |
| `/linkAnalysisModel/*` | Modele d'analyse de traces |
| `/sceneTraceIdModel/*` | Gestion des ID de trace de scenario |
| `/sceneBranch/*` | Gestion des branches de scenario |
| `/sceneModelNode/*` | Gestion des noeuds de scenario |
| `/apiHeatModel/*` | Modele de chaleur API |
| `/traceHbase/*` | Requete de donnees de trace HBase |
| `/aresCollect/*` | Reception des donnees collectees |

## Structure du projet

```
src/main/java/io/deepcover/datacenter/
├── common/           # Classes communes (BusinessResult, ResultEnum, BaseJobHandler)
├── dal/              # Couche d'acces aux donnees
│   ├── dao/entity/   # Entites Neo4j
│   ├── dao/mapper/   # MyBatis Mapper
│   ├── dao/mysqlentity/  # Entites MySQL
│   └── dao/repository/   # Spring Data Neo4j Repository
├── deploy/           # Classe de lancement de l'application
└── service/          # Couche service
    ├── config/       # Classes de configuration
    ├── controller/   # Controleurs REST API
    ├── impl/         # Implementations de services
    ├── kafka/        # Consommateurs Kafka
    ├── mq/           # Consommateurs RocketMQ
    ├── scheduler/    # Taches planifiees
    └── utils/        # Classes utilitaires
```

## Contribuer

Les contributions sont les bienvenues ! Veuillez lire [CONTRIBUTING.md](../CONTRIBUTING.md) pour plus de details.

## Licence

Ce projet est sous licence [Apache License 2.0](../LICENSE).
