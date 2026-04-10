# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/lang/zh-CN/).

## [1.0.0] - 2026-04-09

### 首次开源发布

#### 新增

- 完全开源发布，采用 Apache 2.0 许可证
- 完整文档: README.md、CONTRIBUTING.md、CHANGELOG.md、LICENSE
- GitHub Issue 和 PR 模板 (.github/)
- GitHub Actions CI 自动构建测试，支持 Java 8/11/17 矩阵
- 多语言 README: 6 种语言版本 (中文/EN/JA/FR/PT/RU)
- 社区规范文件: CODE_OF_CONDUCT.md, SECURITY.md
- Maven Wrapper (`mvnw`) 支持无预装 Maven 构建
- Apache 2.0 License 头部添加到所有 Java 源文件
- 85 个单元测试覆盖核心模块

#### 安全修复 (CRITICAL)

- Cypher 注入漏洞修复: 15+ 处字符串拼接改为参数化查询
- HBase 资源泄漏修复: 改用 try-with-resources 模式
- 明文凭据脱敏: 所有配置文件中的密码/密钥替换为占位符
- 删除调试端点 setLinkDate1

#### 稳定性修复 (HIGH)

- HashMap 替换为 ConcurrentHashMap (ObjectConverterUtil)
- NPE 防护: 异常消息 null 检查、List.get(0) 空检查
- ExecutorService 添加 DisposableBean 销毁回调
- ConcurrentHashMap 去重缓存添加大小上限
- null 值安全处理: MessageHandleServiceImpl traceId 空值过滤
- e.printStackTrace() 全部替换为 log.error (10+ 文件)
- 线程池拒绝策略: DiscardOldestPolicy 改为 CallerRunsPolicy

#### 代码优化 (MEDIUM/LOW)

- 匿名内部类初始化改为 static 块 (AuditLogServiceImpl)
- 空 catch 块添加日志记录 (AliyunLogClient)
- 日志级别修正: info 改为 warn/error
- finally 块中 return 语句移除 (HandleDeleteDataJob)
- 字符串拼接改为 StringBuilder (DateTimeUtil)
- ObjectMapper 静态复用 (ObjectConverterUtil)
- HBase readVersions 从 1000 降为 10
- 移除重复 @Slf4j 注解 (MessageHandleServiceImpl)
- 清理注释代码和未使用 import

#### 品牌替换

- 包名: com.timevale.aresdatacenter -> io.deepcover.datacenter
- 移除内部依赖: schedulerT-client, puppeteer-client, hydra-core, esign-mq-core
- 新增替换类: BusinessResult, ResultEnum, BaseJobHandler
- Swagger 联系邮箱更新为 deepcover@example.com
- log4j2 logger 名称统一更新

---

## [Unreleased]

### 计划中

- 支持更多数据源配置
- 性能监控仪表盘
- 精准分析报告生成 API

---

贡献方式请查看 [CONTRIBUTING.md](CONTRIBUTING.md)。
