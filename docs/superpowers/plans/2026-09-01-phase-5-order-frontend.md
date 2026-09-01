# 阶段 5：订单前端 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 Vue 前端完成商品下单与“我的订单”交易闭环。

**Architecture:** 新建 `api/order.ts` 作为订单 HTTP 边界；`ProductDetailView` 仅负责创建订单；`MyOrdersView` 作为单页双页签容器，按当前页签请求买入或卖出分页数据。状态操作永远在成功或失败后从服务端重载当前页。

**Tech Stack:** Vue 3 Composition API、TypeScript、Vue Router、Element Plus、Axios。

**Verification exception:** 用户明确要求不主动运行自动测试、构建或格式化命令。本计划以浏览器手动验收替代；新增代码仍应遵循已有 Vitest 可测试的 API 边界。

---

## 文件地图

| 文件 | 责任 |
| --- | --- |
| `campus-trade-web/src/api/order.ts` | 本阶段页面所需的订单类型与六个 HTTP 方法。 |
| `campus-trade-web/src/views/order/MyOrdersView.vue` | 页签、分页、订单卡片、买家操作。 |
| `campus-trade-web/src/views/market/ProductDetailView.vue` | 下单入口与跳转。 |
| `campus-trade-web/src/router/index.ts` | 将受保护的 `/orders` 指向订单页。 |
| `campus-trade-web/src/layouts/AppLayout.vue` | 登录后显示订单导航。 |

### Task 1: 订单 API 边界

**Files:**
- Create: `campus-trade-web/src/api/order.ts`

- [x] 定义服务端枚举和响应类型，复用现有 `ApiResponse`、`PageResponse`：

```ts
export type OrderStatus =
  | "PENDING_PAYMENT"
  | "CANCELLED"
  | "PAID"
  | "COMPLETED";

export interface OrderItem {
  id: number;
  orderNo: string;
  buyerId: number;
  buyerName: string;
  sellerId: number;
  sellerName: string;
  productId: number;
  productTitle: string;
  productImageBase64: string | null;
  amount: number;
  status: OrderStatus;
  createdAt: string;
  paidAt: string | null;
  completedAt: string | null;
  updatedAt: string;
}

export interface OrderListQuery {
  page: number;
  pageSize: number;
}
```

- [x] 实现请求函数；列表参数固定为 `page`、`pageSize`，不暴露买卖双方 ID：

```ts
export function createOrder(productId: number) {
  return http.post<ApiResponse<OrderItem>>("/orders", { productId });
}
export function getBuyingOrders(params: OrderListQuery) {
  return http.get<ApiResponse<PageResponse<OrderItem>>>("/orders/buying", { params });
}
export function getSellingOrders(params: OrderListQuery) {
  return http.get<ApiResponse<PageResponse<OrderItem>>>("/orders/selling", { params });
}
export const cancelOrder = (id: number) => http.post<ApiResponse<null>>(`/orders/${id}/cancel`);
export const payOrder = (id: number) => http.post<ApiResponse<null>>(`/orders/${id}/pay`);
export const completeOrder = (id: number) => http.post<ApiResponse<null>>(`/orders/${id}/complete`);
```

- [x] 手动检查每个路径与 `docs/api/phase-5.md` 一致，尤其是 `buying` / `selling` 和三个状态操作路径。

### Task 2: 商品详情购买入口

**Files:**
- Modify: `campus-trade-web/src/views/market/ProductDetailView.vue`

- [x] 增加派生权限，避免向未登录用户、商品主人或非在售商品展示购买入口：

```ts
const canBuy = computed(
  () =>
    authStore.isAuthenticated &&
    product.value?.status === "ON_SALE" &&
    !isOwner.value,
);
```

- [x] 添加 `buyLoading` 与 `buy()`：先使用 `ElMessageBox.confirm` 告知价格以服务端为准，再调用 `createOrder(product.value.id)`，成功后 `router.push({ name: "orders", query: { tab: "buying" } })`；取消确认不提示错误，接口失败使用 `getErrorMessage`。

- [x] 在详情操作区添加按钮，防止重复点击：

```vue
<el-button v-if="canBuy" type="primary" :loading="buyLoading" @click="buy">
  立即购买
</el-button>
```

