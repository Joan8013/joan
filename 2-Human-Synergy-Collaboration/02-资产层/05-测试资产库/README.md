# 05-测试资产库

本目录沉淀可复用的**测试规范、脚手架、样板与测试场景**，供各需求在开发与测试阶段直接复用。
后端与前端的可复用测试资产已统一收纳于此（原 `docs/testing`、`docs/frontend-testing` 已迁入）。

## 目录导航

| 子目录/文件 | 内容 |
| --- | --- |
| `后端/README.md` | 后端测试脚手架总览与使用步骤（Java8 / SpringBoot2.3 / MyBatis） |
| `后端/pom模板/` | Maven 测试依赖与插件模板：`parent-testing.xml`、`module-testing.xml`、`pom-snippets.md`（JUnit5 / AssertJ / Testcontainers / WireMock / JaCoCo / PIT，含 profiles） |
| `后端/样板测试/` | 可复制样板：`GoldenFile.java`（黄金文件工具）、`AbstractMySqlIT.java`、`SettleServiceTest/IT.java`、`LeqInvoiceClientTest.java`、`golden/`、`db/` |
| `前端/README.md` | 前端 Playwright 测试脚手架总览（Vue2.6 + Element UI，manage/merchant 两端） |
| `前端/samples/` | `playwright.config.js`、`support/apiMock.js`（API 打桩）、`merchant/login.spec.js`、`deps-and-scripts.md` |

> 配套的 Cursor 规范文件不在本目录（需保留在项目根 `.cursor/rules/` 才生效）：
> `.cursor/rules/testing.mdc`（后端）、`.cursor/rules/frontend-testing.mdc`（前端）。

## 建议沉淀内容（测试场景）

| 类型 | 示例 |
| --- | --- |
| 主链路场景 | 正常扣费、正常清分、正常结算、正常开票 |
| 异常链路场景 | 扣费失败、外部接口超时、重复回调、状态不一致 |
| 权限场景 | 商户隔离、停车场隔离、加油站隔离、管理端权限 |
| 幂等场景 | 重复请求、重复点击、定时任务重跑、补偿任务重复执行 |
| 数据一致性场景 | 交易、清分、结算、对账、发票金额一致性 |
| 回归场景 | 需求影响的旧接口、旧页面、旧报表 |

## 使用原则

1. A 类需求必须优先复用本目录的高风险测试场景与样板。
2. 新发现的缺陷应沉淀为后续回归场景。
3. 测试数据可以记录构造方式，但不得写入生产敏感数据。
4. 后端测试遵循 `.cursor/rules/testing.mdc`，前端遵循 `.cursor/rules/frontend-testing.mdc`。
