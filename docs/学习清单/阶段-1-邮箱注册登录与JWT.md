# 阶段 1：邮箱注册、登录与 JWT

## 目标

实现邮箱验证码注册、用户名密码登录、JWT 鉴权、获取当前用户、退出登录黑名单。完成后先用 Apifox/Postman 测试，不急着写页面。

## 1. 准备

- [ ] 本地创建 MySQL 数据库 `campus_trade`，字符集选 UTF-8。
- [ ] 启动 Redis，记录连接地址和端口。
- [ ] 添加依赖：MyBatis Plus、MySQL 驱动、Spring Data Redis、Spring Security Crypto、JWT、Spring Mail、数据库迁移工具（推荐 Flyway）。
- [ ] 所有密码、SMTP 授权码、JWT 密钥都只放环境变量或 Git 忽略的本地配置中。
- [ ] 创建 `.env.example`，仅写变量名：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`JWT_SECRET`、`JWT_EXPIRE_SECONDS`、`MAIL_MODE`。

## 2. 用户表

- [ ] 创建迁移文件 `V1__create_sys_user.sql`。
- [ ] 表 `sys_user` 包含：`id`、`username`、`email`、`password`、`role`、`status`、`email_verified`、`created_at`、`updated_at`。
- [ ] 对用户名和邮箱加唯一索引；默认角色 `USER`，默认可用，默认邮箱未验证。
- [ ] 管理员只能靠初始化 SQL 或手动数据库创建；公开注册接口绝不能传入或创建 `ADMIN`。
- [ ] 运行迁移，检查真实表结构。

## 3. 先写接口文档

- [ ] 在 `docs/api/phase-1.md` 写明下列接口、请求字段、成功响应、权限与错误码：
  - `POST /api/auth/email-code`：发送验证码。
  - `POST /api/auth/register`：用户名、邮箱、密码、验证码。
  - `POST /api/auth/login`：用户名、密码。
  - `GET /api/auth/me`：获取当前用户，需要 JWT。
  - `POST /api/auth/logout`：退出，需要 JWT。
- [ ] 参数规则：用户名 3–32 位；密码 8–64 位；邮箱格式合法；验证码刚好 6 位数字。
- [ ] 错误码约定：`1001` 验证码无效/过期，`1002` 用户名或邮箱已存在，`1003` 账号禁用，`1004` 用户名或密码错误，`401` 未登录，`403` 无权访问。

## 4. 后端实现

- [ ] 在 `auth` 模块中创建：DTO、`SysUser` 实体、Mapper、Service、Controller、JWT 工具/服务、JWT 过滤器、邮箱验证码服务。
- [ ] 验证码使用安全随机数生成 6 位数字；不得记录密码、JWT 密钥。
- [ ] Redis 键：`auth:email:code:{email}`，5 分钟；`auth:email:cooldown:{email}`，60 秒。
- [ ] `MAIL_MODE=log` 时只向后端日志输出收件邮箱和验证码；`MAIL_MODE=smtp` 时按环境变量调用 SMTP。不要把 SMTP 账号密码提交到 Git。
- [ ] 注册要在事务中进行：校验验证码 → BCrypt 加密密码 → 插入用户 → 成功后删除验证码。
- [ ] 登录时同时校验账号状态与 BCrypt 密码。用户名不存在和密码错误，对外都只返回“用户名或密码错误”。
- [ ] JWT 至少包含用户 ID、用户名、角色、`jti`、签发时间、过期时间；每个受保护请求都校验签名、过期和黑名单。
- [ ] `/me` 的用户 ID 从已认证用户中读取，不能从前端参数读取。
- [ ] 退出时把 `jti` 写入 `auth:token:blacklist:{jti}`，过期时间等于 JWT 剩余时间；之后旧 Token 请求返回 401。

## 5. 测试与验收

- [ ] 测试：发送成功、60 秒限频、正确码注册、错误/过期验证码、用户名/邮箱重复、密码不是明文。
- [ ] 测试：正确登录、错误密码、禁用账号、未带 Token 的 `/me`、退出后用旧 Token 访问 `/me`。
- [ ] 在 Apifox 保存以上请求；正常、未登录、参数错误都要有用例。
- [ ] 运行完整后端测试并提交 Git。
