# 阶段 3：分类与商品接口文档

> 基础地址：`http://localhost:8080`
>
> 所有接口均以 `/api` 开头。除特别说明外，日期时间使用 ISO 8601 字符串，例如 `2026-08-20T14:30:00`。

## 一、通用约定

### 认证

需要登录的接口必须在请求头携带：

```http
Authorization: Bearer <JWT Token>
```

`USER` 表示普通用户，`ADMIN` 表示管理员。管理员接口同时要求已登录且角色为 `ADMIN`。

### 响应格式

成功响应统一为：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

失败响应统一为：

```json
{
  "code": 3002,
  "message": "无权操作该商品",
  "data": null
}
```

请求参数校验失败或业务规则不满足时，当前项目返回 HTTP `400`；未登录或 Token 无效返回 HTTP `401`，已登录但权限不足返回 HTTP `403`。

### 通用错误码

| code | 含义 | 常见场景 |
| --- | --- | --- |
| `400` | 请求参数不合法 | 字段为空、长度不合法、价格格式不合法、分页参数不合法 |
| `401` | 未登录或 Token 无效 | 未携带、过期、伪造或已退出的 Token |
| `403` | 无权访问 | 普通用户调用管理员接口；非卖家修改商品 |
| `2001` | 分类名称已存在 | 新增或改名后与已有分类重复 |
| `2002` | 分类不存在或已停用 | 发布、修改商品时使用无效分类 |
| `3001` | 商品不存在或不可公开访问 | 查询不存在商品，或匿名访问非在售商品 |
| `3002` | 无权操作该商品 | 非卖家修改、下架商品 |
| `3003` | 当前商品状态不允许此操作 | 试图编辑或下架 `LOCKED`、`SOLD`、`OFF_SHELF` 商品 |
| `3004` | 商品图片不合法 | 不是支持的图片、Base64 无法解码或原始大小超过 2 MB |

> `401` 与 `403` 是 HTTP 状态和响应体 `code`；其余业务错误的 HTTP 状态目前均为 `400`。后续若调整全局异常处理器，保持响应体错误码和语义不变。

### 分页参数与响应

所有商品分页接口使用相同规则：

| 参数 | 类型 | 必填 | 规则 | 默认值 |
| --- | --- | --- | --- | --- |
| `page` | integer | 否 | 从 `1` 开始，最小 `1` | `1` |
| `pageSize` | integer | 否 | `1` 到 `50` | `12` |

分页响应的 `data`：

```json
{
  "page": 1,
  "pageSize": 12,
  "total": 36,
  "records": []
}
```

`total` 为符合条件的总条数；页码超出范围时，`records` 返回空数组，不是错误。

## 二、领域模型与状态规则

### 分类状态

| 状态 | 含义 |
| --- | --- |
| `ENABLED` | 启用；可在公开分类列表中出现，也可用于发布商品 |
| `DISABLED` | 停用；不出现在公开分类列表，也不能用于新发布或编辑商品 |

### 商品状态

| 状态 | 含义 | 本阶段允许的状态变化 |
| --- | --- | --- |
| `ON_SALE` | 在售 | 卖家可编辑；卖家可主动下架为 `OFF_SHELF` |
| `LOCKED` | 已锁定，预留给订单待付款场景 | 本阶段不能由卖家编辑或下架 |
| `SOLD` | 已售出，预留给订单完成场景 | 本阶段不能编辑或下架 |
| `OFF_SHELF` | 已下架 | 本阶段不能重新上架、不能编辑 |

商品发布时，后端固定写入 `ON_SALE`。客户端不能提交 `sellerId`、`sellerName`、`status`、`viewCount`、`createdAt` 或 `updatedAt`。

### 图片字段

商品只有一个图片字段 `imageBase64`，不是图片数组。该字段可为 `null`，表示不上传图片或在编辑时移除已有图片；有图片时必须是 Data URL：

```text
data:image/png;base64,iVBORw0KGgo...
```

