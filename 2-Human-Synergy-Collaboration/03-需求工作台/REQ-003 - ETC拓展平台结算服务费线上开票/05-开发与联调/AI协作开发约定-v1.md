# REQ-003 AI 协作开发约定 v1

> **用途**：供后续 AI 助手与开发者在 `extend_platform` 上继续迭代 REQ-003（结算服务费线上开票）时统一遵循。  
> **代码仓库**：`E:\Workspace\extend_platform`  
> **同步副本**：`extend_platform/docs/req-003/AI协作开发约定.md`（内容应与本文件保持一致）  
> **更新时间**：2026-06-19

---

## 1. 适用范围

本约定适用于 REQ-003 相关模块，包括但不限于：

| 端 | 主要页面 / 模块 |
| --- | --- |
| 商户端 | `etcplus-ui-merchant/.../merchantReconBill`、`invoiceManage` |
| 运营端 | `etcplus-ui-manage/.../merchantReconBill`、`invoiceManage` |
| 后端 | `business/recon`、`business/invoice`、`manage/recon`、`split/merchant` |

**不在本约定范围**：其他需求的多级审核可参照第 5 节模板，但权限标识与菜单需独立命名。

---

## 2. 关键代码路径（AI 优先检索）

```
extend_platform/
├── etcplus-modules/etcplus-system/src/main/java/com/etcplus/system/
│   ├── business/recon/          # 汇总单、申请、拆票明细、审核
│   ├── business/invoice/        # 乐企开票执行、发票表
│   ├── manage/recon/            # 运营端 ReconBillController（含三级审核）
│   └── split/merchant/recon|invoice/  # 商户端 API
├── etcplus-modules/etcplus-system/src/main/resources/mapper/
│   ├── business/recon/          # ReconBillMapper、Apply、Detail
│   └── business/invoice/        # ReconBillInvoiceMapper
├── etcplus-ui-manage/src/
│   ├── views/settle/merchantReconBill/
│   ├── views/trans/settleManage/invoiceManage/
│   └── constants/reconBillInvoiceStatus.js
├── etcplus-ui-merchant/src/
│   ├── views/trans/settle/merchantReconBill/
│   ├── views/trans/settle/invoiceManage/
│   └── constants/reconBillInvoiceStatus.js
└── sql/
    ├── recon_bill_audit_menu_permissions.sql
    └── recon_bill_invoice_apply_*.sql
```

---

## 3. 通用编码约定

### 3.1 列表排序：SQL 端完成，前端不排序

**规则**

- 分页列表、详情接口返回的数组（如 `invoiceApplies`、`invoiceDetails`），**默认顺序在 MyBatis `ORDER BY` 中定义**。
- 前端禁止对服务端数据做 `.sort()` / `computed` 重排（审核历史时间线等纯前端组装数据除外，须在注释中说明原因）。
- 列表展示用的「主时间字段」须与 SQL 排序字段语义一致。

**推荐模式**

```sql
-- 业务时间 DESC + id DESC 作稳定次序
order by apply_at desc, id desc

-- 时间可空时与页面展示 fallback 对齐
order by coalesce(invoice_date, last_request_at, create_at) desc, id desc

-- 明细：先按申请时间，再按同申请内序号
order by ia.apply_at desc, ia.id desc, d.split_seq asc, d.id asc
```

**已落地示例**

| 场景 | Mapper | 排序 |
| --- | --- | --- |
| 汇总单列表 | `ReconBillMapper` | `generated_at desc, id desc` |
| 开票申请列表（详情） | `ReconBillInvoiceApplyMapper.selectInvoiceApplyList` | `apply_at desc, id desc` |
| 开票明细（详情） | `ReconBillInvoiceDetailMapper.selectInvoiceDetailList` | `apply_at desc, id desc, split_seq asc, id asc` |
| 发票管理列表 | `ReconBillInvoiceMapper.selectReconBillInvoiceVoList` | `coalesce(invoice_date, last_request_at, create_at) desc, id desc` |

### 3.2 状态 / 文案：集中 constants，页面只调用

**规则**

- 审核状态、开票状态、Tag 类型、行级派生逻辑统一放在：
  - 商户端：`etcplus-ui-merchant/src/constants/reconBillInvoiceStatus.js`
  - 运营端：`etcplus-ui-manage/src/constants/reconBillInvoiceStatus.js`
- 两文件应保持语义一致；运营端额外包含三级审核权限常量（见第 5 节）。
- 页面禁止散落 `if (status === 3)` 硬编码文案。

**常用函数**

| 函数 | 用途 |
| --- | --- |
| `reconBillIssueStatusText(row)` | 汇总单列表「开票状态」 |
| `invoiceApplyRowIssueStatusText(row)` | 申请单维度开票状态 |
| `reconInvoiceDetailIssueStatusText(detail, context)` | 开票明细行（结合审核状态） |
| `invoiceManageIssueStatusText(row)` | 发票管理列表 |
| `resolveInvoiceFailReason(row)` | 失败原因（成功/处理中不展示历史 fail_reason） |

