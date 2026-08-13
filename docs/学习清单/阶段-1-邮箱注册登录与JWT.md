# 阶段 1：邮箱注册、登录与 JWT

## 目标

实现邮箱验证码注册、用户名密码登录、JWT 鉴权、获取当前用户、退出登录黑名单。完成后先用 Apifox/Postman 测试，不急着写页面。

## 本阶段文件地图

所有 Java 文件都在 `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/` 下面创建：

```text
auth/
├── controller/AuthController.java       接收登录、注册、发送验证码、退出等 HTTP 请求
├── service/AuthService.java             注册和登录业务
├── service/EmailCodeService.java        生成、保存、校验、发送验证码
├── mapper/SysUserMapper.java            用户表数据库访问
├── entity/SysUser.java                  对应 sys_user 表
├── dto/SendEmailCodeRequest.java        发送验证码请求
├── dto/RegisterRequest.java              注册请求
├── dto/LoginRequest.java                 登录请求
├── dto/LoginResponse.java                登录成功响应
├── security/JwtService.java              创建与解析 JWT
└── security/JwtAuthenticationFilter.java 每次请求校验 JWT
```

SQL 迁移文件固定放在 `campus-trade-server/src/main/resources/db/migration/V1__create_sys_user.sql`。认证接口文档放在 `docs/api/phase-1.md`。包的创建方式都是：右键后端根包 → `New → Package` → 输入如 `auth.dto` → 在包内 `New → Java Class`。

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

- [ ] 按上方“本阶段文件地图”逐个创建 `auth` 子包和类；先从 DTO、实体、Mapper 开始，再创建 Service、JWT 和 Controller。不要把所有类都堆在 `auth` 根包。
- [ ] 验证码使用安全随机数生成 6 位数字；不得记录密码、JWT 密钥。
- [ ] Redis 键：`auth:email:code:{email}`，5 分钟；`auth:email:cooldown:{email}`，60 秒。
- [ ] `MAIL_MODE=log` 时只向后端日志输出收件邮箱和验证码；`MAIL_MODE=smtp` 时按环境变量调用 SMTP。不要把 SMTP 账号密码提交到 Git。
- [ ] 注册要在事务中进行：校验验证码 → BCrypt 加密密码 → 插入用户 → 成功后删除验证码。
- [ ] 登录时同时校验账号状态与 BCrypt 密码。用户名不存在和密码错误，对外都只返回“用户名或密码错误”。
- [ ] JWT 至少包含用户 ID、用户名、角色、`jti`、签发时间、过期时间；每个受保护请求都校验签名、过期和黑名单。
- [ ] `/me` 的用户 ID 从已认证用户中读取，不能从前端参数读取。
- [ ] 退出时把 `jti` 写入 `auth:token:blacklist:{jti}`，过期时间等于 JWT 剩余时间；之后旧 Token 请求返回 401。

## 5. 用 Postman 手动验收

- [ ] 在 Postman 创建集合“校园二手交易平台”，新建“认证”文件夹；保存：发送成功、60 秒限频、正确码注册、错误/过期验证码、用户名/邮箱重复。
- [ ] 再保存：正确登录、错误密码、禁用账号、未带 Token 的 `/me`、退出后用旧 Token 访问 `/me`。
- [ ] 用 Postman 的环境变量保存 `baseUrl` 和登录得到的 `token`；受保护请求的 Header 填 `Authorization: Bearer {{token}}`。
- [ ] 每条请求检查状态码、`code`、`message`、`data` 是否符合接口文档；全部手动通过后提交 Git。
