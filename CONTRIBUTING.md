# 贡献指南

感谢你对 DeepCover Data Center 项目的关注！欢迎通过以下方式参与贡献。

## 开发环境

- **JDK**: 1.8+
- **Maven**: 3.5+
- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **数据库**: Neo4j 4.4+, MySQL 8.0+

## 构建与测试

```bash
# 编译项目
mvn clean compile

# 运行全部测试 (58 个用例)
mvn clean test

# 运行单个测试类
mvn test -Dtest=BusinessResultTest

# 打包 (跳过测试)
mvn clean package -DskipTests
```

## 代码规范

- 遵循 Java 命名规范
- 使用 Log4j2 进行日志记录，禁止使用 `e.printStackTrace()`
- Cypher 查询必须使用参数化绑定，禁止字符串拼接
- HBase/Neo4j 资源使用 try-with-resources 管理
- 新增代码需编写单元测试

## 提交流程

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/your-feature`)
3. 提交修改 (`git commit -m 'Add some feature'`)
4. 推送分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

### Commit Message 规范

使用简洁明了的中文或英文描述，格式参考：

```
feat: 新增 API 热度查询功能
fix: 修复场景模型查询空指针异常
refactor: 重构 HBase 连接管理
test: 新增 DateTimeUtil 单元测试
docs: 更新 README 文档
```

## 测试

项目当前包含 58 个单元测试，覆盖以下模块：

| 模块 | 测试数 | 说明 |
|------|--------|------|
| BusinessResult | 8 | 通用返回对象 |
| ResultEnum | 6 | 返回码枚举 |
| DateTimeUtil | 11 | 日期工具类 |
| ObjectConverterUtil | 12 | 对象转换工具 |
| Neo4jUtil | 7 | Neo4j 查询工具 |
| HbaseService | 7 | HBase 操作服务 |
| MessageHandleServiceImpl | 7 | 消息处理服务 |

提交 PR 前请确保所有测试通过：`mvn clean test`

## 问题反馈

- 使用 [GitHub Issues](../../issues) 提交 Bug 报告或功能建议
- 使用 Bug Report 模板描述问题
- 附上复现步骤和日志信息

## 许可证

提交代码即表示你同意代码在 Apache License 2.0 下授权。