### 3.3 Vue 2：模板函数必须挂到 `methods`

**规则**

从 `@/constants/*` 导入、并在 `<template>` 中调用的函数，**必须在组件 `methods` 中展开挂载**（仅 `import` 不够）。

```javascript
import { resolveInvoiceFailReason, reconBillIssueStatusText } from '@/constants/reconBillInvoiceStatus'

export default {
  methods: {
    resolveInvoiceFailReason,
    reconBillIssueStatusText,
    // ...
  }
}
```

**反例**：未挂 `methods` 导致表格渲染失败（如「失败原因」列空白）。

### 3.4 异步操作：Loading + 结构化接口返回

**规则**

- 用户确认后的耗时操作（如「重新开票」）必须显示全屏 `this.$loading({ lock: true, ... })`。
- 结果提示应依据**接口返回的状态字段**，而非统一「请求已提交」。
- 需要前端分支时，后端应返回明确字段（如 `{ invoiceStatus, failReason }`），避免前端猜测。

---

## 4. 领域展示约定：开票状态何时显示「-」

**业务原则**：审核未进入「已通过」或尚未实际开票时，**不展示「待开票」「开票失败」等执行态文案**，统一为 `-`。

| 场景 | 展示 |
| --- | --- |
| `apply.audit_status !== 3`（含审核中、驳回、取消） | `-` |
| 审核已通过，但 `invoice_status = 0` / 尚无乐企执行结果 | `-`（发票管理页） |
| 审核已通过且已有执行态（开票中 / 成功 / 失败） | 对应文案 |

**实现层次（三层一致）**

1. **SQL**：`audit_status != 3` 时派生 `issue_status` 返回 `NULL`（见 `InvoiceApplyIssueStatusCase`）。
2. **constants**：`resolveBillApplyIssueStatus` / `shouldHideInvoiceManageIssueStatus` 等在 `audit !== 3` 时返回 `null` → 文案 `-`。
3. **页面**：调用上述函数，禁止直接 `invoiceStatusText(row.invoiceStatus)` 绑定汇总单/申请行。

---

## 5. 三级审核与权限设计约定（必遵）

> **凡涉及多级审核的需求，必须同时设计：后端鉴权、前端按钮可见性、菜单权限 SQL 三处对齐。**  
> 仅隐藏按钮而不做后端 `@PreAuthorize` 视为不合格实现。

### 5.1 审核阶段与 `audit_status`

| audit_status | 含义 | 当前可操作角色 |
| --- | --- | --- |
| 0 | 待运营审核 | 运营 |
| 1 | 待财务初审 | 财务（初审） |
| 2 | 待财务复审 | 财务（复审） |
| 3 | 已通过 | —（进入开票流程） |
| 4 | 已驳回 | — |
| 5 | 已取消 | — |

**接口与 Service**

| 阶段 | HTTP | Service 方法 |
| --- | --- | --- |
| 运营审核 | `POST /reconBill/operationReview` | `operationReview` |
| 财务初审 | `POST /reconBill/financeFirstReview` | `financeFirstReview` |
| 财务复审 | `POST /reconBill/financeFinalReview` | `financeFinalReview` |

Controller：`etcplus-modules/.../manage/recon/controller/ReconBillController.java`

### 5.2 权限标识：三层命名对齐

| audit_status | sys_menu.perms | 说明 |
| --- | --- | --- |
| 0 | `reconBill:audit:operation` | 运营审核 |
| 1 | `reconBill:audit:financeFirst` | 财务初审 |
| 2 | `reconBill:audit:financeFinal` | 财务复审 |

**命名规则（扩展其它多级审核时沿用）**

```
{模块}:{动作}:{阶段}
例：reconBill:audit:operation
```

- `{模块}`：业务域，小驼峰或约定前缀（如 `reconBill`）。
- `{动作}`：固定 `audit` 表示审核类按钮。
- `{阶段}`：与 `audit_status` 或审核节点一一对应，**禁止**多个阶段共用一个 perm。

### 5.3 后端：接口必须 `@PreAuthorize`

每个审核提交接口**独立**声明权限，不得仅用登录态或角色名硬编码：

```java
@PreAuthorize(hasPermi = "reconBill:audit:operation")
@PostMapping("/operationReview")
public AjaxResult operationReview(@RequestBody ReconBillReviewRequest request) { ... }
```

**AI 检查**：新增审核节点时，是否新增对应 Controller 方法 + `@PreAuthorize` + Service 状态机校验。

### 5.4 前端：按钮可见性 = 状态 + 权限

**常量**（运营端 `reconBillInvoiceStatus.js`）：

```javascript
export const REVIEW_PERM_BY_AUDIT_STATUS = {
  0: 'reconBill:audit:operation',
  1: 'reconBill:audit:financeFirst',
  2: 'reconBill:audit:financeFinal'
}

export function reviewPermForAuditStatus(auditStatus) {
  return REVIEW_PERM_BY_AUDIT_STATUS[Number(auditStatus)] || null
}
```

