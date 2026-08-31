# 模拟付款设计

## 目标

为订单增加模拟付款接口，使订单从 `PENDING_PAYMENT` 变为 `PAID`，关联商品从 `LOCKED` 变为 `SOLD`，并记录付款时间。

## 接口

- `POST /api/orders/{orderId}/pay`
- 仅订单买家可调用；不需要请求体。
- 成功响应：`{ "code": 0, "message": "付款成功", "data": null }`。

## 后端流程

1. Controller 校验 `orderId` 为正整数，并从 JWT 取得买家 ID。
2. `OrderService.payOrder` 在一个 `@Transactional` 事务中读取订单。
3. 订单不存在返回 `4001`；调用者不是买家抛出 `AccessDeniedException`，返回 403。
4. 仅允许 `PENDING_PAYMENT` 状态付款；否则返回 `4002`。
5. Mapper 使用订单 ID 与预期状态的条件更新，将状态改为 `PAID`，并写入 `paid_at`。更新行数不是 1 时返回 `4002`。
6. Mapper 使用商品 ID 与预期状态的条件更新，将商品从 `LOCKED` 改为 `SOLD`。更新行数不是 1 时抛出业务异常，使事务回滚。

## 数据一致性与验收

- 订单更新和商品更新必须在同一事务中：任一步失败，订单保留 `PENDING_PAYMENT`，商品保留 `LOCKED`。
- 手工验收付款成功、重复付款、取消后付款，以及卖家和第三方越权付款。
- 本次不新增 JUnit；按既有阶段清单使用接口请求与数据库查询验收。
