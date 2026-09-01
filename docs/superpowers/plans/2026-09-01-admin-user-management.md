# 管理员用户管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理员可分页管理用户状态；禁用用户后不能登录且旧 JWT 立即失效。

**Architecture:** JWT 过滤器在验签后从 `sys_user` 读取最新用户状态与角色；管理员用户 Service 复用 `SysUserMapper` 的 MyBatis-Plus 分页，不返回实体的密码字段；Vue 页面通过独立 API 模块提供筛选、分页和状态操作。

**Tech Stack:** Spring Boot、Spring Security、MyBatis-Plus、Vue 3、TypeScript、Element Plus。

**Verification exception:** 用户明确要求不主动运行自动测试、构建或格式化命令；以 Postman 和浏览器手动验收替代。

---

### Task 1: 记录管理员用户 API

**Files:**
- Modify: `docs/api/phase-6.md`

- [ ] 增加用户列表约定：

```text
GET /api/admin/users?page=1&pageSize=12&username=&email=&role=USER&status=1
```

`page` 最小 1，`pageSize` 为 1–50；`username`、`email` 为可选模糊筛选，`role` 仅 `USER|ADMIN`，`status` 仅 `0|1`。

- [ ] 记录用户列表项只含 `id`、`username`、`email`、`role`、`status`、`createdAt`、`updatedAt`；不返回 `password`、JWT、认证码。
- [ ] 记录状态接口与错误：

```text
PATCH /api/admin/users/{id}/status
{ "status": 0 }
```

用户不存在为 `1005`，管理员禁用自己为 `1006`；普通用户为 HTTP 403；禁用后旧 Token 的受保护请求为 HTTP 401。

### Task 2: 每请求检查用户状态

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/security/JwtAuthenticationFilter.java`

- [ ] 向过滤器注入 `SysUserMapper`，并在 Token 验签、黑名单检查后读取 Token 中的 `userId`。
- [ ] 查询用户并仅在账号存在且 `status == 1` 时建立认证上下文：

```java
SysUser user = sysUserMapper.selectById(userId.longValue());
if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
    SecurityContextHolder.clearContext();
    filterChain.doFilter(request, response);
    return;
}
```

- [ ] 用 `user.getId()`、`user.getUsername()`、`user.getRole()` 构建 `AuthenticatedUser` 和 `ROLE_<role>`，不使用 Token 中过期的用户名或角色。
- [ ] 保持公共接口匿名可访问；禁用用户访问任何受保护接口时由现有 `RestAuthenticationEntryPoint` 返回 HTTP 401。

### Task 3: 管理员用户后端接口

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/dto/AdminUserQuery.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/dto/UserStatusUpdateRequest.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/vo/AdminUserResponse.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/service/AdminUserService.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/controller/AdminUserController.java`

- [ ] `AdminUserQuery` 提供默认 `page = 1`、`pageSize = 12` 与 `@Min/@Max`；`role` 用 `@Pattern(regexp = "USER|ADMIN")`，`status` 用 `@Min(0) @Max(1)`。
- [ ] `AdminUserService.listUsers(query)` 使用 `Page<SysUser>` 与 LambdaQueryWrapper：`username`、`email` 各自按非空模糊匹配，角色与状态精确匹配，按 `createdAt`、`id` 倒序；将结果映射为不含密码的 `AdminUserResponse` 和 `PageResponse`。
- [ ] `updateUserStatus(currentAdminId, targetUserId, status)`：先查目标用户；不存在抛 `new BusinessException(1005, "用户不存在")`；目标 ID 等于当前管理员 ID 且请求停用时抛 `new BusinessException(1006, "不能禁用当前登录管理员")`；否则更新 `status`。
- [ ] Controller 使用 `@RequestMapping("/api/admin/users")`、`@Valid` 与 `@AuthenticationPrincipal AuthenticatedUser`，返回统一 `ApiResponse`；不在 Controller 复制管理员角色判断，由已有 `/api/admin/**` 安全规则集中处理。

### Task 4: 用户管理前端 API 与页面

**Files:**
- Create: `campus-trade-web/src/api/admin-user.ts`
- Create: `campus-trade-web/src/views/admin/UserManageView.vue`

- [ ] 在 API 模块中定义 `AdminUser`、`AdminUserQuery`、`UserRole`，封装 `getAdminUsers(params)` 与 `updateAdminUserStatus(id, status)`；复用 `ApiResponse`、`PageResponse`。
- [ ] 页面提供用户名/邮箱关键字、角色、状态筛选；任何筛选变化重载第 1 页。表格展示用户名、邮箱、角色、状态、创建时间和操作；提供 skeleton、错误重试、空状态、分页。
- [ ] 状态按钮根据当前状态显示“禁用”或“启用”；二次确认后调用接口并刷新当前页。当前登录管理员对应行的“禁用”按钮禁用并提示“不能禁用自己”。
- [ ] 使用 scoped CSS 延续管理员分类页面的现有色彩与表格风格。

### Task 5: 用户管理路由、菜单与验收

**Files:**
- Modify: `campus-trade-web/src/router/index.ts`
- Modify: `campus-trade-web/src/layouts/AppLayout.vue`
- Modify: `docs/学习清单/阶段-6-今日交接记录-2026-09-01.md`

- [ ] 新增 `/admin/users` 路由：`meta: { requiresAuth: true, roles: ["ADMIN"] }`；`/admin` 仍重定向到分类页。
- [ ] 在管理员可见的侧栏中新增“用户管理”，普通学生不渲染此链接。
- [ ] Postman：管理员分页与筛选成功；普通 `USER` Token 返回 403；管理员不能禁用自己；禁用用户不能登录；禁用前旧 Token 请求 `/api/auth/me` 或订单接口返回 401。
- [ ] 浏览器：管理员可筛选、禁用、启用用户；学生无菜单且直接访问 `/admin/users` 跳 403。
- [ ] 回填阶段 6 交接记录；不运行构建、格式化、自动测试，记录为用户决定的例外。
