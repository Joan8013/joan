# extend_platform 多角色协同开发编排

单会话 subagent 编排：一个主 Agent 当 **协调者(Orchestrator)**，依次驱动
**产品经理 / 架构计划 / 开发 / 测试 / 代码审查** 五个角色，从需求走到
**“测试全绿 + 上线清单”**。风格：**高自动、少介入**。

## 目录

```
.cursor/rules/multi-agent-dev.mdc   # 编排主规范（放项目根 .cursor/rules/）
docs/agents/
├── README.md                       # 本文件
├── roles/                          # 各角色职责定义（子代理提示词依据）
│   ├── pm.md
│   ├── planner.md
│   ├── dev.md
│   ├── tester.md
│   └── reviewer.md
└── templates/                      # 各工件模板
    ├── spec.md
    ├── plan.md
    ├── changes.md
    ├── test-report.md
    ├── review.md
    ├── release-checklist.md
    └── status.json
```

## 依赖

本编排的测试环节复用测试规范 `.cursor/rules/testing.mdc` 与测试资产库
（见 `2-Human-Synergy-Collaboration/02-资产层/05-测试资产库/`）。两套一起放进项目效果最佳。

## 使用步骤

1. 把 `.cursor/rules/multi-agent-dev.mdc` 复制到 extend_platform 根目录的 `.cursor/rules/`。
2. 把 `docs/agents/`（roles + templates）复制到 extend_platform 的 `docs/agents/`。
3. 确保测试规范 `.cursor/rules/testing.mdc` 也已就位。
4. 在 Cursor 桌面版单会话里说：
   > “按多角色流程做这个需求：<需求描述>。目标到‘测试全绿 + 上线清单’为止。”
5. 主 Agent 作为 Orchestrator 自动跑完 S0→S6，工件落在项目 `.agents/` 目录，
   最后交给你验收。

## 工作流一览（状态机）

```
S0 INIT → S1 SPEC(PM) → S2 PLAN(Planner)
      → S3 BUILD: 每任务[ Dev → Tester → (≤3轮修) → Reviewer(≤2轮) ]
      → S4 REGRESS(全量回归+门禁, ≤2轮)
      → S5 CHECKLIST(生成上线清单)
      → S6 DONE(交人验收)
```

## 高自动 / 少介入

Orchestrator 默认全自动推进，仅在这些情况停下问人（记录在 status.json 的 pendingHuman）：
- 需求无法转成可测验收标准
- 同一任务 Dev↔Test 超 3 轮仍红
- 全量回归超 2 轮仍红
- 遇到不可逆/高风险动作（改表结构、删数据、未授权的资金逻辑变更）

## 设计要点（为什么这么设计）

- **不用裸 loop**：用状态机 + 有界循环，避免不收敛、上下文爆炸。
- **靠工件交接**：角色间只通过 `.agents/` 下的结构化文件传递，不靠聊天历史。
- **测试独立于开发**：Tester 只按验收标准测，不看实现下结论，避免自写自测自欺。
- **人只在关键点介入**：默认全自动，遇到无法自动决策才升级。
- **止步上线清单**：不执行部署，产出可交付运维的 checklist。

## 模型选型建议（按角色混搭）

金融场景下，迭代多的角色用快而省的 **Composer 2.5** 打主力，把关类角色用 **Opus** 兜底：

| 角色 | 推荐模型 |
|---|---|
| Orchestrator | Composer 2.5（预算足可用 Opus） |
| PM（验收标准） | **Opus 4.8** |
| Planner | Composer 2.5 |
| Dev（开发） | **Composer 2.5** |
| Tester（测试） | **Composer 2.5** |
| Reviewer（审查） | **Opus 4.8** |

- 省钱：可全程 Composer 2.5，但验收标准与审查两步需加强人工把关。
- 求稳：PM/Reviewer 步骤手动切 Opus。
- 建议先拿低风险小需求全程 Composer 2.5 跑通再调整。

详见 `.cursor/rules/multi-agent-dev.mdc` 第 8 节。

## 降级方案：单 Agent 简化版

若桌面版子代理调度不理想，可降级为“同一个 Agent 依次扮演各角色”，
状态机/工件交接/质量门/有界循环全部不变，靠 `.agents/` 文件保持状态。
详见 `.cursor/rules/multi-agent-dev.mdc` 第 9 节。

## 后期演进（云端多 Agent 并行）

`plan.md` 里相互独立的任务，后期可用云端 Cloud Agent 各起一个分支并行开发，
用 PR + status.json 协调。当前先在桌面版把串行流水线跑稳。
