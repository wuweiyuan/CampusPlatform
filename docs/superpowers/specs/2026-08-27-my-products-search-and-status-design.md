# 我的发布：搜索与状态筛选设计

## 目标

扩展登录用户的 `GET /api/products/mine`：在分页基础上，支持仅搜索当前用户发布商品的标题/描述，并可按商品状态筛选；默认仍返回当前用户的全部状态商品。

## 查询参数

- `page` 默认 `1`，最小 `1`；`pageSize` 默认 `12`，范围 `1` 至 `50`。
- `keyword` 可选，去首尾空格后为空则视为未传，最大 60 个字符；匹配标题或描述。
- `status` 可选，只允许 `ON_SALE`、`LOCKED`、`SOLD`、`OFF_SHELF`。不传时不附加状态条件，因此包含已下架商品。
- 客户端不能传 `sellerId`；Service 只使用 JWT 中当前用户的 ID。

## 查询实现

- 新建专用 `MyProductQuery`，不复用公开商品广场的 `ProductQuery`，避免两个接口的参数契约互相耦合。
- Mapper 使用 MyBatis-Plus `Page<ProductPageResponse>`，联查商品、分类和卖家；固定 `p.seller_id = #{sellerId}`，按需追加参数化关键词和状态筛选。
- 结果按 `p.created_at DESC, p.id DESC` 返回，并复用 `PageResponse<ProductPageResponse>`。

## 权限与验收

- `GET /api/products/mine` 不新增公开规则，继续由 `anyRequest().authenticated()` 保护。
- 未登录返回 `401`；账号 A 绝不可能通过参数读取账号 B 的商品。
- Postman 验收默认全部状态、关键词、各状态、关键词和状态组合、分页、非法参数，以及未登录访问。
