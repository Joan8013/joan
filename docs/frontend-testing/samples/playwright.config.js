// Playwright 配置模板（Vue2.6 + Element UI）
// 放到前端门户项目根目录，按需改 baseURL / webServer 命令。
// 三套门户可各放一份，或用 projects 区分。

const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './e2e',
  snapshotDir: './e2e/__screenshots__',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['html', { open: 'never' }], ['list']],

  use: {
    // 改成你门户 dev server 地址；不同门户不同端口
    baseURL: process.env.BASE_URL || 'http://localhost:8080',
    trace: 'on-first-retry',       // 失败重试时录 trace，方便排查
    screenshot: 'only-on-failure',
    locale: 'zh-CN',
  },

  // 让 Playwright 自动拉起前端 dev server（按你项目实际命令改）
  webServer: {
    command: 'npm run serve',
    url: process.env.BASE_URL || 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },

  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    // 需要多浏览器时再开：
    // { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
  ],
});
