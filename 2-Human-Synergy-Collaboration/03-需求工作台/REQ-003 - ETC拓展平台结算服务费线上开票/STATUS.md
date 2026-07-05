# STATUS

需求名称：REQ-003 ETC拓展平台结算服务费线上开票

需求等级：A 类资金与开票流程需求

当前阶段：**06-测试与验收**（05 开发主体已完成，进入验收收口）

更新时间：**2026-07-01**

设计基线：`03-方案设计/方案设计-v6.md`（2026-07-01 修订）、`03-方案设计/发票表设计-v6.md`（2026-07-01 修订）、**`05-开发与联调/业务规则修订-2026-07-01.md`**

代码仓库：`E:\Workspace\extend_platform`

负责人：待补充

---

## 1. 当前结论

1. **全链路已在代码中落地**：服务费汇总 → 商户开票申请（按 **`invoice_limit` 拆 1~N 张**）→ 运营 / 财务初审 / 财务复审 → **Job 后台首轮乐企开票** → 发票管理（查询 / 重试 / 下载）。
2. **业务规则（2026-07-01 冻结）**：**尾差不调平**（各行独立拆税，仅校验 JSHJ 合计）；开票张数 **不硬编码 2 张**，上限 = 商户 `invoice_limit`；12 月 **汇总** 最多 2 笔/业务性质（第 2 笔须全选剩余结算单）。详见 `05-开发与联调/业务规则修订-2026-07-01.md`。
3. **乐企对接已完成**：`GP_FPKJ` 测试环境真实开票成功；`GP_FPCX` 链路可达；`job-report` 定时轮询 `invoice_status=1` + **复审通过首轮开票 Job**（每 2 分钟）。
4. **apply 表当前确认为 27 列**（23 列精简基线 + 4 个周期快照字段）：审核快照与开票张级统计已迁至 `apply_audit_log` 与 `etcplus_recon_bill_invoice`，汇总周期快照用于乐企 `BZ`。
5. **单元测试**：`ReconBillInvoiceServiceImplTest`（38 项）、`ReconBillFpqqlshUtilsTest`、`JobReconBillInvoiceTaskTest`、`ReconBillServiceImplTest`（含 12 月汇总规则）已通过；审核流单测 **待补**。
6. **当前重点**：按 `06-测试与验收/本周验收步骤与数据脚本-v1.md` 执行人工验收；开票限额联调可用 `extend_platform/sql/merchant_invoice_limit_test_seed_cz20260701.sql`。

> 说明：本 STATUS 取代 2026-06-18 版中「本期不做审核」的冻结口径；以 `extend_platform` 实际代码为准。

---

## 2. 已实现（对照代码）

### 2.1 数据表

| 表 | 状态 | 说明 |
| --- | --- | --- |
| `etcplus_recon_bill` | ✅ | 服务费汇总单 |
| `etcplus_recon_bill_settle_detail_snapshot` | ✅ | 汇总绑定结算明细快照 |
| `etcplus_recon_bill_invoice_apply` | ✅ | 申请头表（27 列，当前态，含周期快照） |
| `etcplus_recon_bill_invoice_detail` | ✅ | 拆票明细（1~N 条/申请，N ≤ invoice_limit） |
| `etcplus_recon_bill_invoice_apply_audit_log` | ✅ | 审核历史（只追加） |
| `etcplus_recon_bill_invoice` | ✅ | 发票执行记录（含 `detail_id`、`split_seq`） |
| `etcplus_recon_bill_invoice_retry_log` | ✅ | 开票/重试日志 |

### 2.2 后端（etcplus-system）

| 模块 | 路径要点 | 状态 |
| --- | --- | --- |
| 汇总/申请/审核 | `business/recon/ReconBillServiceImpl` | ✅ |
| 乐企开票/重试/补偿 | `business/invoice/ReconBillInvoiceServiceImpl` | ✅ |
| 管理端汇总审核 | `manage/recon/ReconBillController` | ✅ |
| 管理端发票 | `manage/invoice/ReconBillInvoiceController` | ✅ |
| 商户端汇总/申请 | `split/merchant/recon/MhReconBillController` | ✅ |
| 商户端发票 | `split/merchant/invoice/MhReconBillInvoiceController` | ✅ |

**关键行为（代码，2026-06-28）**

- 财务复审通过 → **仅** `review()` 更新 `audit_status=3`；**不**在 HTTP 内调乐企。
- Job `issueApprovedApplies` → 扫描未认领 apply → 建 invoice（`fpqqlsh=LHDF…`）→ 行级 CAS 后 `GP_FPKJ` 一次。
- 重试：先 `GP_FPCX` 查询，未开票则复用原 `fpqqlsh` 与 `request_payload`；**后端允许 `invoice_status` 为 1 或 3**，商户前端按钮仅 `3` 展示「重新开票」。
- 商户 `apply/{id}/issue`：亦处理 `invoice_status=0`（Job 崩溃补跑）。
- `syncApplyIssueStatus`：仅回写 apply.`issue_status` / `finish_at`，不写 success/fail 计数。

### 2.3 乐企（etcplus-exchange）

| 项 | 状态 |
| --- | --- |
| `LeqInvoiceClient` / 双向 HTTPS | ✅ |
| `GP_FPKJ` / `GP_FPCX` | ✅ 联调通过 |
| `LeqInvoiceIntegrationTest` | ✅ 默认跳过 |

### 2.4 定时任务（etcplus-job-report）

