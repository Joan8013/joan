# 前端测试依赖与脚本（对齐现有结构）

## 现状（据仓库检查）

| 项目 | 单元测试 | E2E | 说明 |
|---|---|---|---|
| etcplus-ui-manage | ✅ Jest + @vue/test-utils | ❌ | 已有 jest.config.js，脚本 `npm run test:unit` |
| etcplus-ui-merchant | ❌ | ❌ | 只有 lint/dev/build |
| etcplus-ui-restarea | ❌ | ❌ | 同上 |
| **e2e-tests（根目录独立包）** | — | ✅ Playwright ^1.61.1 | 已有 playwright.config.js |
| Cypress | 未安装 | — | 保持不装，全用 Playwright |

**结论：E2E 全部集中在根目录 `e2e-tests` 包；组件测试放各门户各自的 Jest。**

## 1. E2E（集中在根目录 e2e-tests 包）

Playwright 已装，无需重复安装。只需：

1. 用本目录 `playwright.config.js` 覆盖/对齐 `e2e-tests/playwright.config.js`
   （已改为三门户 projects 结构，按端口改 baseURL）。
2. 建目录：
   ```
   e2e-tests/
   ├── playwright.config.js
   └── tests/
       ├── support/apiMock.js        # 打桩 helper
       ├── manage/                    # 运营端用例
       ├── merchant/                  # 商户端用例
       └── restarea/                  # 服务区用例
   ```
3. 确保浏览器已装：`npx playwright install --with-deps chromium`（在 e2e-tests 目录执行）。

### e2e-tests/package.json 脚本

```json
{
  "scripts": {
    "test:e2e": "playwright test",
    "test:e2e:manage": "playwright test --project=manage",
    "test:e2e:merchant": "playwright test --project=merchant",
    "test:e2e:restarea": "playwright test --project=restarea",
    "test:e2e:ui": "playwright test --ui",
    "test:e2e:update": "playwright test --update-snapshots",
    "test:e2e:codegen": "playwright codegen http://localhost:8080",
    "test:e2e:report": "playwright show-report"
  }
}
```

跑单个门户：`npm run test:e2e:merchant`（需该门户 dev server 已启动，或用 config 里的 webServer）。

## 2. 组件测试（各门户 Jest）

- **etcplus-ui-manage**：已有 Jest，直接在 `tests/unit/` 补用例，`npm run test:unit`。
- **etcplus-ui-merchant / restarea**：尚无，补 Jest：
  ```bash
  # 在对应门户目录执行
  vue add unit-jest
  ```
  会带来 `@vue/cli-plugin-unit-jest` + `@vue/test-utils@1`（Vue2 版），脚本 `npm run test:unit`。

## 3. Playwright MCP（让 Cursor 里的 AI 驱动浏览器）

在 Cursor 的 MCP 配置里添加 Playwright MCP server，AI 即可实时打开页面、点击、截图，
用于探索性测试与用例生成。产出务必转成 `e2e-tests/tests/**/*.spec.js` 确定性脚本用于回归。

## 4. CI 注意

- 在 e2e-tests 目录 `npx playwright install --with-deps chromium`。
- 视觉快照基准图建议在与 CI 一致的环境（容器）里生成，避免跨平台渲染差异。
- CI 里前端用**构建产物 + 静态服务**（而非 vue-cli serve）更快更稳。
