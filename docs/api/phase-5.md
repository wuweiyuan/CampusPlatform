# 阶段 5：订单状态机与事务接口文档

> 基础地址：`http://localhost:8080`
>
> 所有接口均以 `/api` 开头，全部订单接口都需要登录。日期时间使用 ISO 8601 字符串，例如 `2026-08-29T14:30:00`。

## 一、通用约定

### 认证与归属

请求头必须携带：

```http
Authorization: Bearer <JWT Token>
```

后端始终从 JWT 获取当前用户 ID。客户端不能提交或覆盖订单的买家、卖家、金额、订单号、订单状态和时间；创建订单时只允许提交 `productId`。

订单详情仅允许该订单的买家或卖家查看；取消、付款、确认完成仅允许买家执行。无权操作返回 HTTP/响应体 `403`。

### 响应与分页

成功响应统一为：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

请求参数校验失败或业务规则不满足时，当前项目返回 HTTP `400`；未登录或 Token 无效返回 HTTP `401`；已登录但无权访问返回 HTTP `403`。

订单列表分页参数：

| 参数 | 类型 | 必填 | 规则 | 默认值 |
| --- | --- | --- | --- | --- |
| `page` | integer | 否 | 从 `1` 开始，最小 `1` | `1` |
| `pageSize` | integer | 否 | `1` 到 `50` | `12` |

分页响应的 `data`：

```json
{
  "page": 1,
  "pageSize": 12,
  "total": 36,
  "records": []
}
```

页码超过范围时返回空 `records`，不是错误。买入和卖出列表都按 `createdAt`、`id` 倒序排列。

### 错误码

| code | 含义 | 常见场景 |
| --- | --- | --- |
| `400` | 请求参数不合法 | 缺少或传入非正整数 `productId`、`orderId`、分页参数不合法 |
| `401` | 未登录或 Token 无效 | 未携带、过期、伪造或已退出的 Token |
| `403` | 无权访问 | 非买卖双方查看订单；卖家或第三方取消、付款、完成订单 |
| `3001` | 商品不存在或不可交易 | 商品不存在 |
| `3003` | 当前商品状态不允许此操作 | 对 `LOCKED`、`SOLD`、`OFF_SHELF` 商品创建订单；并发抢购时其他买家落败 |
| `4001` | 订单不存在 | 查询或操作不存在的订单 |
| `4002` | 当前订单状态不允许此操作 | 重复取消/付款、付款后取消、未付款直接完成 |
| `4003` | 不能购买自己的商品 | 买家尝试购买自己发布的商品 |

## 二、状态机与事务规则

### 订单状态

| 订单状态 | 含义 |
| --- | --- |
| `PENDING_PAYMENT` | 已锁定商品，等待买家模拟付款 |
| `CANCELLED` | 买家在付款前取消，交易结束 |
| `PAID` | 已模拟付款，等待买家确认完成 |
| `COMPLETED` | 买家已确认完成，交易结束 |

### 合法状态变化

| 操作 | 操作者 | 订单变化 | 商品变化 |
| --- | --- | --- | --- |
| 创建订单 | 买家 | 无 → `PENDING_PAYMENT` | `ON_SALE` → `LOCKED` |
| 取消订单 | 该订单买家 | `PENDING_PAYMENT` → `CANCELLED` | `LOCKED` → `ON_SALE` |
| 模拟付款 | 该订单买家 | `PENDING_PAYMENT` → `PAID` | `LOCKED` → `SOLD` |
| 确认完成 | 该订单买家 | `PAID` → `COMPLETED` | 保持 `SOLD` |

- 创建、取消、付款、确认完成均必须在一个 `@Transactional` 方法中执行。
- 创建订单时必须用条件更新锁定商品：仅当 `product.status = 'ON_SALE'` 时才能更新为 `LOCKED`。受影响行数不是 `1` 即创建失败，因此两个买家同时下单最多一个成功。
- 订单保存的是创建时的 `sellerId` 和 `amount` 快照；后续不能因商品价格变化而改变。
- 每次状态变化都从数据库读取当前订单状态并进行带预期状态的条件更新。重复操作和越级操作返回 `4002`，不允许静默成功。
- 事务中任一订单或商品更新失败必须抛出异常并整体回滚，不能留下“订单已创建但商品未锁定”等半完成数据。

## 三、数据对象

### 订单详情 `OrderDetailResponse`

```json
{
  "id": 18,
  "orderNo": "20260829143000123123456",
  "buyerId": 11,
  "buyerName": "buyerA",
  "sellerId": 8,
  "sellerName": "zhangsan",
  "productId": 101,
  "productTitle": "九成新高等数学教材",
  "productImageBase64": "data:image/jpeg;base64,/9j/4AAQ...",
  "amount": 25.5,
  "status": "PENDING_PAYMENT",
  "createdAt": "2026-08-29T14:30:00",
  "paidAt": null,
  "completedAt": null,
  "updatedAt": "2026-08-29T14:30:00"
}
```

- `amount` 是下单时从商品读取后保存的金额快照，单位为元。
- `productTitle`、`productImageBase64` 用于界面展示，查询时从商品表关联得到；图片可以为 `null`。
- 不返回邮箱、密码、JWT 等敏感字段。

### 订单列表项 `OrderPageResponse`

