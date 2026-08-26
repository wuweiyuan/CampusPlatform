# 商品公开分页查询 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 提供无需登录的在售商品分页列表，支持分类和关键词筛选。

**Architecture:** 请求 DTO 负责绑定与校验查询参数；MyBatis-Plus 分页拦截器为参数化关联 SQL 自动完成 MySQL 分页与总数统计；Service 规范化关键词并组装通用分页响应；Controller 暴露公开 GET 路由。

**Tech Stack:** Java 17、Spring Boot、Jakarta Bean Validation、MyBatis-Plus/MyBatis 注解 SQL、MySQL。

---

## 文件地图

- Create: `product/dto/ProductQuery.java` — 分页与筛选参数。
- Create: `product/vo/ProductPageResponse.java` — 商品卡片响应。
- Create: `common/api/PageResponse.java` — 通用分页响应。
- Create: `common/config/MybatisPlusConfig.java` — MySQL 分页拦截器。
- Modify: `product/mapper/ProductMapper.java` — 列表与总数关联查询。
- Modify: `product/service/ProductService.java` — 查询参数标准化与结果组装。
- Modify: `product/controller/ProductController.java` — `GET /api/products`。
- Modify: `common/config/SecurityConfig.java` — 精确放行公开 GET 商品列表。

### Task 1: 定义请求和响应对象

- [ ] 新建 `ProductQuery`：`page` 默认 `1`、`pageSize` 默认 `12`、`categoryId` 可空、`keyword` 可空；校验页码最小 `1`、每页 `1` 到 `50`、分类 ID 为正数、关键词最大 60 字符。
- [ ] 新建 `ProductPageResponse`，包含卡片契约中的商品字段、分类名、卖家名和创建时间；状态使用 `ProductStatus` 枚举。
- [ ] 新建泛型 `PageResponse<T>`，字段固定为 `page`、`pageSize`、`total`、`records`。

### Task 2: 配置 MyBatis-Plus 并用参数化 SQL 查询在售商品

- [ ] 新建 `MybatisPlusConfig`，注册 `MybatisPlusInterceptor` 与 `PaginationInnerInterceptor(DbType.MYSQL)`。
- [ ] 在 `ProductMapper` 添加接收 MyBatis-Plus `Page<?>` 的 `selectOnSalePage`；参数为可空分类 ID 和已标准化关键词。
- [ ] 列表 SQL 联查 `product p`、`category c`、`sys_user u`，固定 `p.status = 'ON_SALE'`，仅在参数非空时增加 `p.category_id = #{categoryId}` 和标题/描述 `LIKE CONCAT('%', #{keyword}, '%')` 条件。
- [ ] SQL 使用 `ORDER BY p.created_at DESC, p.id DESC`，不手写 `LIMIT` 或 `COUNT`；分页拦截器负责这两件事。
- [ ] 先以包含单引号和百分号的关键词调用接口，确认它作为参数搜索而不是改变 SQL 结构。

### Task 3: 组装服务与公开路由

- [ ] 在 `ProductService` 新增公开查询方法：关键词为空白时改为 `null`，创建 `Page<ProductPageResponse>`，调用 Mapper 后从 `IPage` 读取分页数据，返回 `PageResponse<ProductPageResponse>`。
- [ ] 在 `ProductController` 增加 `@GetMapping`，以 `@Valid ProductQuery` 接收查询参数，返回 `ApiResponse.success(...)`。
- [ ] 在 `SecurityConfig` 精确增加 `HttpMethod.GET, "/api/products"` 的 `permitAll()` 规则；不放行 POST、PUT、下架或我的发布路由。
- [ ] 用 Postman 验收默认分页、第二页、分类筛选、关键词搜索、两者组合、空关键词、页码越界、非法分页参数，以及下架商品不可见。

## 自检

- 列表 SQL 只使用 `#{...}` 绑定外部输入，未拼接关键词。
- 列表始终只返回 `ON_SALE` 商品，排序为最新优先。
- 当前变更不实现详情、浏览量或“我的发布”；它们留给下一小步。
