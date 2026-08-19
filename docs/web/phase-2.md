# 阶段 2：前端认证与权限接口说明

## 路由规划

| 路径 | 页面 | 权限 |
| --- | --- | --- |
| `/login` | 登录 | 公开 |
| `/register` | 注册 | 公开 |
| `/` | 商品广场占位页 | 已登录 |
| `/profile` | 个人中心 | 已登录 |
| `/products/new`、`/my-products`、`/favorites`、`/orders` | 后续功能占位页 | 已登录 |
| `/admin` | 管理端预留页 | `ADMIN` |
| `/403` | 无权限 | 公开 |
| `/404` | 未找到页面 | 公开 |

未登录访问需要登录的路由时，跳转到 `/login?redirect=原始地址`。普通用户访问 `/admin` 时跳转到 `/403`。

## 认证接口

所有请求的基础路径是 `/api`。已登录请求由 Axios 自动添加 `Authorization: Bearer <token>`。

| 方法 | 接口 | 用途 |
| --- | --- | --- |
| `POST` | `/auth/email-code` | 发送注册验证码，`{ email }` |
| `POST` | `/auth/register` | 注册，`{ username, email, password, emailCode }` |
| `POST` | `/auth/login` | 登录，`{ username, password }` |
| `GET` | `/auth/me` | 获取可信的当前用户信息 |
| `POST` | `/auth/logout` | 退出并使当前 Token 失效 |

登录成功后，先保存 Token，再请求 `/auth/me`，只把 `{ id, username, email, role }` 保存到 Pinia 和 localStorage；密码与验证码不会保存。受保护请求收到 `401` 时，前端清空本地会话并跳转登录页。
