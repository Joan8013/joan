# Maven POM 模板说明

本目录提供 **可直接对照合并** 的完整 POM 文件，替代 `pom-snippets.md` 中的零散片段。

| 文件 | 用途 |
|---|---|
| [parent-testing.xml](parent-testing.xml) | 根 POM：测试版本号、BOM、Surefire/Failsafe/JaCoCo/PIT 插件与 Profile |
| [module-testing.xml](module-testing.xml) | 业务模块：test scope 依赖（Testcontainers / WireMock / spring-boot-starter-test） |

## 合并步骤（extend_platform）

1. 打开项目根 `pom.xml` 与 `pom/parent-testing.xml` 对照。
2. 将 **properties**（testcontainers、wiremock、jacoco、pitest 等）并入根 pom 现有 `<properties>`。
3. 将 **testcontainers-bom**、**wiremock-jre8** 并入 `<dependencyManagement>`。
4. 将 **pluginManagement** + **build/plugins**（surefire、failsafe、jacoco）并入根 pom `<build>`。
5. 按需启用 **profiles**：`-Pcoverage-gate`、`-Pmutation-test`、`-Pskip-it`。
6. 打开目标业务模块 pom（如 `etcplus-modules/etcplus-system/pom.xml`），把 `module-testing.xml` 里 `<dependencies>` 合并进去。

## 常用命令

```bash
mvn test                              # 单元测试 *Test
mvn verify                            # 单元 + 集成 *IT + JaCoCo 报告
mvn verify -Pcoverage-gate            # 附加行/分支覆盖率门禁
mvn -Pmutation-test org.pitest:pitest-maven:mutationCoverage
mvn test -Pskip-it                    # 跳过 *IT（快速 CI）
mvn -Dgolden.update=true test         # 更新黄金文件后人工审查
```

## 与 snippets 的关系

- `pom-snippets.md` 保留为快速查阅的片段说明。
- **以本目录 XML 为准**；版本或插件有变更时同步更新两处。
