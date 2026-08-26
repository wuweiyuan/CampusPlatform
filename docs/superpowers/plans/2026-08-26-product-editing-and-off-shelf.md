# 商品编辑与主动下架 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让登录用户完整编辑本人在售商品，并可主动下架本人在售商品。

**Architecture:** 新建独立的完整更新请求 DTO；Service 集中处理商品存在性、卖家所有权和在售状态，再分别更新允许的字段或状态。Controller 只从 JWT 主体取得操作者，并复用已有详情响应映射。

**Tech Stack:** Java 17、Spring Boot、Spring Security、Jakarta Bean Validation、MyBatis-Plus、JUnit 5。

---

## 文件地图

- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/dto/UpdateProductRequest.java` — 编辑请求与字段校验。
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/service/ProductService.java` — 所有权、状态与更新逻辑。
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/controller/ProductController.java` — `PUT` 与下架路由。
- Create: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/service/ProductServiceTest.java` — Service 的可重复行为验证。

### Task 1: 先定义并验证编辑请求

- [ ] 新建 `UpdateProductRequest`，字段与 `CreateProductRequest` 相同，但保持为独立类型；它应忽略未知 JSON 字段。
- [ ] 为 `categoryId`、`title`、`description`、`price` 复制创建请求已有的 Bean Validation 注解；`imageBase64` 可为空，空表示删除原图。
- [ ] 用无效请求体调用 `PUT /api/products/{id}`，确认缺分类、全空标题、短描述、价格为零或超过两位小数均返回 HTTP 400 / `code: 400`。

### Task 2: 测试优先实现 Service 状态机

- [ ] 先写针对 `ProductService` 的失败测试：商品不存在返回 `BusinessException(3001)`；卖家不匹配抛 `AccessDeniedException`；`LOCKED`、`SOLD`、`OFF_SHELF` 返回 `BusinessException(3003)`。
- [ ] 运行该测试，确认它因待新增的方法而失败；不要在测试首次执行前写生产实现。
- [ ] 在 Service 添加一个私有前置校验方法，固定顺序为“存在 → 所有权 → 状态”。
- [ ] 新增完整更新方法：先执行前置校验，再校验分类启用、校验图片，仅写入 `categoryId`、去首尾空格后的 `title` 和 `description`、`price`、`imageBase64`；调用 `updateById` 后按 ID 返回最新记录。
- [ ] 新增下架方法：先执行相同前置校验，再将状态设为 `ProductStatus.OFF_SHELF.name()`，调用 `updateById`。
- [ ] 运行 Service 测试，确认全部通过；额外检查更新路径不会改动 `sellerId`、`viewCount`、`createdAt`。

### Task 3: 暴露 HTTP 接口并手动验收

- [ ] 在 `ProductController` 添加 `PUT /{id}`：使用 `@Valid @RequestBody UpdateProductRequest` 和 `@AuthenticationPrincipal AuthenticatedUser`，调用 Service 后用已有 `toProductDetailResponse` 返回结果。
- [ ] 添加 `POST /{id}/off-shelf`：从当前用户取 ID，下架成功返回 `new ApiResponse<>(0, "下架成功", null)`。
- [ ] 启动后端并依次以 Postman 验收：本人编辑成功、编辑时清除图片、本人下架成功、跨账号编辑/下架返回 HTTP 403 与 `code: 403`、不存在返回 `3001`、三种非在售状态返回 `3003`、停用分类返回 `2002`、非法图片返回 `3004`。
- [ ] 以匿名请求确认下架商品不再被公开列表或公开详情返回；该部分依赖后续公开查询接口完成后复验。

## 自检

- 需求覆盖：完整更新、图片和分类复用校验、卖家所有权、在售状态与主动下架均有对应任务。
- 不修改 Flyway 迁移，不引入重新上架或管理员强制下架。
- 状态与错误码统一使用既有 `ProductStatus`、`BusinessException` 和 `RestAccessDeniedHandler`。
