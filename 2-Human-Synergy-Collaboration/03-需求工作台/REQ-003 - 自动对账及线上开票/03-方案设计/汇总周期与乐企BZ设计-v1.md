# 汇总周期与乐企 BZ 设计 v1

需求：`REQ-003 ETC拓展平台结算服务费开票功能`

版本定位：2026-06-27，对齐 **extend_platform** 已实现逻辑，作为**全流程测试**与验收依据。

关联代码：

| 模块 | 路径 |
| --- | --- |
| 周期快照工具 | `etcplus-system/.../invoice/util/ReconBillInvoiceBzUtils.java` |
| 汇总创建 | `ReconBillServiceImpl#createReconBill` / `buildReconBill` |
| 开票申请 | `ReconBillServiceImpl#applyInvoiceInternal` → `copyToApply` |
| 发票执行 | `ReconBillInvoiceServiceImpl`（从 apply 拷贝 → `remark` → 乐企 `BZ`） |
| 销方配置 | `iface.leq.seller`（`application-dev.yml`，勿在 `.properties` 写中文） |

关联 SQL（按顺序执行）：

1. `extend_platform/sql/recon_bill_settlement_period_snapshot.sql` — 汇总主表
2. `extend_platform/sql/recon_bill_invoice_settlement_period.sql` — 申请头表 + 发票执行表

---

**修订 2026-07-01**：开票张数改为 `invoice_limit`；12 月「2 次」指**汇总单**次数，非开票 2 张。详见 `05-开发与联调/业务规则修订-2026-07-01.md`。

---

## 1. 业务规则

### 1.1 汇总周期与开票张数

| 场景 | `period_type` | 汇总单规则 | 单次开票张数 | 乐企 `BZ` 格式 | 示例 |
| --- | --- | --- | --- | --- | --- |
| 1–11 月 | `1`（按月） | 同商户、同业务性质、同月仅 **1 笔**有效汇总；须勾选该月全部可汇总结算单 | **1~N 张**，N ≤ `invoice_limit`（默认 1） | 单月 `yyyyMM` | `202606` |
| 12 月 | `2`（12 月自定义区间） | 同商户、同业务性质、12 月最多 **2 笔**汇总（见 §1.2） | **1~N 张**，N ≤ `invoice_limit`；同一申请多票 **BZ 相同** | 跨月 **`yyyyMMdd-yyyyMMdd`** | `20251201-20260430`、`20261201-20261215` |

说明：

1. **BZ = 汇总周期**，不是商户抬头里的「发票摘要」（`merchant_invoice.invoice_remark`）。
2. 财务票面样例：12 月跨月备注为 **`yyyyMMdd-yyyyMMdd`**（如 `20251201-20260430`）；1–11 月仍为 **`yyyyMM`**（如 `202606`）。**已对照真实乐企 PDF 确认（2026-07-05）。**
3. **`invoice_limit`** = 有效数电抬头数（`invoice_type ∈ {3,4}`），在商户入驻/开票信息维护时写入 `etcplus_merchant.invoice_limit`，汇总单创建时快照。
4. **不再**将单次开票上限写死为 2 张；L4 商户等可分开开 4 张（每张不同抬头）。

### 1.2 12 月汇总次数（与开票张数无关）

| 次序 | 可勾选结算单范围 |
| --- | --- |
| 第 1 笔汇总 | 12 月该业务性质下，**部分**已到账且未汇总的结算单 |
| 第 2 笔汇总 | **必须**勾选该业务性质下**全部剩余**已到账且未汇总结算单 |
| 第 3 笔及以后 | **拒绝**：「12月同一业务性质最多生成2笔服务费汇总」 |

代码：`ReconBillServiceImpl#validateMonthlyUniqueWhenRequired`、`#validateFullMonthSelectionWhenRequired`。

### 1.2 与乐企字段映射

| 乐企字段 | 系统来源 | 说明 |
| --- | --- | --- |
| `BZ` | `etcplus_recon_bill_invoice.remark` | 由 `settlement_period_start` + `settlement_mon_end` 格式化为 BZ 文本 |
| `GMF_MC` / `GMF_NSRSBH` | 申请明细购买方（商户抬头） | 与 BZ 无关 |
| `XSF_*` / `KPR` | 配置 `iface.leq.seller` | 平台固定销方；dev 联调可与测试企业税号一致 |
| `XMMC` | 固定 **`技术服务费`** | 乐企按 **SPBM** 渲染 PDF 前缀 `*生产生活服务*`；报文断言 `SPBM` + `XMMC` |
| `COMMON_FPKJ_XMXX` | 固定常量 + 明细金额 | 数量 `"1"`，税率 6% |

