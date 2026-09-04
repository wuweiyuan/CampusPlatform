# Redis 键与缓存约定

> 阶段 7 文档。Redis 用于认证短期状态与读取优化；MySQL 仍是分类、商品和订单的业务数据源。

## 统一约定

- Redis 地址由 Spring 配置提供，当前本地默认是 `localhost:6379`。
- 键使用冒号分层；动态部分以 `{...}` 表示，例如 `{email}`、`{jti}`。
- 不在日志中记录验证码值、完整 JWT、密码、完整 Base64 图片或 Redis 中的完整业务 JSON。
- 缓存读取失败不改变 MySQL 的业务正确性：分类和热门商品可降级查 MySQL 并记录警告；认证相关 Redis 键不可用时必须失败并返回明确服务错误，不能绕过验证码、发送限频或退出黑名单。
- 缓存失效只在对应的数据库业务操作成功后执行；事务失败时不应按成功路径删除缓存。

## 已有认证键

| 键名/模式 | 值结构 | TTL | 谁写入 | 谁读取 | 删除时机 | Redis 不可用 |
| --- | --- | --- | --- | --- | --- | --- |
| `auth:email-code:{email}` | 6 位验证码字符串；敏感值，不记录日志 | 5 分钟 | `EmailCodeService.sendCode` | `EmailCodeService.verifyCode` | 注册成功后由 `deleteCode` 删除；否则自然过期 | 认证流程失败，返回明确服务错误。 |
| `auth:email-code:cooldown:{email}` | 固定字符串 `"1"`，表示该邮箱仍在发送冷却中 | 60 秒 | `EmailCodeService.sendCode` 通过 `setIfAbsent` 写入 | 下一次 `sendCode` 通过 `setIfAbsent` 判断 | 仅自然过期 | 发送验证码失败，不能绕过限频。 |
| `auth:token:blacklist:{jti}` | 固定字符串 `"1"`，表示该 JWT 已退出 | JWT 的剩余有效期；本地默认 JWT 生命周期为 7200 秒 | `TokenBlacklistService.addToBlacklist`，由退出登录调用 | `JwtAuthenticationFilter` 经 `TokenBlacklistService.isBlacklisted` 查询 | 仅自然过期；已过期 Token 不再需要黑名单记录 | 受保护认证失败，不能把已退出 Token 当作有效 Token。 |

> 当前开发邮件模式会在后端日志输出验证码，便于本地学习；阶段 7 新增的缓存日志不得再输出验证码值。

## 计划分类缓存

| 项目 | 约定 |
| --- | --- |
| 键名 | `category:list` |
| 值结构 | 启用分类列表的专用 JSON 数组：`id`、`name`、`sort`；按 `sort ASC, id ASC`。 |
| TTL | 30 分钟。 |
| 谁写入 | `CategoryService` 在公开分类列表 Redis 未命中后查询 MySQL 并回填。 |
| 谁读取 | 公开 `GET /api/categories` 对应的 `CategoryService` 查询路径。 |
| 何时删除 | 分类新增、编辑名称/排序、启用、停用成功后删除；不在事务失败时删除。 |
| Redis 不可用 | 记录不含业务数据的 warning，降级直接查询 MySQL；不影响分类管理写操作。 |

策略为 Cache Aside：先读 Redis；未命中查 MySQL，再写 Redis；写操作不直接更新缓存，而是删除键，下一次读再回填。

## 计划热门商品缓存

| 项目 | 约定 |
| --- | --- |
| 键名 | `product:hot` |
| 值结构 | 前 10 条热门在售商品的专用 JSON 数组：`id`、`title`、`price`、`status`、`categoryId`、`categoryName`、`sellerId`、`sellerName`、`viewCount`、`createdAt`。不缓存 `favorited`、密码、JWT 或完整 Base64 图片。 |
| TTL | 10 分钟。 |
| 谁写入 | `HotProductService` 在 `GET /api/products/hot` Redis 未命中后查询 MySQL 并回填。 |
| 谁读取 | 热门商品接口及商品广场热门区域。 |
| 何时删除 | 商品发布、编辑、下架、浏览量成功更新；订单创建、取消、付款成功导致商品状态变化后删除。学习阶段只删除，不主动重建。 |
| Redis 不可用 | 记录不含业务数据的 warning，降级查 MySQL；商品/订单事务仍只以 MySQL 为准。 |

热门排序固定为：`status = ON_SALE`，`view_count DESC`，`created_at DESC`，取前 10 条。

## 手动观察命令

以下命令仅用于本地手动验收；不要在终端输出验证码、JWT 或完整缓存 JSON：

```bash
redis-cli TTL category:list
redis-cli TTL product:hot
redis-cli EXISTS category:list
redis-cli EXISTS product:hot
redis-cli --scan --pattern 'auth:token:blacklist:*'
```

分类缓存验收至少观察：第一次请求未命中并写入、第二次请求命中、分类写操作后键被删除、下一次请求重新回填。热门商品缓存按同样的未命中、命中、失效、回填流程验收。
