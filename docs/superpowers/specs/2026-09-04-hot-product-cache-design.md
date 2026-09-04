# 阶段 7：热门商品缓存设计

## 目标

提供公开热门商品列表：只包含前 10 条在售商品，按浏览量和创建时间排序；使用 `product:hot` Redis 缓存减少重复查询，并在商品可见性或热度变化后失效。

## 缓存与响应边界

- Redis 键固定为 `product:hot`，TTL 为 10 分钟。
- 缓存值为 `HotProductResponse` JSON 数组，只包含 `id`、`title`、`price`、`status`、`categoryId`、`categoryName`、`sellerId`、`sellerName`、`viewCount`、`createdAt`。
- 不缓存 `favorited`，因为它取决于当前登录用户；不缓存完整 Base64 图片，避免大值占用 Redis。前端以分类文字占位图展示无图片热门商品。
- 热门 SQL 固定条件 `status = ON_SALE`，排序为 `view_count DESC, created_at DESC`，最多返回 10 条。

## 服务与公开接口

`HotProductService` 是唯一的热门缓存读取、回填和失效入口：`listHotProducts()` 使用 Cache Aside，Redis 读/JSON 失败时降级查 MySQL 并记录不含业务数据的 warning；`evictHotProductCache()` 尽力删除键，失败仅记录 warning。

新增公开 `GET /api/products/hot`，不要求登录；安全配置必须在 `anyRequest().authenticated()` 前明确放行。接口不接收用户 ID、筛选或分页参数。

## 失效触发点

以下数据库操作成功后删除 `product:hot`，不直接重建：

- 商品发布、编辑、卖家下架、管理员下架；
- 公开商品详情成功增加浏览量；
- 订单创建使商品 `ON_SALE → LOCKED`、取消使商品 `LOCKED → ON_SALE`、付款使商品 `LOCKED → SOLD`。

订单和商品事务仍以 MySQL 为准；缓存删除不可影响已经成功的业务写入。

订单创建、取消、付款属于事务方法，热门缓存失效必须注册为事务 `afterCommit` 回调：只有 MySQL 事务实际提交后才删除 `product:hot`；事务回滚时不执行删除。普通商品发布、编辑、下架和浏览量更新不在订单事务内，可在成功 SQL 后立即失效。

## 前端

商品广场在筛选区和原商品分页区域之间增加“本周热门”横向卡片带，标注 `TRENDING NOW` 与“按浏览热度排序”。热门区域独立加载并支持 loading、错误、空状态；加载失败不阻断普通商品分页区域。

热门卡使用分类文字占位图，不请求或展示完整图片 Base64；展示标题、价格、浏览量和卖家，点击卡片跳转已有商品详情页。热门接口仅返回在售商品，前端不显示状态操作或收藏状态；该区域作为热度提示，不替代原来的搜索、分类筛选和分页主列表。

## 验收

1. 删除 `product:hot` 后，首次 `GET /api/products/hot` 查 MySQL 并写 Redis；第二次命中缓存。
2. 热门列表仅有在售商品，最多 10 条，排序符合浏览量和创建时间规则。
3. 发布、编辑、下架、浏览量增加、下单、取消、付款后，缓存键均被删除；下一次读取重新回填。
4. Redis 不可用时热门接口降级查 MySQL；日志不含缓存 JSON、JWT、密码或 Base64。
5. 按用户决定，不运行构建、格式化或自动测试，以 Postman、日志、`redis-cli` 和浏览器手动验收。