---

## 2. 数据流（写死快照 + 只拷贝）

```text
商户创建汇总单 (createReconBill)
  ├─ 写入 period_start_at、period_end_at（结束时间必落库）
  ├─ 计算并写死 settlement_period_start、settlement_mon_end
  └─ settlement_mon（划账月份，已有）

商户提交开票申请 (applyInvoiceInternal)
  ├─ copyToApply：从汇总单拷贝周期快照 → apply 头表
  └─ detail.remark = 格式化 BZ（与 apply 一致）

财务复审通过（HTTP 仅 audit_status=3）
  ↓
Job issueApprovedApplies → createInvoicesFromApply（幂等建票）
  ├─ invoice 拷贝 apply 周期字段 + detail.remark
  ├─ fpqqlsh = LHDF + format(applyId*10+splitSeq)
  ├─ request_payload.bz = invoice.remark
  └─ 重试开票：刷新销方快照与 payload；**fpqqlsh 与 BZ 仍用 invoice 已存值**
```

**原则：**

- **汇总创建时**计算并持久化区间（唯一真相源）。
- **申请 / 发票执行**只拷贝，**不再**根据结算明细重新 `resolve` 推导结束月。
- 本期为新功能上线，**无历史脏数据**；12 月跨月汇总在创建时**必须**写入 `period_end_at`。

---

## 3. 表结构与字段

### 3.1 汇总主表 `etcplus_recon_bill`（源头）

| 字段 | 类型 | 写入时机 | 说明 |
| --- | --- | --- | --- |
| `period_start_at` | datetime(3) | 创建汇总 | 对账时段开始 |
| `period_end_at` | datetime(3) | 创建汇总 | 对账时段结束；12 月跨月**必存** |
| `settlement_mon` | datetime(3) | 创建汇总 | 划账月份（当月第一天） |
| `settlement_period_start` | datetime(3) | 创建汇总 | BZ 起始月；1–11 月同 `settlement_mon` |
| `settlement_mon_end` | datetime(3) | 创建汇总 | BZ 结束月；仅 12 月跨月有值，否则 NULL |
| `period_type` | tinyint | 创建汇总 | `1=按月`，`2=12月自定义区间` |

### 3.2 开票申请头 `etcplus_recon_bill_invoice_apply`（拷贝）

| 字段 | 说明 |
| --- | --- |
| `settlement_mon` | 从汇总拷贝 |
| `settlement_period_start` | 从汇总拷贝 |
| `settlement_mon_end` | 从汇总拷贝 |
| `period_type` | 从汇总拷贝 |

### 3.3 拆票明细 `etcplus_recon_bill_invoice_detail`

| 字段 | 说明 |
| --- | --- |
| `invoice_summary` | 展示用，固定「服务费」（`LeqInvoiceConstants.XMMC`） |
| `remark` | **乐企 BZ 文本**，与 apply 周期一致；**不再**使用 `merchant_invoice.invoice_remark` |

### 3.4 发票执行 `etcplus_recon_bill_invoice`（拷贝）

| 字段 | 说明 |
| --- | --- |
| `settlement_mon` / `settlement_period_start` / `settlement_mon_end` / `period_type` | 从 apply 拷贝 |
| `remark` | 乐企 `BZ`；写入 `request_payload` 的 `bz` 字段 |

---

## 4. 代码逻辑摘要

### 4.1 汇总创建 `buildSnapshotAtCreate`

入参：`periodType`、`settlementMonth`（yyyyMM）、`periodStartAt`、`periodEndAt`。

| period_type | settlement_period_start | settlement_mon_end | invoiceBz |
| --- | --- | --- | --- |
| 1 | 划账月第一天 | NULL | `yyyyMM` |
| 2 | `periodStartAt` 归一化日 | 与起始不同月时取 `periodEndAt` 归一化日 | **`yyyyMMdd-yyyyMMdd`** 或单月 `yyyyMM` |

实现类：`ReconBillInvoiceBzUtils#buildInvoiceBz`（`extend_platform`）。

若请求未传 `periodStartAt` / `periodEndAt`，后端用所选结算明细 min/max 对账日期兜底（仍保证非空后落库）。

### 4.2 开票申请 `copyToApply`

从已持久化的汇总单读取 `settlement_period_start`、`settlement_mon_end`、`period_type`，写入 apply；**禁止**再调 `resolve(reconBill, settleDetails)`。

### 4.3 BZ 格式化 `buildInvoiceBz`

```text
period_type = 2 且 settlement_period_end 与 start 不同日（跨月区间）
  → CONCAT(format(start,'yyyyMMdd'), '-', format(end,'yyyyMMdd'))
否则
  → format(settlement_period_start,'yyyyMM')
```