**页面**（`merchantReconBill/index.vue`）：

```javascript
canReview(row) {
  const auditStatus = resolveBillApplyAuditStatus(row)
  if (auditStatus == null || ![0, 1, 2].includes(auditStatus)) {
    return false
  }
  const perm = reviewPermForAuditStatus(auditStatus)
  return !!perm && checkPermi([perm])
}
```

**规则**

- 「审核」按钮：`v-if="canReview(row)"`，**禁止**仅按 `audit_status` 显示而不校验 `checkPermi`。
- 打开审核弹窗前再次调用 `canReview`，无权限时提示「无当前阶段审核权限」。
- 商户端无审核能力，**不要**复制运营端审核按钮逻辑到商户端。

### 5.5 菜单权限 SQL

脚本：`extend_platform/sql/recon_bill_audit_menu_permissions.sql`

- 按钮菜单挂在页面菜单下：`component = settle/merchantReconBill`，`menu_type = F`（按钮）。
- 角色在「角色管理 → 菜单权限」勾选后生效；用户需**重新登录**刷新 `permissions`。
- 增删 perm 时注意清理误挂到其它父节点的历史按钮（脚本内已有 DELETE 防护）。

### 5.6 角色 / 用户保存副作用（已踩坑）

| 场景 | 约定 |
| --- | --- |
| 保存角色时 `menuIds == null` | **不得**清空原有菜单（后端应 skip wipe） |
| 保存用户时 `roleIds == null` | **不得**清空原有角色 |
| 角色保存未勾选任何菜单 | 前端应 warn，避免误提交空权限 |

涉及文件：`SysRoleServiceImpl`、`SysUserServiceImpl`、运营端 `role/index.vue`。

### 5.7 新增「第 N 级审核」扩展清单

AI 或开发者新增审核级时，按序检查：

- [ ] `audit_status` 枚举与状态机（Service）扩展
- [ ] 新 perm 命名并写入 `REVIEW_PERM_BY_AUDIT_STATUS`
- [ ] `ReconBillController` 新接口 + `@PreAuthorize`
- [ ] `sys_menu` 按钮 SQL（`menu_type = F`）
- [ ] 前端 `canReview` / `resolveReviewType` 映射
- [ ] 审核日志 `audit_log` 阶段字段
- [ ] 单测 / E2E（参考 `ReconBillInvoiceFlowE2ETest`）
- [ ] **禁止**运营与财务阶段共用同一 perm

---

## 6. SQL 派生字段约定

| 字段 | 规则 |
| --- | --- |
| `apply_issue_status`（列表子查询） | `audit_status != 3` → `NULL` |
| `issue_status`（申请详情） | 同上，`InvoiceApplyIssueStatusCase` |
| `fail_reason` | 开票成功时 UPDATE 置 `NULL`；SELECT 侧 `invoice_status = 2` 时不返回 |
| 汇总单 `status` | 由最新 `apply.audit_status` + 发票聚合派生，勿与 `apply_audit_status` 混用 |

---

## 7. 分层职责（避免 AI 改错表）

```
apply（申请头 + 拆票 detail）  →  审核、商户填票方案
        ↓ audit_status = 3
invoice（乐企执行）           →  FPQQLSH、开票状态、PDF/OFD
retry_log                     →  每次 GP_FPKJ/GP_FPCX 快照
```

- **禁止**在汇总列表放「手动调乐企开票」按钮（首轮由 Job / 复审通过后自动触发）。
- 商户「重新开票」仅针对 `invoice_status = 3` 且审核已通过记录。

---

## 8. AI 协助开发推荐流程

1. 先读 `STATUS.md` 与本约定，再读 `方案设计-v6.md`。
2. 改列表顺序 → 只改 Mapper XML，不改 Vue `.sort()`。
3. 改状态展示 → 先改 `reconBillInvoiceStatus.js`，再改页面绑定。
4. 改审核 → 必须同时检查 Controller 权限、前端 `canReview`、SQL 菜单。
5. 改商户/运营双端 → 常量与语义保持一致，权限逻辑仅运营端需要。
6. 提交前：相关单测 / 手工验收步骤见 `06-测试与验收/`。

---

## 9. 相关文档

| 文档 | 路径 |
| --- | --- |
| 需求 STATUS | `../STATUS.md` |
| 方案设计 v6 | `../03-方案设计/方案设计-v6.md` |
| 复审异步开票 | `../03-方案设计/复审异步开票与防重复设计-v1.md` |
| 代码路径清单 | `./代码路径清单-2026-06-26.md` |
| 验收步骤 | `../06-测试与验收/本周验收步骤与数据脚本-v1.md` |
| 审核菜单 SQL | `extend_platform/sql/recon_bill_audit_menu_permissions.sql` |

---

## 10. 变更记录

| 版本 | 日期 | 说明 |
| --- | --- | --- |
| v1 | 2026-06-19 | 初版：排序、状态展示、Vue/constants、三级审核权限、SQL 派生、AI 流程 |
