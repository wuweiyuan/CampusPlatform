# 阶段 6：管理员订单管理设计

## 目标

管理员能够跨买家、卖家分页查看和筛选订单；订单管理在阶段 6 完全只读，不能付款、取消或确认完成订单。

## 后端结构

新增 `AdminOrderQuery` 接收分页、订单号、状态、买家 ID、卖家 ID。复用现有 `orders` 表、`OrderMapper`、`OrderPageResponse` 与 `OrderStatus`；管理员查询 Mapper 联查订单、商品、买家和卖家，按创建时间、ID 倒序返回分页结果。

新增 `AdminOrderService.listOrders` 只负责查询参数标准化和分页转换，不调用 `OrderService` 的任何状态更新方法。现有 `AdminController` 新增 `GET /api/admin/orders`；`/api/admin/**` 的统一安全配置负责 `ADMIN` 角色校验。

## 状态与权限边界

`PENDING_PAYMENT`、`CANCELLED`、`PAID`、`COMPLETED` 都能被管理员查看和筛选。管理员没有订单状态变更接口，不复用普通用户的付款、取消、完成接口，也不新增对应的 Service 方法。普通 `USER` 请求管理员订单接口应由后端返回 HTTP `403`。

## 验收

1. 管理员可查询全部订单，并分别按订单号、状态、买家和卖家筛选与翻页。
2. 普通用户请求列表返回 `403`；参数非法返回统一 `400`。
3. 管理员接口中不存在付款、取消、完成路由；订单状态在查询前后不变。
4. 按用户决定，不运行构建、格式化或自动测试，以 Postman 和浏览器手动验收。
