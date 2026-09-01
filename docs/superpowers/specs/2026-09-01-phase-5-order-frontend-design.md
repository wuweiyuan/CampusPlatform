# 阶段 5：订单前端设计

## 目标

为现有订单后端接入购买入口与“我的订单”页面，完成下单、取消、模拟付款、确认完成的浏览器交易闭环。

## 范围与边界

- 使用单个“我的订单”页面，通过“我买到的 / 我卖出的”页签切换两个后端分页接口。
- 不新增订单详情页、购物车、真实支付、退款或物流能力。
- 下单成功跳转 `/orders?tab=buying`；页面默认展示买入订单。
- 不信任前端本地状态：操作成功或状态冲突后必须重新拉取当前列表。

## 文件与职责

| 文件 | 变更 |
| --- | --- |
| `campus-trade-web/src/api/order.ts` | 订单请求函数与 TypeScript 类型。 |
| `campus-trade-web/src/views/market/ProductDetailView.vue` | 购买按钮、二次确认、下单和跳转。 |
| `campus-trade-web/src/views/order/MyOrdersView.vue` | 页签、分页、订单卡片和状态操作。 |
| `campus-trade-web/src/router/index.ts` | `/orders` 指向订单页面。 |
| `campus-trade-web/src/layouts/AppLayout.vue` | 登录后显示“我的订单”导航。 |

## 数据流

```text
商品详情（ON_SALE、已登录、非本人）
  → 二次确认 → POST /orders
  → /orders?tab=buying
  → GET /orders/buying?page=&pageSize=

订单页签切换
  → GET /orders/buying 或 /orders/selling

订单状态操作
  → 二次确认 → cancel / pay / complete
  → 重新请求当前分页列表
```

## 交互规则

- 待付款（`PENDING_PAYMENT`）：买家显示“取消订单”“模拟付款”。
- 已付款（`PAID`）：买家显示“确认完成”。
- 已取消、已完成：仅显示状态，不显示操作按钮。
- 卖出页只展示状态，不提供买家专属操作。
- 使用 Element Plus 的确认框与消息提示；错误文本复用 `getErrorMessage`。

## 验收

- 未登录、商品本人、非在售商品不显示购买按钮。
- 下单后默认打开买入页签；买入和卖出数据相互隔离。
- 取消链路与付款后确认完成链路可在浏览器走通。
- 按用户要求，不主动执行前端构建或自动测试；由浏览器手动验收功能。
