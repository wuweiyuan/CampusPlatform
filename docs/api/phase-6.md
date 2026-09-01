# 阶段 6：管理员分类管理接口文档

> 基础地址：`http://localhost:8080`。本文件先记录管理员分类管理接口；用户、商品、订单管理接口在各自小节完成前追加。

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
