# 商品公开分页查询设计

## 目标

提供无需登录的商品广场接口：只展示在售商品，支持分页、分类筛选和标题/描述关键词搜索。

## 请求与响应

- `GET /api/products` 接收 `page`、`pageSize`、可选 `categoryId` 和可选 `keyword`。
- `page` 默认 `1`，最小 `1`；`pageSize` 默认 `12`，范围 `1` 至 `50`。
- `keyword` 在服务层去首尾空格；空字符串视为未传，非空时最大 60 个字符。
- 响应为 `PageResponse<ProductPageResponse>`，包含当前页、每页数量、总数和记录列表。

## 查询实现

- 新建 `ProductQuery`、`ProductPageResponse` 和通用 `PageResponse<T>`。
- 新建 MyBatis-Plus 分页拦截器配置，指定 MySQL 方言。
- `ProductMapper` 增加一条接收 `Page` 参数的列表 SQL，固定条件 `p.status = 'ON_SALE'`；分页拦截器自动追加 `LIMIT` 并执行总数查询。
- 列表 SQL 联查 `category` 和 `sys_user`，在同一查询中获得 `categoryName`、`sellerName`，避免逐记录查询关联数据。
- 分类 ID 与关键词均通过 MyBatis 参数绑定；关键词匹配标题或描述，禁止拼接 SQL。
- 列表按 `p.created_at DESC, p.id DESC` 排序；Service 从 `IPage` 读取当前页、每页数量、总数和记录。

## 边界与验收

- 未传分类或关键词时不附加对应条件；关键词只含空白时等价于未传。
- 页码超出总页数返回空 `records`，仍返回正确 `total`。
- `OFF_SHELF`、`LOCKED`、`SOLD` 商品不可出现在该接口。
- Postman 覆盖默认分页、分类筛选、关键词搜索、组合筛选、非法分页参数和下架商品不可见。
