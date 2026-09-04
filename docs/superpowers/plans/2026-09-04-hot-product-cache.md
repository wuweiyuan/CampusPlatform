# 阶段 7：热门商品缓存 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 公开返回前 10 条热门在售商品，并通过 `product:hot` Cache Aside 缓存与写后失效保持结果新鲜。

**Architecture:** `HotProductResponse` 是不含用户收藏状态与 Base64 图片的专用缓存 DTO；`HotProductService` 负责查询、Redis 回填与尽力失效；商品/订单写操作调用其失效方法，不改变 MySQL 事务正确性。

**Tech Stack:** Spring Boot、Spring Data Redis、MyBatis-Plus、Jackson JsonMapper、Vue 3。

**Verification exception:** 用户明确要求不主动运行自动测试、构建或格式化命令；以 Postman、日志、`redis-cli` 和浏览器手动验收替代。

---

### Task 1: 热门查询与缓存服务

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/common/cache/CacheKeys.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/vo/HotProductResponse.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/mapper/ProductMapper.java`
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/service/HotProductService.java`

- [ ] 添加 `PRODUCT_HOT = "product:hot"`；DTO 不含 `favorited` 与图片 Base64。
- [ ] Mapper 查询 `ON_SALE` 商品，按 `view_count DESC, created_at DESC`，限制 10 条，联查分类和卖家。
- [ ] `HotProductService.listHotProducts` 使用 Cache Aside、10 分钟 TTL；Redis 读/JSON/写失败记录 warning 并降级 MySQL。
- [ ] `evictHotProductCache` 删除键，异常不覆盖业务写操作结果。

### Task 2: 失效与公开接口

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/service/ProductService.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/admin/service/AdminProductService.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/order/service/OrderService.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/controller/ProductController.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/common/config/SecurityConfig.java`

- [ ] 商品发布、编辑、卖家下架、管理员下架、公开详情浏览量更新成功后失效。
- [ ] 订单创建、取消、付款中商品状态更新成功后注册事务 `afterCommit` 缓存失效；事务失败或回滚时不删除缓存。
- [ ] 新增公开 `GET /api/products/hot`，无请求参数；安全配置仅放行这个 `GET`。

### Task 3: 热门商品前端与验收

**Files:**
- Modify: `campus-trade-web/src/api/product.ts`
- Modify: `campus-trade-web/src/views/market/ProductMarketView.vue`
- Modify: `docs/学习清单/阶段-7-今日交接记录-2026-09-04.md`

- [ ] API 模块定义无 `favorited`、无图片字段的热门商品类型并调用公开接口；热门请求不带 Token 相关用户参数。
- [ ] 商品广场在筛选区和分页商品区之间独立加载“本周热门”横向卡片带，含 `TRENDING NOW`、热度排序说明、loading、错误和空状态；热门失败不得阻断普通商品列表。
- [ ] 热门卡只显示分类文字占位图、标题、价格、浏览量和卖家，点击跳转详情；不显示图片、收藏状态或任何状态操作。
- [ ] 用 `redis-cli`、日志、Postman 和浏览器验证未命中、命中、所有失效触发点与前端展示；如实记录未运行自动命令。
