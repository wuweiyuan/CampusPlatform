# 阶段 6：管理员订单管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理员可跨用户分页筛选订单，且不具备任何订单状态变更能力。

**Architecture:** `AdminOrderQuery` 表达筛选参数；`OrderMapper` 增加管理员联表分页查询并复用 `OrderPageResponse`；`AdminOrderService` 只做参数标准化与分页转换；现有 `AdminController` 暴露管理员查询路由，统一安全规则负责权限。

**Tech Stack:** Spring Boot、MyBatis-Plus、Spring Security。

**Verification exception:** 用户明确要求不主动运行自动测试、构建或格式化命令；以 Postman 手动验收替代。

---

### Task 1: 管理员订单查询 DTO

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/dto/AdminOrderQuery.java`

- [ ] 设置默认 `page = 1`、`pageSize = 12`，并校验页码最小 1、每页 1–50。
- [ ] `orderNo` 最长 32 个字符；`buyerId`、`sellerId` 为可选正整数；`status` 使用 `OrderStatus` 枚举，使非法状态进入统一参数错误响应。

### Task 2: 管理员订单分页查询

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/mapper/OrderMapper.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/service/AdminOrderService.java`

- [ ] Mapper 新增 `selectAdminOrderPage`：联查订单、商品、买家、卖家；订单号、状态、买家、卖家均为可选精确条件；按 `created_at DESC, id DESC` 排序；返回 `IPage<OrderPageResponse>`。
- [ ] Service 标准化订单号，状态枚举转换为数据库代码，使用 `Page<OrderPageResponse>` 调用 Mapper 并返回 `PageResponse<OrderPageResponse>`。
- [ ] 不调用或新增付款、取消、完成的更新方法。

### Task 3: 管理员路由与验收

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/controller/AdminController.java`
- Modify: `docs/学习清单/阶段-6-今日交接记录-2026-09-01.md`

- [ ] 添加 `GET /api/admin/orders`，使用 `@Valid AdminOrderQuery` 并返回分页响应；不添加订单状态变更路由。
- [ ] Postman 验证管理员默认/组合筛选、普通用户 `403`、非法参数 `400`；分别在调用前后确认订单状态没有改变。
- [ ] 回填交接记录，说明未运行构建、格式化和自动测试。

### Task 4: 管理员订单前端 API 与页面

**Files:**
- Create: `campus-trade-web/src/api/admin-order.ts`
- Create: `campus-trade-web/src/views/admin/OrderManageView.vue`

- [ ] `admin-order.ts` 定义管理员订单筛选参数，复用 `OrderItem`、`OrderStatus`、`PageResponse`、`ApiResponse`；只封装 `getAdminOrders(params)` 的 `GET /admin/orders`，不导出状态更新请求。
- [ ] 订单管理页提供订单号、状态、买家 ID、卖家 ID 筛选和重置，且仅发送正整数 ID 与非空订单号；查询/重置回到第 1 页，翻页保持条件。
- [ ] 页面使用管理员只读审计表格显示订单号/商品、买家、卖家、金额、状态、下单时间和只读标记；提供 skeleton、错误重试、空状态、分页与窄屏横向滚动。
- [ ] 订单状态使用语义标签，但页面不得出现付款、取消、完成或其他状态操作按钮。

### Task 5: 订单管理路由、菜单与浏览器验收

**Files:**
- Modify: `campus-trade-web/src/router/index.ts`
- Modify: `campus-trade-web/src/layouts/AppLayout.vue`
- Modify: `docs/学习清单/阶段-6-今日交接记录-2026-09-01.md`

- [ ] 添加 `/admin/orders`，`meta: { requiresAuth: true, roles: ["ADMIN"] }`。
- [ ] 管理员管理区域新增“订单管理”；学生不渲染该入口。
- [ ] 浏览器验证管理员加载、筛选、翻页和无订单状态操作；学生无菜单且直接访问跳 `/403`。如实记录不运行构建、格式化和自动测试。
