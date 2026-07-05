# STATUS

需求名称：REQ-003 ETC拓展平台结算服务费线上开票

需求等级：A 类资金与开票流程需求

当前阶段：**06-测试与验收**（自动化测试与 L-F2 PDF 已确认，UAT 收口）

更新时间：**2026-07-05**

设计基线：`03-方案设计/方案设计.md`（现行，收敛 v1~v6）、`03-方案设计/发票表设计.md`（现行）、**`05-开发与联调/业务规则修订-2026-07-01.md`**、**`03-方案设计/汇总周期与乐企BZ设计-v1.md`**（2026-07-05 修订）

代码仓库：`E:\Workspace\extend_platform`（测试索引：`docs/req-003/README.md`）

负责人：待补充

---

## 1. 当前结论

1. **全链路已在代码中落地**：服务费汇总 → 商户开票申请（按 **`invoice_limit` 拆 1~N 张**）→ 运营 / 财务初审 / 财务复审 → **Job 后台首轮乐企开票** → 发票管理（查询 / 重试 / 下载）。
2. **业务规则（2026-07-01 冻结）**：**尾差不调平**（各行独立拆税，仅校验 JSHJ 合计）；开票张数 **不硬编码 2 张**，上限 = 商户 `invoice_limit`；12 月 **汇总** 最多 2 笔/业务性质（第 2 笔须全选剩余结算单）。详见 `05-开发与联调/业务规则修订-2026-07-01.md`。
3. **乐企对接已完成**：`GP_FPKJ` 测试环境真实开票成功；`GP_FPCX` 链路可达；`job-report` 定时轮询 `invoice_status=1` + **复审通过首轮开票 Job**（每 2 分钟）。
4. **apply 表当前确认为 27 列**（23 列精简基线 + 4 个周期快照字段）：审核快照与开票张级统计已迁至 `apply_audit_log` 与 `etcplus_recon_bill_invoice`，汇总周期快照用于乐企 `BZ`。
5. **单元 / 集成测试**：`run-invoice-tests-jdk8.ps1 -Mode verify` 已通过（含 FlowIT、驳回再申请 IT、12 月 BZ IT、商户审核同步 IT）；L-F1 报文 golden、L-F2 PDFBox 数值抽取单测已入库。
6. **L-F2 真实 PDF/OFD**：财务已从乐企下载 **1～3 张真实票面**，字段与模板一致，**2026-07-05 确认准确**。
7. **前端单测**：manage / merchant Jest（`run-frontend-recon-tests.ps1`）已覆盖审核权限、拆分、`invoice_limit`、价税/XMDJ。
8. **当前重点**：目标环境 **TODO-009** 部署；可选 UI 抽检（验收清单 §2）。

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

| 测试类 / 脚本 | 状态 | 说明 |
| --- | --- | --- |
| `ReconBillInvoiceServiceImplTest` | ✅ | 开票、重试、补偿、下载 |
| `ReconBillServiceImplTest` | ✅ | 汇总、申请、审核、幂等、12 月汇总规则 |
| `LeqIssuePayloadGoldenTest` | ✅ | L-F1 报文 golden（424/636/337/302/1337/1000/dec） |
| `ReconBillInvoiceBzUtilsTest` | ✅ | BZ 含 `yyyyMMdd-yyyyMMdd` |
| `ReconBillInvoiceFlowIT` / `FlowFailIT` | ✅ | WireMock 成功/失败全流程 |
| `ReconBillInvoiceRejectReapplyIT` | ✅ | 驳回释放 clientRequestId + 同 key 再申请 |
| `ReconBillInvoiceDecemberBzFlowIT` | ✅ | 12 月区间 remark / payload BZ |
| `MerchantInvoiceAuditSyncIT` | ✅ | 入驻审核落正式表 |
| `InvoiceFacePdfExtractTest` | ✅ | PDFBox 数值 token（非中文票面） |
| manage / merchant Jest | ✅ | `run-frontend-recon-tests.ps1` |
| `ReconBillInvoiceFlowE2EIT` | 🟡 可选 | dev 库 + exchange + 乐企证书；PDF 已人工验 |
| `ReconBillInvoiceAcceptanceIT` | ✅ | TODO-001/002/004 验收 IT |
| `MhReconBillInvoiceControllerAccessTest` | ✅ | TC-302 商户端 API 越权 |
| Playwright 全链路 E2E | ✅ | `recon-invoice-reject-reapply.spec.js`（需 `E2E_INTEGRATION=true`） |

---

## 3. 待完成

| 编号 | 事项 | 优先级 | 说明 |
| --- | --- | --- | --- |
| TODO-001 | **06 主链路 UAT** | ~~P0~~ **自动化 ✅** | `ReconBillInvoiceAcceptanceIT`；UI 抽检见验收清单 §2 |
| TODO-002 | 失败重试 / 3 次上限 | ~~P1~~ **✅** | `AcceptanceIT` + `ReconBillInvoiceServiceImplTest` |
| TODO-003 | PDF/OFD/XML 下载验证 | ~~P0~~ **✅** | **2026-07-05 财务确认真实票面准确** |
| TODO-004 | 商户权限隔离 | ~~P0~~ **自动化 ✅** | `AcceptanceIT` + `MhReconBillInvoiceControllerAccessTest` |
| TODO-005 | system verify 回归 | ~~P1~~ **✅** | `run-invoice-tests-jdk8.ps1 -Mode verify` |
| TODO-006 | 审核流 / 幂等 IT | ~~P2~~ **✅** | `ReconBillServiceImplTest` + `RejectReapplyIT` |
| TODO-007 | 前后端重试规则对齐 | ~~P2~~ **✅** | 产品口径 TC-008：前端仅 `invoice_status=3`；后端 1/3 为 API 容错；Jest `invoiceManageActions.spec.js` |
| TODO-008 | Playwright 全链路 E2E | ~~P2~~ **✅** | `e2e-tests/tests/recon-invoice-reject-reapply.spec.js` |
| TODO-009 | 部署 `uk_fpqqlsh` + 开票 Job | P0 **脚本 ✅** | `sql/req003_invoice_production_deploy.sql`；**目标环境待执行** |

---

## 4. 已知差异（文档/产品待确认）

| 项 | 文档/产品期望 | 当前代码 |
| --- | --- | --- |
| 重试按钮 | 仅失败(3)展示；开票中(1)不展示 | 商户前端 ✅ TC-008；后端 retry 接受 1/3 供接口容错 |
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

1. 目标环境执行 **TODO-009**（`05-开发与联调/req003-生产部署清单-2026-07-05.md`）。
2. 可选 UI 抽检验收清单 §2 剩余「UI 待测」项。
3. TODO-009 执行后在 verify 脚本通过后更新本 STATUS 与 `07-复盘沉淀/上线验收记录-2026-07-05.md`。

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
  req003_invoice_production_deploy.sql              # TODO-009 生产/预发合并部署
  req003_invoice_production_deploy_verify.sql       # TODO-009 部署验证
```
