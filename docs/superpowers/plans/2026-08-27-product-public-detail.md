# 商品公开详情与浏览量 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 提供公开商品详情，并在每次成功访问时原子地增加浏览量。

**Architecture:** Mapper 以状态条件更新浏览量，成功后联查商品、分类和卖家数据；Service 根据受影响行数处理公开访问边界；Controller 复用详情响应对象和路径 ID 校验；Security 精确放行详情 GET 路由。

**Tech Stack:** Java 17、Spring Boot、Spring Security、MyBatis-Plus/MyBatis 注解 SQL、MySQL。

---

## 文件地图

- Modify: `product/mapper/ProductMapper.java` — 条件自增与详情联表查询。
- Modify: `product/service/ProductService.java` — 公开详情业务编排。
- Modify: `product/controller/ProductController.java` — `GET /api/products/{id}`。
- Modify: `common/config/SecurityConfig.java` — 精确放行详情 GET。

### Task 1: 先写 Mapper 数据库操作

- [ ] 在 `ProductMapper` 添加条件更新方法：仅当 ID 存在且状态是 `ON_SALE` 时执行 `view_count = view_count + 1`，返回受影响行数。
- [ ] 添加详情联表查询：查询 ID 对应的 `ON_SALE` 商品，联查分类名和卖家用户名，并完整映射为 `ProductDetailResponse`。
- [ ] 使用 `#{id}` 参数绑定路径 ID；查询与更新条件都必须限制 `ON_SALE`。

### Task 2: 测试优先编排详情业务

- [ ] 先写失败测试：更新受影响行数为 `0` 时，`ProductService` 抛出业务码 `3001`；更新成功时，返回 Mapper 查询到的详情。
- [ ] 运行测试并确认其因详情方法尚未实现而失败。
- [ ] 在 `ProductService` 新增详情方法：先调用条件自增；为 `0` 立即抛 `3001`；否则查询详情并返回。
- [ ] 运行测试，确认成功路径不会遗漏浏览量自增。

### Task 3: 暴露公开 HTTP 路由

- [ ] 在 `ProductController` 添加 `GET /{id}`：先复用 `validateProductId`，再调用详情 Service，返回 `ApiResponse<ProductDetailResponse>`。
- [ ] 在 `SecurityConfig` 使用 `RegexRequestMatcher.regexMatcher(HttpMethod.GET, "^/api/products/[1-9]\\d*$")` 放行仅由正整数组成的详情路径；不能使用 `/api/products/*`，否则会公开需要登录的 `/api/products/mine`。已有精确 `/api/products` 规则继续保留。
- [ ] 用 Postman 验收：未登录访问在售详情；连续两次确认 `viewCount` 递增；不存在或下架商品返回 `3001`；`id=0` 返回 `400`。

## 自检

- 条件更新保证下架商品不增加浏览量。
- 查询返回已有 `ProductDetailResponse`，不新增或泄露敏感字段。
- 本次不实现“我的发布”、重新上架、收藏或订单逻辑。
