# 07-编码约定

本目录沉淀 **extend_platform 的项目编码约定**——"本项目具体怎么写"的事实层，
从真实代码归纳而来，随代码演进更新。

## 内容

| 文件 | 说明 |
| --- | --- |
| `编码约定.md` | 分层/响应/异常/金额/MyBatis/权限/幂等/Feign/日志/复用/安全/技术债，含 DO/DON'T 与代码为证 |

## 定位（与 superpowers、Cursor 规则的关系）

```text
怎么写得好的"纪律"  → 06-Superpowers技能方法/08-编码实现流程.md（TDD→约定→review→verification）
本项目具体怎么写"约定" → 本目录 编码约定.md（项目事实）
Cursor 写码时自动加载  → .cursor/rules/coding-standard.mdc（要点，指向本约定）
```

- **纪律靠 superpowers**（通用工程方法，不重写通用提示词）。
- **约定靠本目录**（项目事实，superpowers 给不了）。
- 二者在 `08-编码实现流程` 里串起来：红-绿-重构时，实现遵循本约定。

## 维护规则

1. 约定应始终反映真实代码；发现偏离先确认"改约定还是改代码"。
2. 新增/变更约定时同步更新 `.cursor/rules/coding-standard.mdc` 的要点。
3. `编码约定.md` §12 记录已知技术债，新代码不得沿袭。
