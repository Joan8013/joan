# 06-测试与验收

存放测试点、验收标准、上线检查和验收结论。

## 文档索引

| 文件 | 用途 |
| --- | --- |
| [`测试验收清单-v1.md`](测试验收清单-v1.md) | **主清单**（UAT 勾选 + 自动化 ✅ 标记） |
| [`本周验收步骤与数据脚本-v1.md`](本周验收步骤与数据脚本-v1.md) | 历史脚本顺序（D1~D3）；新种子见代码仓 `merchant-platform-test-seed.md` |

## 代码仓（executable）

路径：`E:\Workspace\extend_platform`

| 脚本 | 命令 |
| --- | --- |
| 后端 unit + IT | `.\scripts\run-invoice-tests-jdk8.ps1 -Mode verify` |
| 前端 Jest | `.\scripts\run-frontend-recon-tests.ps1 -Target all` |
| Playwright E2E | `cd e2e-tests; $env:E2E_INTEGRATION='true'; npm test` |
| PIT 变异（IMP-003） | 见 `06-测试与验收/测试可信度报告.md` §3 |
| 生产部署 TODO-009 | 工作台 `05-开发与联调/req003-生产部署清单-2026-07-05.md` |

详细索引：`extend_platform/docs/req-003/README.md`

## 本阶段输出

1. 主链路是否通过（UAT 剩余项见清单 §2）。
2. 异常链路是否覆盖（单测/IT ✅；UAT 待勾）。
3. 回归范围是否明确（§6、§9）。
4. 是否满足上线条件（§10–11；**L-F2 PDF 2026-07-05 已确认**）。
5. 自动化测试是否通过（**✅ verify + Jest**）。
