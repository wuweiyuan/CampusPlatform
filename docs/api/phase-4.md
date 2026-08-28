# 阶段 4：商品收藏接口文档

> 基础地址：`http://localhost:8080`
>
> 所有接口均以 `/api` 开头。日期时间使用 ISO 8601 字符串，例如 `2026-08-28T14:30:00`。

## 一、通用约定

### 认证与归属

本阶段全部接口都要求登录，并在请求头携带：

```http
Authorization: Bearer <JWT Token>
```

后端始终从 JWT 取得当前用户 ID。客户端不得传递 `userId`，也不能通过任何参数查询、创建、删除其他用户的收藏。

### 响应格式

成功响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

失败响应沿用项目统一格式；请求参数或业务规则不满足时当前项目返回 HTTP `400`，未登录或 Token 无效返回 HTTP `401`，无权操作返回 HTTP `403`。

### 分页

“我的收藏”分页参数沿用商品分页规则：

| 参数 | 类型 | 必填 | 规则 | 默认值 |
| --- | --- | --- | --- | --- |
| `page` | integer | 否 | 从 `1` 开始，最小 `1` | `1` |
| `pageSize` | integer | 否 | `1` 到 `50` | `12` |

分页响应：

```json
{
  "page": 1,
  "pageSize": 12,
  "total": 36,
  "records": []
}
```

页码超过范围时返回空 `records`，不是错误。

## 二、收藏业务规则

- 一条收藏关系只属于一个用户和一个商品；数据库必须以 `(user_id, product_id)` 唯一索引作为最终兜底。
- 只有状态为 `ON_SALE` 的商品可以新增收藏。商品不存在、已下架、已锁定或已售出时均不能新增收藏。
- 重复收藏是幂等操作：已收藏同一商品时仍返回成功，不重复插入数据。
- 取消收藏同样是幂等操作：当前用户原本没有该商品收藏记录时仍返回成功。
- 商品被收藏后即使变为 `OFF_SHELF`、`LOCKED` 或 `SOLD`，仍应出现在该用户的“我的收藏”；前端据 `status` 显示状态，不应提供新增收藏入口。
- 收藏列表按 `favorite.created_at`、`favorite.id` 倒序返回，并通过联表一次查询商品、分类和卖家摘要，避免 N+1 查询。

## 三、数据对象

### 收藏列表对象 `FavoritePageResponse`

用于“我的收藏”分页 `records` 元素：

```json
{
  "id": 18,
  "productId": 101,
  "title": "九成新高等数学教材",
  "price": 25.5,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQ...",
  "status": "ON_SALE",
  "categoryId": 1,
  "categoryName": "教材书籍",
  "sellerId": 8,
  "sellerName": "zhangsan",
  "favoriteCreatedAt": "2026-08-28T14:30:00"
}
```

- `id` 是收藏记录 ID，`productId` 是商品 ID；前端跳转详情使用 `productId`。
- `imageBase64` 可为 `null`，前端显示图片占位。
- `status` 可为 `ON_SALE`、`LOCKED`、`SOLD` 或 `OFF_SHELF`；收藏列表不因商品状态变化而过滤记录。

### 商品收藏状态 `favorited`

商品广场卡片 `ProductPageResponse` 与商品详情 `ProductDetailResponse` 增加：

```json
{
  "favorited": false
}
```

- 未登录访问商品广场或详情时固定返回 `false`。
- 已登录访问时后端根据“当前用户 + 商品 ID”的收藏记录计算，客户端不得自行提交或修改。
- `favorited: true` 不代表商品仍可收藏；是否显示“收藏”操作仍取决于商品状态为 `ON_SALE`。

## 四、收藏商品

- 请求地址：`POST /api/products/{productId}/favorite`
- 是否登录：是
- 作用：收藏当前一件在售商品。

路径参数：

| 参数 | 规则 |
| --- | --- |
| `productId` | 正整数 |

请求不需要 Body。

成功响应：

```json
{
  "code": 0,
  "message": "收藏成功",
  "data": null
}
```

规则：

- 后端从 JWT 获取用户 ID。
- 商品不存在或状态不是 `ON_SALE` 时返回业务码 `3001` 和可理解的提示。
- 当前用户已收藏该商品时，返回成功；数据库中不增加第二条记录。
- 未登录返回 HTTP/响应体 `401`。

## 五、取消收藏

- 请求地址：`DELETE /api/products/{productId}/favorite`
- 是否登录：是
- 作用：取消当前用户对一件商品的收藏。

路径参数 `productId` 必须为正整数；请求不需要 Body。

成功响应：

```json
{
  "code": 0,
  "message": "已取消收藏",
  "data": null
}
```

规则：

- 删除条件必须同时包含当前 JWT 用户 ID 和 `productId`。
- 即使当前用户没有这件商品的收藏记录，也返回成功；不得泄露或删除他人的收藏记录。
- 商品不存在、下架或其他状态都不阻止取消收藏。
- 未登录返回 HTTP/响应体 `401`。

## 六、我的收藏

- 请求地址：`GET /api/favorites`
- 是否登录：是
- 作用：分页查看当前用户收藏的商品，包含后续下架、锁定或售出的商品。

查询参数为 `page`、`pageSize`，规则见“分页”。

成功响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "page": 1,
    "pageSize": 12,
    "total": 1,
    "records": [
      {
        "id": 18,
        "productId": 101,
        "title": "九成新高等数学教材",
        "price": 25.5,
        "imageBase64": null,
        "status": "OFF_SHELF",
        "categoryId": 1,
        "categoryName": "教材书籍",
        "sellerId": 8,
        "sellerName": "zhangsan",
        "favoriteCreatedAt": "2026-08-28T14:30:00"
      }
    ]
  }
}
```

后端只返回当前登录用户的收藏，不接受 `userId`、`sellerId` 等越权筛选参数。

## 七、前端交互约定

- 商品广场和详情页仅在已登录且商品为 `ON_SALE` 时显示收藏或取消收藏按钮。
- 收藏请求进行中禁用对应按钮，防止重复点击；只有后端成功后才更新本地 `favorited` 状态。
- “我的收藏”显示状态标签、图片占位、加载/空/错误状态和分页；每条记录可跳转 `/products/{productId}`。
- 对已下架、已锁定或已售出的收藏商品，保留取消收藏按钮，不显示新增收藏操作。

## 八、手动验收要点

1. 未登录调用三个接口均返回 `401`。
2. 对在售商品新增收藏成功，再次新增仍成功，数据库只有一条关系。
3. 对下架、锁定、已售出或不存在商品新增收藏失败。
4. 取消自己的收藏成功；重复取消仍成功；取消操作不会删除另一用户的记录。
5. 两个账号的收藏列表相互隔离。
6. 商品下架后，已收藏用户的列表仍可见该商品和状态；未收藏用户不能再新增收藏。
7. 验证分页、商品卡片/详情 `favorited` 状态与浏览器收藏按钮联动。