| 项 | 状态 |
| --- | --- |
| `JobReconBillInvoiceTask.queryPendingResult` | ✅ |
| `JobReconBillInvoiceTask.issueApprovedApplies` | ✅ |
| Quartz 轮询 SQL | `sql/recon_bill_invoice_query_pending_job.sql` |
| Quartz 首轮开票 SQL | `sql/recon_bill_invoice_issue_approved_job.sql` |
| `uk_fpqqlsh` DDL | `sql/recon_bill_invoice_fpqqlsh_uk.sql` |

### 2.5 前端

| 端 | 页面 | 路径 | 状态 |
| --- | --- | --- | --- |
| 商户 | 服务费汇总/开票申请 | `etcplus-ui-merchant/.../merchantReconBill` | ✅ |
| 商户 | 发票管理 | `etcplus-ui-merchant/.../invoiceManage` | ✅ |
| 管理 | 汇总审核 | `etcplus-ui-manage/.../merchantReconBill` | ✅ 复审成功提示「后台自动开票」 |
| 管理 | 发票管理 | `etcplus-ui-manage/.../invoiceManage` | ✅ |

### 2.6 自动化测试

| 测试类 | 状态 | 说明 |
| --- | --- | --- |
| `ReconBillInvoiceServiceImplTest` | ✅ 通过 | 开票、重试、补偿、下载 |
| `ReconBillServiceImplTest` | ✅ 通过 | 汇总单创建、商户配置 |
| `ReconBillInvoiceFlowE2ETest` | 🟡 可选 | 需 dev 库 + exchange + 乐企证书 |
| 审核/apply 精简单测 | ❌ 待补 | `operationReview` 等未覆盖 |
| 前端交互自动化 | ❌ 未做 | TASK-008A |

---

## 3. 待完成

| 编号 | 事项 | 优先级 | 说明 |
| --- | --- | --- | --- |
| TODO-001 | **06 主链路人工验收** | P0 | 见本周验收步骤文档 |
| TODO-002 | 失败重试 / 3 次上限实机验证 | P0 | TC-201~203 |
| TODO-003 | PDF/OFD/XML 下载验证 | P0 | 需成功开票样例 |
| TODO-004 | 商户权限隔离 | P0 | TC-301~302 |
| TODO-005 | system 全量编译 + 单测回归 | P1 | `mvn -pl etcplus-modules/etcplus-system -am test` |
| TODO-006 | 补审核流单测 | P2 | review + audit_log + issue_status 汇总 |
| TODO-007 | 前后端重试规则对齐 | P2 | 后端允许 status=1，前端仅 3；产品确认是否改 |
| TODO-009 | 部署 `uk_fpqqlsh` + 首轮开票 Job SQL | P0 | 目标环境执行 DDL + Quartz 注册 |

---

## 4. 已知差异（文档/产品待确认）

| 项 | 文档/产品期望 | 当前代码 |
| --- | --- | --- |
| 重试按钮 | 仅失败(3)展示 | 商户前端 ✅；后端 retry 亦接受 status=1 |
| 首次开票入口 | 列表仅「重新开票」 | 商户汇总单列表已移除重开；仅发票管理重试 |
| 财务复审响应 | 秒回，后台 Job 开票 | HTTP 仅改审核态；2 分钟内 Job 建票 issue |
| 开票时间展示 | `invoice_date` | 列表 fallback `lastRequestAt` / `createAt` |
| 发票类型字典 | `invoice_type` 字典 | 前端映射乐企码 `030`/`032` |
| 尾差调平 | v6 初版强制调平 | **2026-07-01 已改为不调平**，见业务规则修订文档 |
| 开票张数上限 | 文档曾写「最多 2 张」 | **按 `invoice_limit`**，L4 等可 4 张 |

---

## 5. 风险与原则

| 编号 | 风险/原则 | 结论 |
| --- | --- | --- |
| RISK-001 | 乐企查询同步延迟 | 保留 `queryPendingResult` 补偿，勿将未命中判失败 |
| RISK-002 | 敏感信息 | 密钥/证书密码不得入仓、不得打日志 |
| RISK-003 | SQL 注入 | MyBatis 参数绑定 |
| RISK-004 | 列表大字段 | 禁止返回 `request_payload`、`item_detail_payload` |
| RISK-005 | 真实开票测试 | 集成测试默认关闭 |

---

## 6. 本周行动

1. 执行 `06-测试与验收/本周验收步骤与数据脚本-v1.md`。
2. 在 `06-测试与验收/测试验收清单-v1.md` 勾选 TC 状态。
3. 验收通过后进入 `07-复盘沉淀`，并关闭 TODO-001~005。

---

## 7. 关键代码索引（extend_platform）

完整路径见：`05-开发与联调/代码路径清单-2026-06-26.md`

```
etcplus-modules/etcplus-system/
  business/recon/          # 汇总单、申请、三段审核
  business/invoice/        # 乐企开票、重试、补偿
  manage/recon|invoice/    # 管理端 API
  split/merchant/recon|invoice/  # 商户端 API

etcplus-modules/etcplus-exchange/   # 乐企 HTTP
etcplus-modules/etcplus-job-report/ # 开票中轮询

etcplus-ui-merchant/src/views/trans/settle/
  merchantReconBill/  invoiceManage/
etcplus-ui-manage/src/views/settle/
  merchantReconBill/  settleManage/invoiceManage/

sql/
  recon_bill_invoice_fpqqlsh_uk.sql
  recon_bill_invoice_issue_approved_job.sql
  recon_bill_invoice_apply_v3.sql
  recon_bill_invoice_apply_drop_redundant_columns.sql  # 已执行
  recon_bill_invoice_apply_ui_test_seed.sql
  recon_bill_invoice_apply_test_seed.sql
  recon_bill_invoice_apply_test_seed_extend.sql
  merchant_invoice_limit_test_seed_cz20260701.sql   # 开票限额 L1/L2/L4 联调
```
