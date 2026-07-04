# extend_platform 前端自动化测试（Playwright）

面向三套 Vue 2.6 + Element UI 门户（manage / merchant / restarea）的前端测试体系。
核心：**AI 生成/维护脚本，机器稳定回归，人只审意图表 + 截图快照。**

## 目录

```
.cursor/rules/frontend-testing.mdc     # 前端测试规范（放项目 .cursor/rules/）
docs/frontend-testing/
├── README.md                          # 本文件
└── samples/
    ├── playwright.config.js           # Playwright 配置模板
    ├── deps-and-scripts.md            # 依赖安装 + package.json 脚本 + MCP 说明
    ├── support/apiMock.js             # API 打桩 helper（E2E 脱离后端）
    └── merchant/login.spec.js         # 商户端登录 E2E 样板（含视觉快照）
```

## “AI 自动化测试”的三个层次

| 层次 | 谁在测 | 用途 | 稳定性 |
|---|---|---|---|
| L1 AI 写脚本 | AI 生成 Playwright，机器重复跑 | 回归（首选） | 高 |
| L2 AI 驱动浏览器 | AI 实时点页面(Playwright MCP) | 探索/生成用例 | 低 |
| L3 AI 判定 | AI 看截图/diff | 视觉验证 | 中 |

**原则：L2 探索生成用例 → 转成 L1 确定性脚本做回归。** 不用 AI 每次实时点击跑回归。

## 快速开始

1. 复制 `.cursor/rules/frontend-testing.mdc` 到门户项目根 `.cursor/rules/`。
2. 按 `samples/deps-and-scripts.md` 安装 Playwright、加 package.json 脚本。
3. 用 `samples/playwright.config.js` 覆盖配置，改 baseURL 与 `npm run serve` 命令。
4. 把 `samples/support/apiMock.js` 放到 `e2e/support/`。
5. 参考 `samples/merchant/login.spec.js` 写第一条 E2E（先让 AI 出“测试意图表”）。
6. 首次跑视觉快照会生成基准图，人工审查后 `git add` 入库。

## 关键约定（详见规范）

- **E2E 必须打桩后端**（`page.route()` / apiMock helper），不真连微服务。
- **禁止脆弱定位**：优先 `getByRole`/`getByLabel`/文本；建议关键元素加 `data-test`。
- **视觉快照**：遮罩动态区域，基准图人工审查后入库。
- **回归用确定性脚本**，AI 实时点击只用于探索。

## 与多角色编排打通

前端 Tester 角色见 `docs/agents/roles/frontend-tester.md`，
测试环节遵循本规范；人快速审查方式与后端一致（意图表 + 快照）。
