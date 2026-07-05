// 商户端登录 E2E 样板（Vue2.6 + Element UI）
// 集中式结构：复制到根目录 e2e-tests/tests/merchant/login.spec.js。
// apiMock helper 放 e2e-tests/tests/support/apiMock.js。按真实页面/接口调整。

const { test, expect } = require('@playwright/test');
const { mockApi, failOnConsoleError } = require('../support/apiMock');

test.describe('商户端登录', () => {

  test('输入正确账号密码应登录成功并进入首页', async ({ page }, testInfo) => {
    failOnConsoleError(page, testInfo);

    // 打桩后端：登录 + 登录后拉取的用户信息
    await mockApi(page, {
      'POST **/auth/login':    { code: 200, data: { access_token: 'fake-token' } },
      'GET  **/getInfo':       { code: 200, user: { userName: '测试商户' }, roles: ['merchant'], permissions: ['*:*:*'] },
      'GET  **/getRouters':    { code: 200, data: [] },
    });

    await page.goto('/login');

    // 优先语义/占位符定位；建议前端给关键元素加 data-test
    await page.getByPlaceholder('账号').fill('test_merchant');
    await page.getByPlaceholder('密码').fill('123456');
    await page.getByRole('button', { name: /登\s*录/ }).click();

    await expect(page).toHaveURL(/\/index/);
    await expect(page.getByText('测试商户')).toBeVisible();
  });

  test('密码错误应提示且停留登录页', async ({ page }, testInfo) => {
    failOnConsoleError(page, testInfo);

    await mockApi(page, {
      'POST **/auth/login': { code: 500, msg: '用户名或密码错误' },
    });

    await page.goto('/login');
    await page.getByPlaceholder('账号').fill('test_merchant');
    await page.getByPlaceholder('密码').fill('wrong');
    await page.getByRole('button', { name: /登\s*录/ }).click();

    // Element UI 的 message 提示
    await expect(page.getByText('用户名或密码错误')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('登录页视觉快照', async ({ page }) => {
    await page.goto('/login');
    await expect(page).toHaveScreenshot('merchant/login.png', {
      maxDiffPixelRatio: 0.01,
    });
  });
});
