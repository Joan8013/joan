# extend_platform 前端自动化测试（Playwright）

面向三套 Vue 2.6 + Element UI 门户（manage / merchant / restarea）的前端测试体系。
核心：**AI 生成/维护脚本，机器稳定回归，人只审意图表 + 截图快照。**

## 范围与对齐

**本次仅覆盖 manage（:8081）与 merchant（:8082）两端**，restarea 暂不做。

- E2E 已有根目录独立包 **`e2e-tests`（Playwright ^1.61.1）** → **所有 E2E 集中放这里**，用 projects 区分两端。
- `etcplus-ui-manage` 已有 **Jest** → 组件测试沿用；`merchant` 无，需 `vue add unit-jest`。
- Cypress 未安装 → 保持不装，全用 Playwright。

目标结构：

```
e2e-tests/                             # 根目录已存在，E2E 集中于此
├── playwright.config.js              # 两门户 projects 配置
└── tests/
    ├── support/apiMock.js            # API 打桩 helper
    ├── manage/                        # 运营端 E2E (baseURL :8081)
    └── merchant/                      # 商户端 E2E (baseURL :8082)
etcplus-ui-manage/tests/unit/          # 组件测试(已有 Jest)
etcplus-ui-merchant/tests/unit/        # 组件测试(需补 Jest)
```

## 本目录内容

```
.cursor/rules/frontend-testing.mdc     # 前端测试规范（放项目 .cursor/rules/）
docs/frontend-testing/
├── README.md                          # 本文件
└── samples/
    ├── playwright.config.js           # 三门户 projects 配置（覆盖 e2e-tests 的配置）
    ├── deps-and-scripts.md            # 对齐现状的安装/脚本/MCP 说明
    ├── support/apiMock.js             # API 打桩 helper（放 e2e-tests/tests/support/）
    └── merchant/login.spec.js         # 商户端登录 E2E 样板（放 e2e-tests/tests/merchant/）
```

## “AI 自动化测试”的三个层次

| 层次 | 谁在测 | 用途 | 稳定性 |
|---|---|---|---|
| L1 AI 写脚本 | AI 生成 Playwright，机器重复跑 | 回归（首选） | 高 |
| L2 AI 驱动浏览器 | AI 实时点页面(Playwright MCP) | 探索/生成用例 | 低 |
| L3 AI 判定 | AI 看截图/diff | 视觉验证 | 中 |

**原则：L2 探索生成用例 → 转成 L1 确定性脚本做回归。** 不用 AI 每次实时点击跑回归。

## 快速开始（对齐现状）

1. 复制 `.cursor/rules/frontend-testing.mdc` 到仓库根 `.cursor/rules/`。
2. 用 `samples/playwright.config.js` 覆盖/对齐 `e2e-tests/playwright.config.js`，按端口改三端 baseURL。
3. 建目录 `e2e-tests/tests/{support,manage,merchant,restarea}/`，把 `samples/support/apiMock.js` 放到 `e2e-tests/tests/support/`。
4. 参考 `samples/merchant/login.spec.js` 放到 `e2e-tests/tests/merchant/`，写第一条 E2E（先让 AI 出“测试意图表”）。
5. 启动对应门户 dev server（manage :8081 / merchant :8082），`npm run test:e2e:merchant` 跑。
6. 首次跑视觉快照会生成基准图，人工审查后 `git add` 入库。
7. 组件测试：manage 直接补 `tests/unit/`；merchant 先 `vue add unit-jest`。

## 关键约定（详见规范）

- **E2E 必须打桩后端**（`page.route()` / apiMock helper），不真连微服务。
- **禁止脆弱定位**：优先 `getByRole`/`getByLabel`/文本；建议关键元素加 `data-test`。
- **视觉快照**：遮罩动态区域，基准图人工审查后入库。
- **回归用确定性脚本**，AI 实时点击只用于探索。

## 与多角色编排打通

前端 Tester 角色见 `docs/agents/roles/frontend-tester.md`，
测试环节遵循本规范；人快速审查方式与后端一致（意图表 + 快照）。
