// 统一 API 打桩 helper：让前端 E2E 脱离真实后端，稳定可复现。
//
// 用法：
//   await mockApi(page, {
//     'POST **/auth/login':     { code: 200, data: { token: 'fake' } },
//     'GET  **/merchant/info':  { code: 200, data: { name: '测试商户' } },
//     'GET  **/rate/list':      { code: 500, msg: '服务异常' },   // 测失败降级
//   });
//
// key 格式："<METHOD> <glob-url>"，METHOD 可省略（默认匹配所有方法）。
// value 为返回的 JSON body（贴近后端真实结构：code/data/msg）。

async function mockApi(page, routes) {
  for (const key of Object.keys(routes)) {
    const [maybeMethod, ...rest] = key.trim().split(/\s+/);
    const methods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'];
    const hasMethod = methods.includes(maybeMethod.toUpperCase());
    const method = hasMethod ? maybeMethod.toUpperCase() : null;
    const urlGlob = hasMethod ? rest.join(' ') : key.trim();
    const body = routes[key];

    await page.route(urlGlob, async (route, request) => {
      if (method && request.method().toUpperCase() !== method) {
        return route.fallback();
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(body),
      });
    });
  }
}

// 断言页面无 JS 报错：在测试开始调用
function failOnConsoleError(page, testInfo) {
  page.on('pageerror', (err) => {
    testInfo.errors.push(new Error('页面 JS 报错: ' + err.message));
  });
}

module.exports = { mockApi, failOnConsoleError };
