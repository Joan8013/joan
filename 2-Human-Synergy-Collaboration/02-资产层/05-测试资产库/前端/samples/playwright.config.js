// Playwright 配置模板（集中式：放在根目录 e2e-tests 包，一套管两端）
// 你的仓库已有根目录 e2e-tests 独立包 + Playwright，本文件用于覆盖/对齐其配置。
//
// 仅覆盖 manage 与 merchant 两端，用 projects 区分，各自 baseURL + testDir。

const { defineConfig, devices } = require('@playwright/test');

// 各门户地址（可用环境变量覆盖）
const MANAGE_URL = process.env.MANAGE_URL || 'http://localhost:8081';
const MERCHANT_URL = process.env.MERCHANT_URL || 'http://localhost:8082';

module.exports = defineConfig({
  testDir: './tests',
  snapshotDir: './__screenshots__',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['html', { open: 'never' }], ['list']],

  use: {
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    locale: 'zh-CN',
  },

  // 按门户分 project：每个 project 只跑自己目录的用例，用自己的 baseURL
  projects: [
    {
      name: 'manage',
      testDir: './tests/manage',
      use: { ...devices['Desktop Chrome'], baseURL: MANAGE_URL },
    },
    {
      name: 'merchant',
      testDir: './tests/merchant',
      use: { ...devices['Desktop Chrome'], baseURL: MERCHANT_URL },
    },
  ],

  // 可选：让 Playwright 自动拉起两套 dev server（本地调试用；CI 建议改为构建产物静态服务）
  // webServer: [
  //   { command: 'npm --prefix ../etcplus-ui-manage run serve',   url: MANAGE_URL,   reuseExistingServer: !process.env.CI, timeout: 180000 },
  //   { command: 'npm --prefix ../etcplus-ui-merchant run serve', url: MERCHANT_URL, reuseExistingServer: !process.env.CI, timeout: 180000 },
  // ],
});
