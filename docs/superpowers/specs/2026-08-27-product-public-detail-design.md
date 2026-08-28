# 商品公开详情与浏览量设计

## 目标

提供无需登录的 `GET /api/products/{id}`：只允许查看在售商品，并使每次成功查看的浏览量增加一次；响应返回增加后的浏览量。

## 数据库操作

1. Mapper 先执行条件更新：`UPDATE product SET view_count = view_count + 1 WHERE id = #{id} AND status = 'ON_SALE'`。
2. 更新影响行数为 `0` 时，商品不存在或不可公开访问，Service 返回业务码 `3001`。
3. 更新成功后，Mapper 联查 `product`、`category`、`sys_user`，查询该 ID 的在售商品完整详情。

## 接口边界

- Controller 校验路径 ID 为正整数，调用 Service，并以既有 `ProductDetailResponse` 返回。
- Controller 不接收请求体，也不需要当前用户。
- SecurityConfig 使用仅匹配正整数 ID 的正则请求匹配器放行 `GET /api/products/{id}`；不能用 `/api/products/*`，否则将意外公开后续需要登录的 `/api/products/mine`。
- 联查结果包含分类名、卖家展示名和已增加的 `viewCount`，不暴露卖家邮箱或其他敏感字段。

## 验收

- 未登录访问在售商品详情成功，连续请求时 `viewCount` 逐次增加。
- 不存在商品与下架商品均返回 `3001`。
- 负数或零 ID 返回请求参数错误。
