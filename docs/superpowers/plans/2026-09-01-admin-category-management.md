# 管理员分类管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 提供管理员分类管理页面，并确保学生无法访问其路由或接口。

**Architecture:** 复用现有 `/api/admin/categories` 后端接口，在前端以 `admin-category.ts` 隔离请求和类型；页面使用单个表格与共享弹窗表单。路由和菜单根据管理员角色展示，但后端 `/api/admin/**` 权限规则仍为最终保障。

**Tech Stack:** Vue 3 Composition API、TypeScript、Vue Router、Element Plus、Axios。

**Verification exception:** 用户明确要求不主动运行自动测试、构建或格式化命令；本计划使用 Postman 和浏览器手动验收。

---

### Task 1: 记录管理员分类 API

**Files:**
- Create: `docs/api/phase-6.md`

- [x] 记录通用管理员约定：所有 `/api/admin/**` 需要 `Authorization: Bearer <ADMIN JWT>`；普通 `USER` 返回 HTTP 403。
- [x] 记录并给出请求/响应样例：

```text
GET   /api/admin/categories
POST  /api/admin/categories                 { "name": "教材书籍", "sort": 10 }
PATCH /api/admin/categories/{id}            { "name": "教材与资料", "sort": 11 }
PATCH /api/admin/categories/{id}/status     { "status": "ENABLED" | "DISABLED" }
```

- [x] 固定业务错误：分类名称重复 `2001`、分类不存在 `2002`、非法参数 HTTP 400、普通用户 HTTP 403；停用分类后商品创建/编辑返回“分类不存在或已停用”。

### Task 2: 管理员分类前端 API 边界

**Files:**
- Create: `campus-trade-web/src/api/admin-category.ts`

- [x] 定义前端类型：

```ts
export type CategoryStatus = "ENABLED" | "DISABLED";
export interface AdminCategory {
  id: number;
  name: string;
  sort: number;
  status: CategoryStatus;
  createdAt: string;
  updatedAt: string;
}
export interface CategoryPayload {
  name: string;
  sort: number;
}
```

- [x] 封装请求，路径和方法与文档完全一致：

```ts
export const getAdminCategories = () => http.get<ApiResponse<AdminCategory[]>>("/admin/categories");
export const createAdminCategory = (payload: CategoryPayload) => http.post<ApiResponse<AdminCategory>>("/admin/categories", payload);
export const updateAdminCategory = (id: number, payload: CategoryPayload) => http.patch<ApiResponse<AdminCategory>>(`/admin/categories/${id}`, payload);
export const updateAdminCategoryStatus = (id: number, status: CategoryStatus) => http.patch<ApiResponse<AdminCategory>>(`/admin/categories/${id}/status`, { status });
```

- [x] 手动核对：请求层不传管理员 ID 或角色，Axios 拦截器自动携带 JWT。

### Task 3: 分类管理页面

**Files:**
- Create: `campus-trade-web/src/views/admin/CategoryManageView.vue`

- [x] 建立列表状态 `records`、`loading`、`errorMessage`、`operatingId`；`load()` 只调用 `getAdminCategories()`，失败时使用 `getErrorMessage(error, "分类加载失败")`。
- [x] 以 Element Plus `el-table` 显示分类名称、排序、状态、更新时间和操作。状态显示“启用/已停用”；按 `sort`、`id` 由后端返回的顺序展示，不在前端二次排序。
- [x] 使用一个 `el-dialog` 同时服务新增和编辑；表单字段为 `name`、`sort`，用 `el-form` 规则校验名称必填/最多 30 字、排序必填整数。编辑时预填行数据；提交按模式调用 `createAdminCategory` 或 `updateAdminCategory`，成功后关闭弹窗并重新加载。
- [x] 实现 `toggleStatus(category)`：先 `ElMessageBox.confirm`，停用时明确提示该分类不能再用于发布/编辑商品；调用 `updateAdminCategoryStatus`，成功或接口失败后重新加载列表。
- [x] 提供 skeleton、错误重试和空状态；所有按钮在当前行操作中禁用，避免重复请求。
- [x] 使用页面内 scoped CSS 延续现有米白、深绿、橙色强调的视觉样式，不修改无关全局样式。

### Task 4: 管理员路由与菜单

**Files:**
- Modify: `campus-trade-web/src/router/index.ts`
- Modify: `campus-trade-web/src/layouts/AppLayout.vue`

- [x] 将已有占位 `/admin` 改为重定向：

```ts
{
  path: "admin",
  redirect: { name: "admin-categories" },
}
```

- [x] 新增分类管理路由，并把权限元数据写在目标路由上：

```ts
{
  path: "admin/categories",
  name: "admin-categories",
  component: () => import("../views/admin/CategoryManageView.vue"),
  meta: { requiresAuth: true, roles: ["ADMIN"] },
}
```

- [x] 在登录菜单中仅为 `authStore.user?.role === "ADMIN"` 显示：

```vue
<RouterLink v-if="authStore.user?.role === 'ADMIN'" to="/admin/categories">
  管理后台
</RouterLink>
```

- [ ] 浏览器人工检查：管理员可进入分类页；学生看不到入口，直接访问 `/admin/categories` 跳 `/403`。

### Task 5: 手动验收与阶段记录

**Files:**
- Modify: `docs/学习清单/阶段-6-今日交接记录-2026-09-01.md`

- [x] 管理员浏览器新增、编辑、停用、启用一个测试分类；每次操作后确认表格刷新。
- [x] 学生浏览器验证无菜单、直接 URL 跳 403；Postman 再次验证学生 Token 访问 API 返回 403。
- [x] 用已停用测试分类提交商品，确认后端拒绝。
- [x] 回填阶段 6 分类页面验收结果；不运行构建、格式化、自动测试，记录其为用户决定的例外。
