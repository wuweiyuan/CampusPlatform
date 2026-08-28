# 我的发布：搜索与状态筛选 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让登录用户分页查看自己的全部商品，并按关键词和状态筛选。

**Architecture:** 专用查询 DTO 定义“我的发布”参数并校验状态字符串；Mapper 用 JWT 卖家 ID 固定查询范围，通过 MyBatis-Plus 自动分页；Service 标准化关键词并转为项目的分页响应；Controller 从认证主体取得卖家 ID。

**Tech Stack:** Java 17、Spring Boot、Spring Security、Jakarta Bean Validation、MyBatis-Plus、MySQL。

---

## 文件地图

- Create: `product/dto/MyProductQuery.java` — 我的发布分页、关键词和状态参数。
- Modify: `product/mapper/ProductMapper.java` — 当前卖家联表分页查询。
- Modify: `product/service/ProductService.java` — 当前卖家查询及分页响应转换。
- Modify: `product/controller/ProductController.java` — `GET /api/products/mine`。

### Task 1: 定义专用查询 DTO

- [ ] 新建 `MyProductQuery`：`page=1`、`pageSize=12`、可选 `keyword`、可选 `status`。
- [ ] 使用 `@Min`、`@Max`、`@Size` 校验分页和关键词；`status` 使用 `String` 配合 `@Pattern(regexp = "ON_SALE|LOCKED|SOLD|OFF_SHELF")`，使无效状态进入现有统一的 Bean Validation 400 响应，而不是依赖枚举参数转换异常。

### Task 2: 查询当前卖家的商品

- [ ] 在 `ProductMapper` 添加 `selectMyProductPage(Page<ProductPageResponse> page, Long sellerId, String keyword, String status)`。
- [ ] SQL 联查 `product`、`category`、`sys_user`，固定 `p.seller_id = #{sellerId}`；不得接收或信任客户端 seller ID。
- [ ] 仅在关键词非空时搜索标题和描述；仅在状态非空时按 `p.status = #{status}` 筛选。所有外部值都用 `#{...}` 绑定。
- [ ] 按 `p.created_at DESC, p.id DESC` 排序；不手写 `LIMIT` 或 `COUNT`。

### Task 3: 组装服务和登录路由

- [ ] 在 `ProductService` 新增方法：关键词 `trim()` 后为空则改为 `null`，创建 `Page<ProductPageResponse>`，调用 Mapper，并将 `IPage` 转为 `PageResponse<ProductPageResponse>`。
- [ ] 在 `ProductController` 添加 `GET /mine`：使用 `@AuthenticationPrincipal AuthenticatedUser` 获得卖家 ID，以 `@Valid MyProductQuery` 接收参数。
- [ ] 不改 `SecurityConfig`：`/api/products/mine` 不匹配数字详情正则，且会被 `anyRequest().authenticated()` 保护。
- [ ] 用 Postman 验收：未登录 `401`；默认返回当前用户的全部状态；关键词；`ON_SALE` 和 `OFF_SHELF`；关键词加状态；非法状态、非法分页参数；账号 B 不能看到账号 A 的商品。

## 自检

- 状态未传时，不附加状态 SQL 条件，因此下架商品会出现在“我的发布”。
- 关键词和状态均通过 MyBatis 参数绑定，未拼接 SQL。
- 本次不修改商品广场的公开查询契约。