- [ ] 浏览器人工检查：未登录、本人、`LOCKED`、`SOLD` 与 `OFF_SHELF` 均无购买按钮；可购买商品确认后进入买入订单页。

### Task 3: 我的订单双页签页面

**Files:**
- Create: `campus-trade-web/src/views/order/MyOrdersView.vue`

- [x] 建立 `activeTab`、`records`、`page`、`pageSize`、`total`、`loading`、`errorMessage`、`operatingId`。只接受 `"buying"` 与 `"selling"`；其他 `route.query.tab` 回退到 `"buying"`。

- [x] 实现单一加载入口，按页签选择 API：

```ts
async function load(nextPage = page.value) {
  loading.value = true;
  errorMessage.value = "";
  try {
    const request = activeTab.value === "buying" ? getBuyingOrders : getSellingOrders;
    const data = (await request({ page: nextPage, pageSize: pageSize.value })).data.data;
    records.value = data.records;
    page.value = data.page;
    total.value = data.total;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "订单加载失败");
  } finally {
    loading.value = false;
  }
}
```

- [x] 页签变化时通过 `router.replace({ query: { tab } })` 同步 URL 并加载第 1 页；监听 `route.query.tab` 以处理购买后的跳转或浏览器前进后退。

- [x] 定义中文状态文案和 Element Plus 标签类型：`PENDING_PAYMENT` 为“待付款/warning”，`PAID` 为“已付款/primary”，`CANCELLED` 为“已取消/info”，`COMPLETED` 为“已完成/success”。

- [x] 实现 `runOrderAction(item, action)`：为取消、付款、完成各自显示确认标题和文案；调用对应 API；成功提示后在最后一条删除且非第 1 页时加载上一页，否则加载当前页；取消对话框不显示错误，接口异常用 `getErrorMessage` 提示并重载当前页。

- [x] 模板按现有收藏/我的发布页面使用 `el-skeleton`、`el-result`、`el-empty`、`el-pagination`。订单卡片展示商品图或标题占位、订单号、买卖双方、金额、创建时间和状态；操作仅在 `activeTab === "buying"` 时显示：

```vue
<template v-if="activeTab === 'buying' && item.status === 'PENDING_PAYMENT'">
  <el-button :loading="operatingId === item.id" @click="runOrderAction(item, 'cancel')">取消订单</el-button>
  <el-button type="primary" :loading="operatingId === item.id" @click="runOrderAction(item, 'pay')">模拟付款</el-button>
</template>
<el-button v-else-if="activeTab === 'buying' && item.status === 'PAID'" type="primary" :loading="operatingId === item.id" @click="runOrderAction(item, 'complete')">确认完成</el-button>
```

- [ ] 浏览器人工检查：买卖数据隔离、页签与 URL 同步、分页切换、空态、加载错误、三种可操作状态及卖出页无买家操作按钮。

### Task 4: 路由与导航接入

**Files:**
- Modify: `campus-trade-web/src/router/index.ts`
- Modify: `campus-trade-web/src/layouts/AppLayout.vue`

- [x] 将 `orders` 路由替换为懒加载订单页面，移除占位页的 `props`：

```ts
{
  path: "orders",
  name: "orders",
  component: () => import("../views/order/MyOrdersView.vue"),
  meta: { requiresAuth: true },
}
```

- [x] 在仅登录可见的侧栏导航中、收藏与个人中心之间增加：

```vue
<RouterLink to="/orders">我的订单</RouterLink>
```

- [ ] 浏览器人工检查：未登录访问 `/orders` 仍跳转登录；登录后侧栏可进入订单页。

### Task 5: 阶段验收与文档回填

**Files:**
- Modify: `docs/学习清单/阶段-5-今日交接记录-2026-08-29.md`

- [ ] 用卖家、买家 A、买家 B 完成浏览器验收：买家 A 下单后取消；买家 A 下单、付款、确认完成；卖家页面只读；状态变更后列表刷新。
- [ ] 回填前端接入、浏览器验收、分页验收与成功文案统一的清单状态，记录实际人工验收结果。
- [ ] 不运行 `npm run format`、`npm run test` 或 `npm run build`，遵从用户明确的时间边界；在最终交接记录中注明未执行。
