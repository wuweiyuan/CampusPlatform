# 管理员用户管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理员可分页管理用户状态；禁用用户后不能登录且旧 JWT 立即失效。

**Architecture:** `UserRole` 与 `UserStatus` 是 `sys_user` 的唯一角色/状态表达；它们用 MyBatis-Plus `@EnumValue` 保持既有数据库值和 API JSON 值。JWT 过滤器在验签后从 `sys_user` 读取最新枚举状态与角色；管理员用户 Service 复用 `SysUserMapper` 的 MyBatis-Plus 分页，不返回实体的密码字段；Vue 页面通过独立 API 模块提供筛选、分页和状态操作。

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

### Task 2: 将用户角色和状态收敛为枚举

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/user/enums/UserRole.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/user/enums/UserStatus.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/user/entity/SysUser.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/service/AuthService.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/security/JwtService.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/dto/UserInfoResponse.java`

- [ ] `UserRole` 只有 `USER`、`ADMIN` 两个值，以 `@EnumValue` 的字符串代码保存；`UserStatus` 只有 `ENABLED(1)`、`DISABLED(0)`，以 `@EnumValue` 的数值代码保存。两个枚举负责将既有 JSON 值反序列化为枚举，并将代码值序列化为 JSON。
- [ ] `SysUser.role` 改为 `UserRole`，`SysUser.status` 改为 `UserStatus`；不修改 `sys_user` 表结构或已有数据。
- [ ] 注册时设置 `UserRole.USER`、`UserStatus.ENABLED`；登录和 `/api/auth/me` 使用 `UserStatus.ENABLED` 判断账号状态；登录响应的角色字段改为 `UserRole`。
- [ ] JWT 的 `role` claim 与权限名使用 `user.getRole().getCode()`，使 Token 与 Spring Security 权限值仍为 `USER`、`ADMIN`。

### Task 3: 每请求检查用户状态

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/auth/security/JwtAuthenticationFilter.java`

- [ ] 向过滤器注入 `SysUserMapper`，并在 Token 验签、黑名单检查后读取 Token 中的 `userId`。
- [ ] 查询用户并仅在账号存在且 `status == UserStatus.ENABLED` 时建立认证上下文：

```java
SysUser user = sysUserMapper.selectById(userId.longValue());
if (user == null || user.getStatus() != UserStatus.ENABLED) {
    SecurityContextHolder.clearContext();
    filterChain.doFilter(request, response);
    return;
}
```

- [ ] 用 `user.getId()`、`user.getUsername()`、`user.getRole().getCode()` 构建 `AuthenticatedUser` 和 `ROLE_<role>`，不使用 Token 中过期的用户名或角色。
- [ ] 保持公共接口匿名可访问；禁用用户访问任何受保护接口时由现有 `RestAuthenticationEntryPoint` 返回 HTTP 401。

### Task 4: 管理员用户后端接口

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/dto/AdminUserQuery.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/dto/UserStatusUpdateRequest.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/vo/AdminUserResponse.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/service/AdminUserService.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/controller/AdminController.java`

- [ ] `AdminUserQuery` 提供默认 `page = 1`、`pageSize = 12` 与 `@Min/@Max`；`role` 类型为 `UserRole`，`status` 类型为 `UserStatus`，由枚举将既有 API 值转换并拒绝非法值。`UserStatusUpdateRequest.status` 类型为必填 `UserStatus`。
- [ ] `AdminUserService.listUsers(query)` 使用 `Page<SysUser>` 与 LambdaQueryWrapper：`username`、`email` 各自按非空模糊匹配，角色与状态枚举精确匹配，按 `createdAt`、`id` 倒序；将结果映射为不含密码的 `AdminUserResponse` 和 `PageResponse`。
- [ ] `updateUserStatus(currentAdminId, targetUserId, status)`：先查目标用户；不存在抛 `new BusinessException(1005, "用户不存在")`；目标 ID 等于当前管理员 ID 且请求状态为 `UserStatus.DISABLED` 时抛 `new BusinessException(1006, "不能禁用当前登录管理员")`；否则更新 `status`。
- [ ] Controller 使用 `@RequestMapping("/api/admin/users")`、`@Valid` 与 `@AuthenticationPrincipal AuthenticatedUser`，返回统一 `ApiResponse`；不在 Controller 复制管理员角色判断，由已有 `/api/admin/**` 安全规则集中处理。

### Task 5: 用户管理前端 API 与页面

**Files:**
- Create: `campus-trade-web/src/api/admin-user.ts`
- Create: `campus-trade-web/src/views/admin/UserManageView.vue`

- [ ] `admin-user.ts` 定义 `UserRole = "USER" | "ADMIN"`、`UserStatus = 0 | 1`、`AdminUser` 与可选筛选字段 `AdminUserQuery`；从 `category.ts` 复用 `ApiResponse`，从 `product.ts` 复用 `PageResponse`。`getAdminUsers` 使用 `GET /admin/users` 和 Axios `params`；`updateAdminUserStatus` 使用 `PATCH /admin/users/{id}/status`，请求体为 `{ status }`。
- [ ] `UserManageView.vue` 用一个可提交/重置的筛选表单管理用户名、邮箱、角色和状态；提交或重置时回到第 1 页，分页切换只改变 `page` 后重新加载。请求参数省略空字符串与未选择值。
- [ ] 页面沿用分类管理页的米白、墨绿、带边框表格样式：标题区含 `ADMIN CONSOLE`、用户管理说明和总用户数；角色、状态使用清晰标签；表格展示用户名、邮箱、角色、状态、注册时间和操作。加载显示 skeleton，失败显示重试，空记录显示 empty，窄屏允许筛选区换行和表格横向滚动。
- [ ] 状态按钮根据当前值显示“禁用”或“启用”；禁用确认文本必须说明旧 Token 会立即失效。确认后请求接口、显示成功/失败消息，并重新加载当前页。当前登录管理员所在行的禁用按钮必须禁用并提示“不能禁用自己”；后端 `1006` 仍是最终保障。

### Task 6: 用户管理路由、菜单与验收

**Files:**
- Modify: `campus-trade-web/src/router/index.ts`
- Modify: `campus-trade-web/src/layouts/AppLayout.vue`
- Modify: `docs/学习清单/阶段-6-今日交接记录-2026-09-01.md`

- [ ] 新增 `/admin/users` 路由：`meta: { requiresAuth: true, roles: ["ADMIN"] }`；`/admin` 仍重定向到分类页。
- [ ] 管理员侧栏把原“管理后台”单一链接改为管理区域内的“分类管理”“用户管理”两个链接；普通学生不渲染整个管理区域。
- [ ] Postman：管理员分页与筛选成功；普通 `USER` Token 返回 403；管理员不能禁用自己；禁用用户不能登录；禁用前旧 Token 请求 `/api/auth/me` 或订单接口返回 401。
- [ ] 浏览器：管理员可筛选、翻页、禁用、启用用户；学生无菜单且直接访问 `/admin/users` 跳 403；禁用自己时前端按钮不可用，禁用其他用户时显示确认弹窗。
- [ ] 回填阶段 6 交接记录；不运行构建、格式化、自动测试，记录为用户决定的例外。
