# 阶段 2：Vue 登录与权限控制

## 目标

用 Vue 完成注册、登录、会话恢复、退出、权限路由和按角色显示菜单。

## 本阶段文件地图

所有文件都在 `campus-trade-web/src/` 下。目录不存在时，在 IDEA 中右键 `src` 或在 VS Code 中新建文件夹：

```text
api/auth.js                    调用后端认证接口
stores/auth.js                 Pinia 登录状态与 localStorage
router/index.js                路由表和路由守卫
layouts/AppLayout.vue          登录后的公共布局与菜单
views/LoginView.vue            登录页
views/RegisterView.vue         注册页
views/ProfileView.vue          个人中心
views/ForbiddenView.vue        403 页面
views/NotFoundView.vue         404 页面
```

修改已有文件：`src/main.js`（注册 Pinia、Element Plus、路由）、`src/App.vue`（渲染路由出口）、`src/api/http.js`（加入 Token 请求/响应拦截）。

## 清单

- [ ] 写 `docs/web/phase-2.md`，规划路由：`/login`、`/register`、`/`、`/profile`、`/403`、`/404`；管理员路径预留为 `/admin/*`，阶段 6 再实现。
- [ ] 使用一个统一状态管理工具，推荐 Pinia。状态只保存 `token` 和脱敏用户信息 `{id, username, email, role}`；绝不存密码、验证码。
- [ ] 把 Token 和用户信息保存到 localStorage；刷新时恢复；退出或接口 401 时同时清空。
- [ ] 创建 `src/api/auth.js`：放发送验证码、注册、登录、当前用户、退出这 5 个请求方法。
- [ ] Axios 请求拦截器：有 Token 时添加 `Authorization: Bearer <token>`。
- [ ] Axios 响应拦截器：收到 401 时清空会话并跳转 `/login?redirect=当前路径`；登录/注册本身失败时不要错误跳转。
- [ ] 创建 `src/views/RegisterView.vue`：用户名、邮箱、密码、验证码；前端校验与后端一致；发验证码 60 秒倒计时；提交中禁用按钮；成功跳登录。
- [ ] 创建 `src/views/LoginView.vue`：用户名、密码；登录成功后保留原先要访问的地址；调用 `/me` 获取可信用户信息。
- [ ] 创建 `src/layouts/AppLayout.vue`：学生菜单为商品广场、发布商品、我的发布、我的收藏、我的订单、个人中心。未做的页面可以暂显示“后续阶段完成”，但不能是坏链接。
- [ ] 创建 `src/views/ProfileView.vue`、`src/views/ForbiddenView.vue`、`src/views/NotFoundView.vue`：分别显示用户信息、403、404。
- [ ] 为需要登录的页面添加 `requiresAuth`；管理员路由添加 `roles: ['ADMIN']`；未登录跳登录，普通学生进管理员路径跳 403。
- [ ] 在浏览器手动检查：登录后 localStorage 有 token/用户信息；退出后两者被清空；不登录直接输入 `/profile` 会跳登录；学生直接输入管理员地址会到 403 页面；注册页验证码倒计时和登录失败提示正常显示。
- [ ] 手动验收：注册 → 登录 → 刷新浏览器 → 访问个人中心 → 退出 → 再访问个人中心应回登录页。
- [ ] 运行 `npm run build`，构建通过后提交 Git。
