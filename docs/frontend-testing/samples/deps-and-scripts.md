# 前端测试依赖与脚本（Playwright，Vue2.6）

## 1. 安装 Playwright（在每个门户项目根目录执行）

```bash
# 初始化（会生成 playwright.config、示例、并安装浏览器）
npm init playwright@latest

# 或手动安装
npm i -D @playwright/test
npx playwright install --with-deps chromium
```

安装后用本仓库 `samples/playwright.config.js` 覆盖生成的配置（按门户改 baseURL/命令）。

## 2. package.json 脚本

```json
{
  "scripts": {
    "test:e2e": "playwright test",
    "test:e2e:ui": "playwright test --ui",
    "test:e2e:headed": "playwright test --headed",
    "test:e2e:update": "playwright test --update-snapshots",
    "test:e2e:codegen": "playwright codegen http://localhost:8080",
    "test:e2e:report": "playwright show-report"
  }
}
```

- `test:e2e` 跑全部 E2E（含视觉快照）
- `test:e2e:update` 确认预期变更后，更新截图基准图（更新后需人工审查）
- `test:e2e:codegen` 录制操作自动生成脚本（配合 AI 修整）
- `test:e2e:ui` 可视化调试

## 3. 组件测试（Vue Test Utils v1 + Jest，可选）

Vue CLI 项目直接加插件：

```bash
vue add unit-jest
```

会带来 `@vue/cli-plugin-unit-jest` + `@vue/test-utils@1`（Vue2 对应版本）。
组件测试放 `tests/unit/`，命令 `npm run test:unit`。

## 4. Playwright MCP（让 Cursor 里的 AI 驱动浏览器）

在 Cursor 的 MCP 配置里添加 Playwright MCP server（`cursor.com/agents` 或设置里的 MCP），
之后可让 AI 实时打开页面、点击、截图，用于探索性测试与用例生成。
产出务必转成 `e2e/**/*.spec.js` 确定性脚本用于回归。

## 5. CI 注意

- CI 里 `npx playwright install --with-deps chromium` 装依赖
- 视觉快照的基准图在不同 OS/渲染下可能有差异，建议基准图在与 CI 一致的环境（或容器）里生成
