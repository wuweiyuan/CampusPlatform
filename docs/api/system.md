# 健康检查与管理员探测接口

> 2026-09-08 按当前 Controller 和 SecurityConfig 整理。Authorization 示例仅使用本地变量占位，不保存实际 Token。错误语义见 [错误码表](error-codes.md)。

## 健康检查

- `GET /api/health`
- 无查询参数、无请求体。
- **当前要求登录**：SecurityConfig 未将其配置为公开接口，落入 `anyRequest().authenticated()`。

```http
GET /api/health HTTP/1.1
Host: localhost:8080
Authorization: Bearer {{userToken}}
```

成功（HTTP 200）：

```json
{"code":0,"message":"ok","data":{"status":"UP"}}
```

Controller 只返回固定的 UP，不执行完整依赖健康诊断，不能把它当 MySQL、Redis、邮件服务全部正常的证明。鉴权路径仍访问用户信息与黑名单。

## 管理员探测

- `GET /api/admin/ping`
- 无查询参数、无请求体。
- 要求登录且角色为 ADMIN。

```http
GET /api/admin/ping HTTP/1.1
Host: localhost:8080
Authorization: Bearer {{adminToken}}
```

成功（HTTP 200）：

```json
{"code":0,"message":"管理员访问成功","data":"pong"}
```

## 失败响应

| 接口/条件 | HTTP / code | message |
| --- | --- | --- |
| 两接口未登录、Token 无效/已退出/过期 | 401 / 401 | 未登录或 Token 无效 |
| ping 使用有效普通 USER Token，依赖正常 | 403 / 403 | 权限不足 |
| 携带有效 Token，Redis 黑名单查询故障 | 503 / 503 | 认证服务暂时不可用，请稍后重试 |

错误响应示例（HTTP 403）：

```json
{"code":403,"message":"权限不足","data":null}
```

前端 `/health` 路由没有登录守卫，与 API 权限存在差异；本文只记录现状，未改变其用途或访问规则。后续验收分别使用匿名、USER、ADMIN 检查页面及接口，参见 [H01、M06、V10 与 D-03](../manual-acceptance.md)。
