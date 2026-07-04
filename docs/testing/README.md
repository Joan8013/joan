# extend_platform 后端测试脚手架

这是一套面向 **extend_platform**（Java 8 / Spring Boot 2.3.7 / MyBatis / Spring Cloud）的测试体系，
目标是：**Agent 写测试、工具兜底、人只需快速审查断言**。

## 目录

```
.cursor/rules/testing.mdc         # 给 Cursor 的测试规范（放项目根目录 .cursor/rules/ 下）
docs/testing/
├── README.md                     # 本文件
├── pom-snippets.md               # Maven 依赖 + 插件配置（复制进 pom.xml）
└── samples/                      # 样板测试代码（复制进项目对应目录，改成你真实类名）
    ├── GoldenFile.java           # 黄金文件断言工具（零依赖）
    ├── AbstractMySqlIT.java      # 集成测试基类（Testcontainers 真实 MySQL8）
    ├── SettleServiceTest.java    # 单元测试样板（参数化表 + 守恒 + 幂等 + 异常 + 黄金文件）
    ├── SettleServiceIT.java      # 集成测试样板
    ├── LeqInvoiceClientTest.java # 外部对接测试样板（WireMock 打桩）
    ├── golden/
    │   └── 完整清分结果.json      # 黄金文件示例（人审查这个）
    └── db/
        └── schema-settle.sql     # 测试建表脚本示例
```

## 快速开始

1. **放规范**：把 `.cursor/rules/testing.mdc` 复制到你项目根目录的 `.cursor/rules/` 下。
2. **加依赖**：按 `docs/testing/pom-snippets.md` 把配置粘进父 pom 和业务模块 pom。
3. **放工具类**：把 `samples/GoldenFile.java` 复制到 `src/test/java/com/etcplus/test/`。
4. **写第一批测试**：在 Cursor 里说「按测试规范给 `SettleService` 补测试」，
   Agent 会先给你「测试意图表」→ 你批准 → 它实现 → 你审查黄金文件/数据表。
5. **首次跑测试**：`mvn test`，黄金文件会自动生成并让测试失败，
   你审查 `src/test/resources/golden/**` 里的 JSON，确认无误后 commit。
6. **开门禁**：稳定后 `mvn verify`（JaCoCo 覆盖率）+ 变异测试。

## 常用命令

```bash
mvn test                                              # 只跑单元测试(*Test)
mvn verify                                            # 单元+集成测试 + JaCoCo 覆盖率校验
mvn test org.pitest:pitest-maven:mutationCoverage    # 变异测试(验证测试是否真的有效)
mvn -Dgolden.update=true test                        # 确认预期变更后，重新生成黄金文件
```

## 人如何“快速审查断言”（本体系核心）

人只在两处介入，且都是「扫读」级别：

1. **审意图表**：Agent 先给一张「场景 + 期望结果」表格，你确认场景全不全、期望对不对（1~2 分钟）。
2. **审期望值**：
   - 多字段结果 → 看 **黄金文件 JSON**（像审一张对账单）
   - 规则逻辑 → 看 **参数化数据表**（每行一个场景+期望，横扫即可）

不需要逐行读测试代码。业务逻辑、安全、金额守恒这些机器判断不了的，才由人把关。

## 说明

- 本仓库不是 extend_platform 本体，这里的 `samples/*.java` 是**模板**，包名/类名（如 `SettleService`、
  `SettleMapper`、`SettleResult`、`SettleOrder`、`SettleOrderBuilder`）请对照你项目实际情况替换。
- 复制到真实项目后，`GoldenFile.java` 放 `src/test/java/...`，样板测试放对应模块的 `src/test/java/...`，
  黄金文件放 `src/test/resources/golden/...`，建表脚本放 `src/test/resources/db/...`。