代码：`ReconBillInvoiceBzUtils`；单测 `ReconBillInvoiceBzUtilsTest`；IT `ReconBillInvoiceDecemberBzFlowIT`。

---

## 5. 全流程测试用例（验收依据）

### 5.1 1–11 月单月汇总 + 开票

| 步骤 | 操作 | 预期（DB / 报文） |
| --- | --- | --- |
| T-BZ-01 | 选择某月全部结算单，创建汇总 | `period_type=1`；`period_end_at` 有值；`settlement_mon_end` 为 NULL |
| T-BZ-02 | 提交开票申请 | apply / detail.remark 与汇总一致 |
| T-BZ-03 | 三段审核通过 | `etcplus_recon_bill_invoice.remark` = `yyyyMM`（如 `202606`） |
| T-BZ-04 | 检查 `request_payload` | `"bz":"202606"`；无商户抬头摘要文案 |
| T-BZ-05 | 销方字段 | `xsfMc`/`kpr` 等为配置中文，非乱码（yml UTF-8） |

### 5.2 12 月跨月区间 + 拆两张票

| 步骤 | 操作 | 预期 |
| --- | --- | --- |
| T-BZ-11 | 12 月创建汇总，区间如 2025-12-01 ~ 2026-04-30 | `period_type=2`；`period_end_at` 为区间结束日；快照含起止日 |
| T-BZ-12 | 申请拆 2 张票（两抬头） | 两条 detail.remark **相同**：如 `20251201-20260430` |
| T-BZ-13 | 复审通过生成 2 条 invoice | 两条 `remark` 均为同一区间 BZ |
| T-BZ-14 | 乐企报文 | 两次 `GP_FPKJ` 的 `BZ` 一致且为 **`yyyyMMdd-yyyyMMdd`** |

### 5.3 12 月单月（区间起止同月）

| 步骤 | 操作 | 预期 |
| --- | --- | --- |
| T-BZ-21 | 12 月汇总但起止在同一月 | `settlement_mon_end` 为 NULL；BZ 为单月 `yyyyMM`（如 `202512`） |

### 5.4 重试与快照

| 步骤 | 操作 | 预期 |
| --- | --- | --- |
| T-BZ-31 | 失败票重试 | `fpqqlsh` 不变；`remark`/BZ 不变；销方配置变更后 `request_payload` 销方字段可更新 |
| T-BZ-32 | 汇总单创建后 | 修改配置**不应**改变已落库 apply/invoice 的周期快照 |

### 5.5 SQL 抽检语句（示例）

```sql
-- 汇总单周期
SELECT id, recon_bill_no, period_type,
       period_start_at, period_end_at,
       settlement_mon, settlement_period_start, settlement_mon_end
FROM etcplus_recon_bill
WHERE recon_bill_no = ?;

-- 申请与发票 BZ
SELECT a.apply_no, a.settlement_period_start, a.settlement_mon_end, a.period_type,
       d.remark AS detail_bz,
       i.remark AS invoice_bz,
       JSON_UNQUOTE(JSON_EXTRACT(i.request_payload, '$.bz')) AS payload_bz
FROM etcplus_recon_bill_invoice_apply a
LEFT JOIN etcplus_recon_bill_invoice_detail d ON d.apply_id = a.id
LEFT JOIN etcplus_recon_bill_invoice i ON i.apply_id = a.id
WHERE a.apply_no = ?;
```

---

## 6. 常见错误对照（测试排错）

| 现象 | 可能原因 | 检查点 |
| --- | --- | --- |
| BZ 为「乐企测试-抬头A(普票)」等商户摘要 | 旧逻辑用了 `invoice_summary` / `invoice_remark` | 应为汇总周期；查 `detail.remark` 来源 |
| BZ 为 `202512` 而非 `20251201-20260430` | 汇总创建未传 `periodEndAt` 或未落库 | 查 `etcplus_recon_bill.period_end_at`、`summary_period_*` |
| 销方中文乱码 | 销方中文写在 `.properties` | 仅用 yml `iface.leq.seller` |
| 两张票 BZ 不一致 | 不应出现 | 查是否同一 apply_id 下 remark 相同 |

---

## 7. 修订记录

| 日期 | 说明 |
| --- | --- |
| 2026-06-27 | 初版：汇总创建写死区间快照；apply/invoice 只拷贝；BZ 格式与全流程测试用例 |
| 2026-07-05 | **12 月跨月 BZ 改为 `yyyyMMdd-yyyyMMdd`**；XMMC=`技术服务费`；与 `ReconBillInvoiceBzUtils` 及真实 PDF 对齐 |
