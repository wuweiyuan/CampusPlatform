# 阶段 7：热门商品与缓存接口

> 2026-09-08 对照 `ProductController`、`HotProductService`、`HotProductResponse` 整理。统一响应与失败语义见 [错误码表](error-codes.md)，缓存键与失效路径见 [缓存约定](../cache.md)。

## 获取热门商品

- 方法与地址：`GET /api/products/hot`
- 权限：允许匿名访问；无请求体、无分页或筛选参数。
- 不携带 Authorization 即为匿名请求；携带有效 Token 时会先检查 Redis 黑名单。
- 查询固定为最多 10 条 `ON_SALE` 商品，按 `view_count DESC, created_at DESC` 排序。两项都相同时没有额外稳定排序保证。
- 当前没有“最近七天”过滤条件；前端标题“本周热门”不代表接口限定一周数据。

请求示例：

```http
GET /api/products/hot HTTP/1.1
Host: localhost:8080
```

成功响应示例（示意数据，HTTP 200）：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "id": 101,
      "title": "高等数学教材",
      "price": 25.50,
      "status": "ON_SALE",
      "categoryId": 1,
      "categoryName": "教材书籍",
      "sellerId": 10,
      "sellerName": "demo_seller",
      "viewCount": 20,
      "createdAt": "2026-09-08T10:00:00"
    }
  ]
}
```

| 字段 | JSON 类型 | 含义 |
| --- | --- | --- |
| id | number | 商品 ID |
| title | string | 商品标题 |
| price | number | 商品价格；显示小数位由前端格式化 |
| status | string | 当前热门查询只返回 ON_SALE |
| categoryId / categoryName | number / string | 分类 ID 与名称 |
| sellerId / sellerName | number / string | 卖家 ID 与名称 |
| viewCount | number | 浏览次数 |
| createdAt | string | 本地日期时间，无时区偏移信息 |

没有在售商品时返回 HTTP 200、`{"code":0,"message":"ok","data":[]}`。响应不是分页对象，不包含 `total`、`records`、`favorited`、`imageBase64`、密码或 Token。

## 缓存与故障

热门使用 `product:hot`，TTL 10 分钟，Cache Aside。命中直接返回，未命中查询 MySQL 并回填；缓存读写失败记录 warning，不阻止匿名查询返回 MySQL 数据。该接口本身不增加商品浏览量；详情接口增加浏览量并失效热门缓存。

分类公开接口 `GET /api/categories` 的请求与数据结构仍见 [分类接口](phase-3.md)，其 `category:list` TTL 为 30 分钟，采用相同的匿名降级原则。缓存失效触发点以 [缓存约定](../cache.md)为准。

| 场景 | HTTP / code | 结果 |
| --- | --- | --- |
| 匿名请求、Redis 正常 | 200 / 0 | 缓存命中或 MySQL 回填 |
| 匿名请求、Redis 故障、MySQL 可用 | 200 / 0 | 降级返回数据；空数据仍成功 |
| 有效 Token 请求、黑名单读取故障 | 503 / 503 | `认证服务暂时不可用，请稍后重试`，不绕过鉴权 |

503 示例：

```json
{"code":503,"message":"认证服务暂时不可用，请稍后重试","data":null}
```

无效/已退出 Token 不应视为已登录；该路由允许匿名，不能笼统承诺任何无效 Token 都返回 401。Redis 当前连接与命令超时各 1 秒；此前匿名降级实测约 2 秒，不作为严格延迟保证，也不意味着 MySQL 故障能降级成功。

前端独立请求热门接口，热门失败时显示提示和重试，普通商品分页仍需独立处理其自身结果。
