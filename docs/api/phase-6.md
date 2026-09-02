# 阶段 6：管理员后台接口文档

> 基础地址：`http://localhost:8080`。本文件记录阶段 6 已完成或已约定的管理员接口；商品、订单管理接口在各自小节完成前追加。

## 通用权限约定

所有接口均以 `/api/admin` 开头，必须携带管理员 JWT：

```http
Authorization: Bearer <ADMIN JWT Token>
```

后端安全配置统一要求 `ADMIN` 角色。未登录或 Token 无效返回 HTTP 401；普通 `USER` 即使直接请求接口也返回 HTTP 403。

成功响应统一为：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

## 分类对象

```json
{
  "id": 1,
  "name": "教材书籍",
  "sort": 10,
  "status": "ENABLED",
  "createdAt": "2026-09-01T10:00:00",
  "updatedAt": "2026-09-01T10:00:00"
}
```

- `status`：`ENABLED` 为启用，`DISABLED` 为停用。
- 分类列表按 `sort`、`id` 升序返回，且包含停用分类。

## 查看全部分类

```http
GET /api/admin/categories
```

成功时 `data` 为分类数组。

## 新增分类

```http
POST /api/admin/categories
Content-Type: application/json
```

```json
{
  "name": "教材书籍",
  "sort": 10
}
```

- `name`：必填，去除首尾空格后不能为空，最长 30 个字符，不能与现有分类重复。
- `sort`：必填整数。
- 新分类默认状态为 `ENABLED`。

## 编辑分类

```http
PATCH /api/admin/categories/{id}
Content-Type: application/json
```

```json
{
  "name": "教材与资料",
  "sort": 11
}
```

`name`、`sort` 至少传一个；名称规则与新增相同。

## 启用或停用分类

```http
PATCH /api/admin/categories/{id}/status
Content-Type: application/json
```

```json
{
  "status": "DISABLED"
}
```

分类停用后，普通用户用该分类创建或编辑商品时，后端返回 HTTP 400 与“分类不存在或已停用”；已经存在的商品和历史订单不被删除。

## 错误约定

| HTTP / code | 场景 |
| --- | --- |
| `400` | 参数缺失、名称为空、名称超过 30 个字符、状态值非法，或使用已停用分类提交商品。 |
| `403` | 当前用户不是管理员。 |
| `2001` | 分类名称已存在。 |
| `2002` | 分类不存在。 |

## 用户管理

### 用户对象

```json
{
  "id": 12,
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "role": "USER",
  "status": 1,
  "createdAt": "2026-09-01T10:00:00",
  "updatedAt": "2026-09-01T10:00:00"
}
```

- `role`：仅为 `USER` 或 `ADMIN`。
- `status`：`1` 为启用，`0` 为禁用。
- 列表项绝不返回 `password`（含密码哈希）、JWT、认证码或其他敏感认证数据。

### 分页查看与筛选用户

```http
GET /api/admin/users?page=1&pageSize=12&username=zhang&email=example.com&role=USER&status=1
```

所有查询参数均为可选；不传筛选条件时返回全部用户。

| 参数 | 类型 | 规则 | 说明 |
| --- | --- | --- | --- |
| `page` | integer | 最小为 `1`，默认 `1` | 页码。 |
| `pageSize` | integer | `1`–`50`，默认 `12` | 每页条数。 |
| `username` | string | 可选 | 按用户名模糊筛选。 |
| `email` | string | 可选 | 按邮箱模糊筛选。 |
| `role` | string | 仅 `USER`、`ADMIN` | 精确筛选角色。 |
| `status` | integer | 仅 `0`、`1` | 精确筛选状态。 |

成功时 `data` 为统一分页对象；其 `records` 中的每项均为上述用户对象。结果按创建时间、ID 倒序排列。

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 12,
        "username": "zhangsan",
        "email": "zhangsan@example.com",
        "role": "USER",
        "status": 1,
        "createdAt": "2026-09-01T10:00:00",
        "updatedAt": "2026-09-01T10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 12
  }
}
```

### 启用或禁用用户

```http
PATCH /api/admin/users/{id}/status
Content-Type: application/json
```

```json
{
  "status": 0
}
```

- `status` 必填，且只能是 `0` 或 `1`。
- 管理员可以启用或禁用其他用户（包括其他管理员），但不能禁用当前登录的管理员账号。
- 禁用后，该用户不能再次登录；禁用前签发的 JWT 访问任何受保护接口时返回 HTTP `401`。重新启用不会恢复旧 Token，用户必须重新登录。
- 此接口只变更用户启用状态，不修改用户角色、密码或资料。

### 用户管理错误约定

| HTTP / code | 场景 |
| --- | --- |
| `400` | `page`、`pageSize`、`role`、`status` 参数非法，或状态更新请求缺少/使用非法 `status`。 |
| `401` | 未登录、Token 无效，或 Token 对应用户已不存在/被禁用而访问受保护接口。 |
| `403` | 当前用户不是管理员。 |
| `1005` | 要操作的用户不存在。 |
| `1006` | 当前管理员尝试禁用自己的账号。 |