- 仅支持 JPEG、PNG、WebP。
- 前端在转换 Base64 前检查文件类型和原始文件大小，最大 2 MB。
- 后端必须再次 Base64 解码，检查真实图片格式（不能只相信 Data URL 的 MIME 前缀）和解码后原始大小，最大 2 MB。
- 一个请求只允许一个字符串；数组或多个图片均不支持。

### 商品公开与查看次数

- 公开商品列表和公开商品详情只返回 `ON_SALE` 商品。
- 成功访问一次公开商品详情，商品的 `viewCount` 增加 `1`；学习项目允许并发情况下存在少量计数误差。
- 商品卖家、分类名称和商品描述由后端查询并组装，前端不可信任自己提交的展示信息。

## 三、数据对象

### 分类对象 `CategoryResponse`

```json
{
  "id": 1,
  "name": "教材书籍",
  "sort": 10,
  "status": "ENABLED",
  "createdAt": "2026-08-20T10:00:00",
  "updatedAt": "2026-08-20T10:00:00"
}
```

公开接口只返回 `id`、`name`、`sort`；管理员查看全部分类时返回以上完整字段。

### 商品卡片对象 `ProductPageResponse`

用于商品广场和“我的发布”的 `records` 元素：

```json
{
  "id": 101,
  "title": "九成新高等数学教材",
  "price": 25.5,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQ...",
  "status": "ON_SALE",
  "categoryId": 1,
  "categoryName": "教材书籍",
  "sellerId": 8,
  "sellerName": "zhangsan",
  "viewCount": 12,
  "createdAt": "2026-08-20T10:00:00"
}
```

公开商品广场中的每条记录固定为 `ON_SALE`；“我的发布”可包含当前用户的全部状态商品。`imageBase64` 可能为 `null`，前端应显示图片占位。

### 商品详情对象 `ProductDetailResponse`

```json
{
  "id": 101,
  "title": "九成新高等数学教材",
  "description": "同济版高等数学第七版，笔记很少，适合本学期使用。",
  "price": 25.5,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQ...",
  "status": "ON_SALE",
  "categoryId": 1,
  "categoryName": "教材书籍",
  "sellerId": 8,
  "sellerName": "zhangsan",
  "viewCount": 13,
  "createdAt": "2026-08-20T10:00:00",
  "updatedAt": "2026-08-20T10:00:00"
}
```

不返回卖家邮箱、密码或其他不必要的个人信息。

## 四、公共分类接口

### 1. 获取启用分类

- 请求地址：`GET /api/categories`
- 是否登录：否
- 作用：供商品广场筛选和发布/编辑表单选择分类。

### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    { "id": 1, "name": "教材书籍", "sort": 10 },
    { "id": 2, "name": "数码电器", "sort": 20 }
  ]
}
```

只返回 `ENABLED` 分类，按 `sort` 升序排列；`sort` 相同时按 `id` 升序排列。

## 五、管理员分类接口

> 管理页面留到阶段六；本阶段仅提供和手动验收 API。以下接口均要求 `ADMIN`。

### 1. 查看全部分类

- 请求地址：`GET /api/admin/categories`
- 是否登录：是，且必须为 `ADMIN`
- 作用：查看启用与停用分类。

成功响应 `data` 为完整 `CategoryResponse` 数组，按 `sort` 升序、`id` 升序。

### 2. 新增分类

- 请求地址：`POST /api/admin/categories`
- 是否登录：是，且必须为 `ADMIN`

请求体：

```json
{
  "name": "运动器材",
  "sort": 60
}
```

| 字段 | 必填 | 规则 |
| --- | --- | --- |
| `name` | 是 | 去除首尾空格后长度为 1 到 30；不得重复 |
| `sort` | 是 | 整数；数值越小越靠前 |

后端创建时状态固定为 `ENABLED`。成功响应的 `data` 为完整 `CategoryResponse`。

### 3. 修改分类名称或排序

- 请求地址：`PATCH /api/admin/categories/{id}`
- 是否登录：是，且必须为 `ADMIN`

请求体至少提供 `name` 或 `sort` 其中之一：

```json
{
  "name": "教材与书籍",
  "sort": 5
}
```

`name` 的规则与新增相同；未提供的字段保持原值。分类不存在时返回 `2002`，名称重复时返回 `2001`。成功响应的 `data` 为修改后的完整 `CategoryResponse`。

### 4. 启用或停用分类

- 请求地址：`PATCH /api/admin/categories/{id}/status`
- 是否登录：是，且必须为 `ADMIN`

请求体：

```json
{
  "status": "DISABLED"
}
```

`status` 只能为 `ENABLED` 或 `DISABLED`。分类不存在时返回 `2002`。停用分类不会修改既有商品的 `categoryId`，但之后不能用该分类发布或编辑商品。成功响应的 `data` 为修改后的完整 `CategoryResponse`。

## 六、公共商品接口

### 1. 商品分页列表（商品广场）

- 请求地址：`GET /api/products`
- 是否登录：否
- 作用：浏览在售商品，支持分类筛选、关键词搜索和分页。

查询参数：

| 参数 | 必填 | 规则 |
| --- | --- | --- |
| `page` | 否 | 见通用分页规则 |
| `pageSize` | 否 | 见通用分页规则，最大 50 |
| `categoryId` | 否 | 正整数；只筛选该分类的商品 |
| `keyword` | 否 | 去除首尾空格后最多 60 字符；在标题和描述中搜索 |

示例：`GET /api/products?page=1&pageSize=12&categoryId=1&keyword=高等数学`

搜索条件必须使用参数化 SQL。没有传 `categoryId` 或 `keyword` 时不加对应筛选；`keyword` 为空字符串时与未传相同。

成功响应的 `data` 为分页对象，`records` 元素为 `ProductPageResponse`。无论是否登录，都只返回 `ON_SALE` 商品。

### 2. 商品公开详情

- 请求地址：`GET /api/products/{id}`
- 是否登录：否
- 作用：查看一件在售商品的完整描述与卖家展示名。

路径参数 `id` 必须为正整数。成功响应的 `data` 为 `ProductDetailResponse`，并在响应前为该商品增加一次 `viewCount`。商品不存在或不是 `ON_SALE` 时返回 `3001`。

## 七、登录用户商品接口

### 1. 发布商品

- 请求地址：`POST /api/products`
- 是否登录：是
- 作用：以当前登录用户身份发布一件商品。

请求体：

```json
{
  "categoryId": 1,
  "title": "九成新高等数学教材",
  "description": "同济版高等数学第七版，笔记很少，适合本学期使用。",
  "price": 25.5,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQ..."
}
```

| 字段 | 必填 | 规则 |
| --- | --- | --- |
| `categoryId` | 是 | 正整数；对应分类必须存在且为 `ENABLED` |
| `title` | 是 | 去除首尾空格后 2 到 60 字符，不能全为空白 |
| `description` | 是 | 去除首尾空格后 10 到 2000 字符，不能全为空白 |
| `price` | 是 | 大于 `0`，最多两位小数，例如 `25`、`25.5`、`25.50` |
| `imageBase64` | 否 | `null` 或一个符合“图片字段”约定的 Data URL |

后端从 JWT 获取卖家 ID，固定设置状态为 `ON_SALE`、浏览量为 `0`。请求中出现 `sellerId`、`status`、`viewCount` 等额外字段时必须被忽略，不能影响持久化结果。

成功响应的 `data` 为新建后的 `ProductDetailResponse`。

### 2. 修改自己的在售商品

- 请求地址：`PUT /api/products/{id}`
- 是否登录：是
- 作用：完整更新当前用户自己的在售商品。

路径参数 `id` 必须为正整数。请求体字段和发布接口完全相同：

```json
{
  "categoryId": 1,
  "title": "九成新高等数学教材（降价）",
  "description": "同济版高等数学第七版，笔记很少，适合本学期使用。",
  "price": 20,
  "imageBase64": null
}
```

这是完整更新：`categoryId`、`title`、`description`、`price` 必须提供；`imageBase64: null` 表示移除原图。后端不得修改 `sellerId`、`status`、`viewCount`、`createdAt`。

只有“当前登录用户就是卖家”且商品状态为 `ON_SALE` 时可修改：

- 商品不存在时返回 `3001`。
- 非卖家时返回 HTTP `403`、`code: 403`。
- 状态不是 `ON_SALE` 时返回 `3003`。
- 分类无效时返回 `2002`，图片无效时返回 `3004`。

成功响应的 `data` 为更新后的 `ProductDetailResponse`。

### 3. 下架自己的在售商品

- 请求地址：`POST /api/products/{id}/off-shelf`
- 是否登录：是
- 作用：将当前用户自己的在售商品变为 `OFF_SHELF`。

不需要请求体。只有“当前登录用户就是卖家”且状态为 `ON_SALE` 时成功。`LOCKED`、`SOLD`、`OFF_SHELF` 都返回 `3003`；本阶段没有重新上架接口。

成功响应：

```json
{
  "code": 0,
  "message": "下架成功",
  "data": null
}
```

下架成功后，该商品不能出现在 `GET /api/products` 或 `GET /api/products/{id}` 的公开结果中。

### 4. 我的发布

- 请求地址：`GET /api/products/mine`
- 是否登录：是
- 作用：分页查看当前登录用户发布的全部商品，包括已下架商品。

查询参数仅为通用分页参数：`page`、`pageSize`。后端从 JWT 取得卖家 ID，客户端不能传入任意 `sellerId` 查询其他用户商品。

成功响应的 `data` 为分页对象，`records` 元素为 `ProductPageResponse`。默认按 `createdAt` 倒序、`id` 倒序排列。

## 八、路由与权限汇总

| 方法 | 路径 | 身份要求 | 作用 |
| --- | --- | --- | --- |
| `GET` | `/api/categories` | 公开 | 启用分类列表 |
| `GET` | `/api/admin/categories` | `ADMIN` | 全部分类列表 |
| `POST` | `/api/admin/categories` | `ADMIN` | 新增分类 |
| `PATCH` | `/api/admin/categories/{id}` | `ADMIN` | 修改分类名称或排序 |
| `PATCH` | `/api/admin/categories/{id}/status` | `ADMIN` | 启用或停用分类 |
| `GET` | `/api/products` | 公开 | 在售商品分页列表 |
| `GET` | `/api/products/{id}` | 公开 | 在售商品详情并增加浏览量 |
| `POST` | `/api/products` | 已登录 | 发布商品 |
| `PUT` | `/api/products/{id}` | 已登录且卖家本人 | 修改自己的在售商品 |
| `POST` | `/api/products/{id}/off-shelf` | 已登录且卖家本人 | 下架自己的在售商品 |
| `GET` | `/api/products/mine` | 已登录 | 我的发布分页列表 |

## 九、阶段三手动验收要点

1. 未登录访问分类列表、商品列表和商品详情应成功；未登录发布、修改、下架、查看“我的发布”应为 `401`。
2. 普通用户调用任一 `/api/admin/categories` 接口应为 `403`。
3. 管理员停用一个分类后，它不再出现于 `GET /api/categories`；以该分类发布或修改商品应失败。
4. 卖家发布商品后，商品广场可按分类、关键词、分页以及三者组合查到它。
5. 更换另一个用户的 Token 后，修改或下架该商品必须返回 `403`。
6. 依次提交无效分类、非法价格、伪造图片 MIME、非图片 Base64 和超过 2 MB 的图片，均应得到清晰的失败 JSON。
7. 下架商品后，匿名商品列表中不再出现；匿名访问该详情返回 `3001`；卖家仍可在“我的发布”中看到它。
8. 连续访问公开详情，确认 `viewCount` 增加。
