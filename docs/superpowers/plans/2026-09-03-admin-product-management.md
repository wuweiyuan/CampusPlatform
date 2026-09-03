# 阶段 6：管理员商品管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理员可跨卖家分页筛选商品，并只对在售商品执行下架操作。

**Architecture:** 前端用独立 `admin-product.ts` 隔离管理员接口和类型，复用通用 `ApiResponse` 与 `PageResponse`。管理页面沿用用户管理页的控制台表格模式；路由和菜单仅提升体验，后端 `/api/admin/**` 仍是权限最终保障。

**Tech Stack:** Vue 3、TypeScript、Element Plus、Axios。

**Verification exception:** 用户明确要求不主动运行构建、格式化或自动测试；以浏览器手动验收替代。

---

### Task 1: 管理员商品 API 模块

**Files:**
- Create: `campus-trade-web/src/api/admin-product.ts`

- [ ] 定义 `AdminProduct`，字段与管理员商品接口响应一致：商品、缩略图、分类、卖家、状态、浏览量、创建/更新时间；不定义 `favorited`。
- [ ] 定义可选 `sellerId`、`categoryId`、`status`、`keyword` 与必填分页字段的 `AdminProductQuery`；复用 `ProductStatus`、`PageResponse`、`ApiResponse`。
- [ ] `getAdminProducts(params)` 发起 `GET /admin/products`，`offShelfAdminProduct(id)` 发起 `PATCH /admin/products/{id}/off-shelf`，且不发送请求体。

### Task 2: 商品管理页面

**Files:**
- Create: `campus-trade-web/src/views/admin/ProductManageView.vue`

- [ ] 提供关键词、卖家 ID、分类 ID、状态筛选；仅把正整数 ID、非空关键词和已选状态发给 API。查询/重置回到第 1 页，翻页保持筛选。
- [ ] 使用管理员控制台标题、总数、缩略图/占位、商品标题与价格、卖家、分类、状态、发布时间和操作表格；实现 skeleton、错误重试、空状态、分页与移动端横向滚动。
- [ ] 仅 `ON_SALE` 行显示下架按钮。确认弹窗说明不可重新上架且不影响既有订单；操作成功或错误后重新加载当前页。

### Task 3: 路由、菜单与浏览器验收

**Files:**
- Modify: `campus-trade-web/src/router/index.ts`
- Modify: `campus-trade-web/src/layouts/AppLayout.vue`
- Modify: `docs/学习清单/阶段-6-今日交接记录-2026-09-01.md`

- [ ] 添加 `/admin/products`，`meta: { requiresAuth: true, roles: ["ADMIN"] }`。
- [ ] 管理员管理区域新增“商品管理”；学生不渲染该入口。
- [ ] 浏览器验证管理员的加载、筛选、分页、仅在售可下架和操作后刷新；学生无菜单且直接访问跳 `/403`。如实记录不运行构建、格式化和自动测试。
