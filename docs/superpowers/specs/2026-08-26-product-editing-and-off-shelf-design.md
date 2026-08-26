# 商品编辑与主动下架设计

## 目标

为已登录用户提供完整编辑本人在售商品，以及主动下架本人在售商品的接口；不改变商品表结构。

## 接口与数据边界

- `PUT /api/products/{id}` 使用独立的 `UpdateProductRequest`。它包含 `categoryId`、`title`、`description`、`price` 和 `imageBase64`，且全部遵循发布接口的校验规则；未知字段忽略。
- `POST /api/products/{id}/off-shelf` 不接收请求体，只将符合条件的商品状态改为 `OFF_SHELF`。
- 两个接口都从 JWT 主体获取操作者 ID，绝不信任客户端提交的卖家、状态、浏览量或时间字段。

## 服务层流程

1. 按商品 ID 查询。不存在时抛出业务错误码 `3001`。
2. 比较商品 `sellerId` 与当前用户 ID。不一致时抛出 `AccessDeniedException`，复用现有 `RestAccessDeniedHandler` 返回 HTTP `403`、响应体 `code: 403`。
3. 仅当状态为 `ON_SALE` 时继续；`LOCKED`、`SOLD`、`OFF_SHELF` 均返回 `3003`。
4. 编辑额外校验分类仍启用，并复用图片校验器；仅更新分类、标题、描述、价格和图片字段。
5. 下架仅更新状态为 `OFF_SHELF`。

## 响应与验收

- 编辑成功返回更新后的 `ProductDetailResponse`，并保留卖家、浏览量、创建时间与状态。
- 下架成功返回 `{ "code": 0, "message": "下架成功", "data": null }`。
- 手动验收覆盖：本人成功、商品不存在、跨账号拒绝、三个非在售状态拒绝、停用分类编辑拒绝、非法图片编辑拒绝，以及下架后公开接口不可见。
