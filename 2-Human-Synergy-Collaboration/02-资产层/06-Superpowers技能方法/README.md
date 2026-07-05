# 06-Superpowers技能方法

本目录用于吸收 Superpowers 的核心思想：

```text
Process over Prompt
流程大于提示词
```

Superpowers 在本体系中的定位是：

```text
AI 执行方法层
```

它不替代：

1. 风险分级。
2. 阶段契约。
3. 检查点完成标准。
4. 强门禁。
5. Owner 权责边界。
6. AI 自我评估。
7. 人工确认和人工上线。

## 1. 使用顺序

```text
先看治理层和流程层
再确认当前阶段和风险等级
再选择 Superpowers 技能方法
最后把产物沉淀回需求工作台
```

## 2. 技能接入表

| Superpowers 技能 | 本体系接入状态 | 适用阶段 |
| --- | --- | --- |
| `using-superpowers` | 已接入 | 技能选择总入口 |
| `brainstorming` | 已接入 | 需求澄清、影响分析、方案讨论 |
| `writing-plans` | 已接入 | 任务拆分与测试策略 |
| `test-driven-development` | 已接入 | 开发与联调阶段的任务级 TDD 循环 |
| `systematic-debugging` | 已接入 | 缺陷修复、联调失败、CI 失败 |
| `requesting-code-review` | 已接入 | 发起代码评审 |
| `receiving-code-review` | 已接入 | 处理评审意见 |
| `verification-before-completion` | 已接入 | 完成前验证 |
| `subagent-driven-development` | 限制接入 | 边界清晰的并行开发 |
| `dispatching-parallel-agents` | 限制接入 | 大任务并行分析或实现 |
| `finishing-a-development-branch` | 限制接入 | 开发分支收尾 |
| `executing-plans` | 暂缓 | 后续视团队需要接入 |
| `writing-skills` | 暂缓 | 后续扩展技能资产时使用 |
| `using-git-worktrees` | 暂缓 | 后续分支/工作树策略成熟后使用 |

## 3. 当前技能卡

| 文件 | 说明 |
| --- | --- |
| `00-技能选择总入口.md` | 如何选择技能 |
| `01-brainstorming需求澄清与方案探索.md` | 澄清、探索、对比 |
| `02-writing-plans任务拆分.md` | 任务拆分和执行计划 |
| `03-test-driven-development任务级TDD.md` | 开发任务内部红-绿-重构 |
| `04-systematic-debugging系统化调试.md` | 缺陷根因定位 |
| `05-code-review评审闭环.md` | 请求评审和处理评审意见 |
| `06-verification-before-completion完成前验证.md` | 宣称完成前验证 |
| `07-parallel-agents并行代理限制规则.md` | 子代理/并行代理使用边界 |
| `08-编码实现流程.md` | 开发阶段编码链：TDD→编码约定→评审→完成前验证（挂接 `07-编码约定`） |

## 4. 一句话规则

```text
Superpowers 只能强化当前阶段的执行纪律，不能替代项目组交付流程和人工决策。
```
