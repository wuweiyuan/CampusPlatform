# 阶段 6：管理员分类管理设计

## 目标

为管理员提供分类查看、新增、编辑与启用/停用页面；学生不可见且不能直接访问路由或接口。

## 范围

- 复用现有 `/api/admin/categories` 后端接口和集中式 `/api/admin/**` 权限保护，不新增后端业务接口。
- 管理台入口直接进入分类管理：`/admin` 重定向到 `/admin/categories`。
- 本次不加入用户、商品、订单管理逻辑；它们在后续小节独立接入。

## 页面与数据流

```text
ADMIN 登录
  → 侧栏“管理后台”
  → /admin/categories
  → GET /api/admin/categories
  → 分类表格

新增 / 编辑
  → Element Plus 弹窗表单
  → POST / PATCH /api/admin/categories
  → 重新加载表格

启用 / 停用
  → 二次确认
  → PATCH /api/admin/categories/{id}/status
  → 重新加载表格
```

## 文件职责

| 文件 | 变更 |
| --- | --- |
| `docs/api/phase-6.md` | 记录管理员分类接口、权限、请求和错误响应。 |
| `campus-trade-web/src/api/admin-category.ts` | 管理员分类类型与 HTTP 请求。 |
| `campus-trade-web/src/views/admin/CategoryManageView.vue` | 表格、弹窗表单、状态切换、加载/空/错误状态。 |
| `campus-trade-web/src/router/index.ts` | 管理台重定向和管理员分类路由。 |
| `campus-trade-web/src/layouts/AppLayout.vue` | 仅管理员可见的管理台入口。 |

## 交互与错误处理

- 新增和编辑共用“名称、排序”表单；前端校验名称非空、长度不超过 30、排序为整数，后端仍是最终校验者。
- 停用前提示：停用后用户不能使用该分类创建或编辑商品；启用/停用成功后重新读取服务端列表。
- 所有接口失败使用现有 `getErrorMessage` 提示；列表加载失败显示重试状态。
- 路由使用 `requiresAuth: true`、`roles: ["ADMIN"]`；后端的 403 是最终权限保障。

## 验收

- 管理员可新增、编辑、启用、停用分类，操作后表格刷新。
- 学生没有侧栏入口；直接访问 `/admin/categories` 跳转 403。
- 普通用户 Token 请求 `/api/admin/categories` 返回 403；管理员 Token 成功。
- 按用户决定，不主动运行构建、格式化或自动测试，使用浏览器和 Postman 手动验收。
