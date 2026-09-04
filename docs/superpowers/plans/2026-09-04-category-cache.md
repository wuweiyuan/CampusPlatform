# 阶段 7：分类列表缓存 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 公开分类列表使用 `category:list` Cache Aside 缓存，并在分类写操作成功后失效。

**Architecture:** `CacheKeys` 集中管理键名。`CategoryService` 读取和写入 `StringRedisTemplate`，缓存专用 `CategoryResponse` JSON；Redis 出错时记录 warning 并回退 MySQL。公开 Controller 仅接收 Service 返回的 DTO，不处理 Redis。

**Tech Stack:** Spring Boot、Spring Data Redis、MyBatis-Plus、Jackson JsonMapper。

**Verification exception:** 用户明确要求不主动运行自动测试、构建或格式化命令；以 Postman、日志和 `redis-cli` 手动验收替代。

---

### Task 1: 缓存键与响应 DTO 查询

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/common/cache/CacheKeys.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/category/service/CategoryService.java`
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/category/controller/CategoryController.java`

- [ ] `CacheKeys` 定义 `public static final String CATEGORY_LIST = "category:list"`，私有构造器防止实例化。
- [ ] `CategoryService.listEnabledCategories` 改为返回 `List<CategoryResponse>`；从 Entity 映射 DTO 的私有方法集中在 Service，公开 Controller 不再映射 Entity。
- [ ] 注入 `StringRedisTemplate` 与 `JsonMapper`，读取 `category:list` 命中时反序列化数组；未命中查 MySQL、映射 DTO、写 JSON，TTL 30 分钟。
- [ ] Redis 读、反序列化、写入异常只记录不含业务数据的 warning，并回退 MySQL 数据；不能让公开分类列表失败。

### Task 2: 分类写操作缓存失效

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/category/service/CategoryService.java`

- [ ] 新增私有 `evictCategoryListCache`，删除 `CacheKeys.CATEGORY_LIST`；删除异常记录 warning，不抛出覆盖分类写操作结果。
- [ ] 分类新增、编辑、启用、停用成功的 `insert`/`updateById` 后调用失效方法；数据库异常或前置校验失败时不得调用。

### Task 3: 手动验收与交接

**Files:**
- Modify: `docs/学习清单/阶段-7-今日交接记录-2026-09-04.md`

- [ ] 删除 `category:list` 后请求公开分类列表，观察未命中、MySQL 查询和 30 分钟缓存写入；再次请求观察命中。
- [ ] 管理员新增、编辑、启用、停用分类后确认键被删除；下次公开读取返回最新结果并重新回填。
- [ ] Redis 不可用时验证公开读取能回退 MySQL，日志不包含分类 JSON 或敏感认证数据；如实说明未运行自动命令。
