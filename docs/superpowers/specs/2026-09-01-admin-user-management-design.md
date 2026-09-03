# 阶段 6：管理员用户管理设计

## 目标

管理员能够分页查看、筛选、启用或禁用用户；禁用后该用户不能登录，已有 JWT 也立即失效。

## 用户角色与状态枚举

用户角色和启用状态在 Java 代码中统一使用枚举，消除散落的字符串与数值字面量：

- `UserRole`：`USER`、`ADMIN`，持久化值与 JSON 值均为 `"USER"`、`"ADMIN"`。
- `UserStatus`：`ENABLED(1)`、`DISABLED(0)`，持久化值与 JSON 值仍为数值 `1`、`0`。

两个枚举均采用 MyBatis-Plus `@EnumValue` 映射已有的 `sys_user.role VARCHAR(16)` 和 `sys_user.status TINYINT` 列；不修改表结构或存量数据。`SysUser` 的 `role`、`status` 字段改为对应枚举。接口保持既有契约：角色传/返回字符串，状态传/返回数值；枚举负责 JSON 的序列化与反序列化。

枚举改动必须同步覆盖注册、登录、`/api/auth/me`、JWT 签发、JWT 过滤器、管理员用户 DTO/VO、Service 查询和状态更新。Spring Security 的权限名仍为 `ROLE_ADMIN`、`ROLE_USER`，由 `UserRole` 的代码值拼接。

## 安全模型

`JwtAuthenticationFilter` 在 JWT 验签、黑名单检查后，按 Token 的 `userId` 查询 `sys_user`：

- 用户不存在或 `status = DISABLED`：不建立 Spring Security 登录上下文，后续受保护请求返回 HTTP 401。
- 用户正常：使用数据库最新 `id`、`username`、`role` 建立 `AuthenticatedUser` 与 `ROLE_<role>` 权限。

这让禁用与角色变化立即生效，不信任旧 Token 中的角色或账号状态。

## API

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/api/admin/users` | 按用户名、邮箱、角色、状态分页查看用户。 |
| `PATCH` | `/api/admin/users/{id}/status` | 启用或禁用指定用户。 |

两个接口均属于 `/api/admin/**`，只能由 `ADMIN` 调用。列表项只返回 ID、用户名、邮箱、角色、状态、创建/更新时间；永不返回密码哈希或 JWT。

## 业务规则

- `UserStatus.ENABLED` 的接口值为 `1`，`UserStatus.DISABLED` 的接口值为 `0`。
- 管理员不能禁用自己，其他管理员也适用此规则。
- 公开注册逻辑保持不变，始终创建 `USER`。
- 启用用户不会恢复已失效的旧 Token；用户需重新登录获取新 Token。

## 前端

- `UserManageView.vue` 提供用户名/邮箱关键词、角色、状态筛选与分页表格。
- 表格显示状态标签；每行提供“启用”或“禁用”，操作前二次确认。
- 用户管理路由为 `/admin/users`，设定 `requiresAuth: true`、`roles: ["ADMIN"]`。
- 管理员侧栏将管理区域明确为“分类管理”和“用户管理”两个入口；学生无入口且直接访问跳 403。
- 操作成功或接口状态冲突后重新加载服务端当前页。

### 视觉与交互

用户管理页沿用现有分类管理页的米白、墨绿和表格化控制台语言，不新增独立视觉系统：标题区使用 `ADMIN CONSOLE` 标识与简短说明；筛选区位于表格上方；表格承载用户信息、角色、状态、注册时间与操作。启用状态使用成功色标签，禁用状态使用中性色标签；“禁用”视为高风险操作，确认弹窗必须说明其会立即使旧 Token 失效。页面提供 skeleton、错误重试、空状态和分页，窄屏时筛选与表格允许纵向排列或横向滚动。

## 验收

- 管理员可筛选、分页、启用、禁用其他用户；不能禁用自己。
- 被禁用用户不能再次登录；禁用前获取的旧 Token 请求受保护接口返回 401。
- 普通 `USER` 请求管理员用户接口返回 403，浏览器没有管理员入口。
- 按用户决定，使用 Postman 与浏览器手动验收，不主动运行构建、格式化或自动测试。