订单列表 `records` 的元素包含 `OrderDetailResponse` 的全部字段；前端可按 `status` 显示状态标签与可执行操作。

## 四、创建订单

- 请求地址：`POST /api/orders`
- 是否登录：是
- 作用：当前用户购买一件正在出售、且不属于自己的商品。

请求体：

```json
{
  "productId": 101
}
```

| 字段 | 必填 | 规则 |
| --- | --- | --- |
| `productId` | 是 | 正整数；商品必须存在且状态为 `ON_SALE` |

后端流程：从 JWT 取得买家 ID，读取商品并检查买家不是卖家，使用条件更新将商品原子地从 `ON_SALE` 改为 `LOCKED`，然后写入金额和卖家快照。订单号由后端按 `yyyyMMddHHmmssSSS` 加 6 位随机数字生成，数据库唯一索引为最终兜底。

成功响应的 `data` 为新建的 `OrderDetailResponse`，其中 `status` 固定为 `PENDING_PAYMENT`。商品已被其他买家锁定、已售出或下架时返回 `3003`；购买自己商品返回 `4003`。

## 五、查看订单详情

- 请求地址：`GET /api/orders/{orderId}`
- 是否登录：是，且必须是订单买家或卖家
- 作用：查看一笔与当前用户有关的订单详情。

路径参数 `orderId` 必须为正整数。成功响应的 `data` 为 `OrderDetailResponse`。

订单不存在返回 `4001`；当前用户既不是买家也不是卖家时返回 `403`。

## 六、我的买入订单

- 请求地址：`GET /api/orders/buying`
- 是否登录：是
- 作用：分页查看当前用户作为买家的订单。

查询参数使用通用分页规则，例如：`GET /api/orders/buying?page=1&pageSize=12`。

后端从 JWT 获取买家 ID，不接受 `buyerId` 查询参数。成功响应的 `data` 是分页对象，`records` 元素为 `OrderPageResponse`。

## 七、我的卖出订单

- 请求地址：`GET /api/orders/selling`
- 是否登录：是
- 作用：分页查看当前用户作为卖家的订单。

查询参数使用通用分页规则，例如：`GET /api/orders/selling?page=1&pageSize=12`。

后端从 JWT 获取卖家 ID，不接受 `sellerId` 查询参数。成功响应的 `data` 是分页对象，`records` 元素为 `OrderPageResponse`。

## 八、取消订单

- 请求地址：`POST /api/orders/{orderId}/cancel`
- 是否登录：是，且必须是订单买家
- 作用：取消一笔待付款订单，并把对应商品恢复为在售。

不需要请求体，`orderId` 必须为正整数。

仅当订单为 `PENDING_PAYMENT` 且商品为 `LOCKED` 时成功。成功后订单变为 `CANCELLED`，商品变为 `ON_SALE`：

```json
{
  "code": 0,
  "message": "订单已取消",
  "data": null
}
```

订单不存在返回 `4001`；非买家返回 `403`；状态不符合时返回 `4002`。重复取消不是幂等成功。

## 九、模拟付款

- 请求地址：`POST /api/orders/{orderId}/pay`
- 是否登录：是，且必须是订单买家
- 作用：模拟支付一笔待付款订单。

不需要请求体，`orderId` 必须为正整数。

仅当订单为 `PENDING_PAYMENT` 且商品为 `LOCKED` 时成功。成功后订单变为 `PAID`，写入 `paidAt`，商品变为 `SOLD`：

```json
{
  "code": 0,
  "message": "付款成功",
  "data": null
}
```

订单不存在返回 `4001`；非买家返回 `403`；重复付款、已取消订单付款等状态错误返回 `4002`。

## 十、确认完成

- 请求地址：`POST /api/orders/{orderId}/complete`
- 是否登录：是，且必须是订单买家
- 作用：买家确认已完成已付款订单。

不需要请求体，`orderId` 必须为正整数。

仅当订单为 `PAID` 时成功。成功后订单变为 `COMPLETED`，写入 `completedAt`，商品保持 `SOLD`：

```json
{
  "code": 0,
  "message": "订单已确认完成",
  "data": null
}
```

订单不存在返回 `4001`；非买家返回 `403`；待付款、已取消或已完成订单确认完成均返回 `4002`。

## 十一、路由与验收汇总

| 方法 | 路径 | 身份要求 | 用途 |
| --- | --- | --- | --- |
| `POST` | `/api/orders` | 已登录，非商品卖家 | 创建订单并锁定商品 |
| `GET` | `/api/orders/{orderId}` | 已登录，订单买家或卖家 | 订单详情 |
| `GET` | `/api/orders/buying` | 已登录 | 我的买入订单 |
| `GET` | `/api/orders/selling` | 已登录 | 我的卖出订单 |
| `POST` | `/api/orders/{orderId}/cancel` | 已登录，订单买家 | 取消待付款订单 |
| `POST` | `/api/orders/{orderId}/pay` | 已登录，订单买家 | 模拟付款 |
| `POST` | `/api/orders/{orderId}/complete` | 已登录，订单买家 | 确认完成 |

手动验收至少覆盖：下单→取消、下单→付款→完成、购买自己的商品、购买非在售商品、卖家/第三方越权操作、错误顺序操作、买卖列表隔离与分页，以及两个不同买家同时下单只有一个成功。
