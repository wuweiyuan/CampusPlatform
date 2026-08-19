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

- [x] 本地创建 MySQL 数据库 `campus_trade`，字符集选 UTF-8。
- [x] 启动 Redis，记录连接地址和端口。
- [x] 添加依赖：MyBatis Plus、MySQL 驱动、Spring Data Redis、Spring Security Crypto、JWT、Spring Mail、数据库迁移工具（推荐 Flyway）。
- [x] 所有密码和 JWT 密钥只放 Git 忽略的 `application-local.yml` 中。
- [x] 创建 `application-local.example.yml` 作为可提交模板；基础 `application.yml` 用可选导入自动读取本机配置。

## 2. 用户表

- [x] 创建迁移文件 `V1__create_sys_user.sql`。
- [x] 表 `sys_user` 包含：`id`、`username`、`email`、`password`、`role`、`status`、`email_verified`、`created_at`、`updated_at`。
- [x] 对用户名和邮箱加唯一索引；默认角色 `USER`，默认可用，默认邮箱未验证。
- [x] 管理员只能靠初始化 SQL 或手动数据库创建；公开注册接口绝不能传入或创建 `ADMIN`。
- [x] 运行迁移，检查真实表结构。

## 3. 先写接口文档

- [x] 在认证接口文档中写明下列接口、请求字段、成功响应、权限与错误码：
  - `POST /api/auth/email-code`：发送验证码。
  - `POST /api/auth/register`：用户名、邮箱、密码、验证码。
  - `POST /api/auth/login`：用户名、密码。
  - `GET /api/auth/me`：获取当前用户，需要 JWT。
  - `POST /api/auth/logout`：退出，需要 JWT。
- [x] 参数规则：用户名 3–32 位；密码 8–64 位；邮箱格式合法；验证码刚好 6 位数字。
- [x] 错误码约定：`1001` 验证码无效/过期，`1002` 用户名或邮箱已存在，`1003` 账号禁用，`1004` 用户名或密码错误，`401` 未登录，`403` 无权访问。

## 4. 后端实现

- [x] 按上方“本阶段文件地图”逐个创建 `auth` 子包和类；先从 DTO、实体、Mapper 开始，再创建 Service、JWT 和 Controller。不要把所有类都堆在 `auth` 根包。
- [x] 验证码使用安全随机数生成 6 位数字；不得记录密码、JWT 密钥。
- [x] Redis 键：`auth:email-code:{email}`，5 分钟；`auth:email-code:cooldown:{email}`，60 秒。
- [ ] 邮箱验证码当前固定输出到后端日志；按 `MAIL_MODE` 切换 SMTP 的实现暂缓，SMTP 凭据不得提交到 Git。
- [x] 注册要在事务中进行：校验验证码 → BCrypt 加密密码 → 插入用户 → 成功后删除验证码。
- [x] 登录时同时校验账号状态与 BCrypt 密码。用户名不存在和密码错误，对外都只返回“用户名或密码错误”。
- [x] JWT 至少包含用户 ID、用户名、角色、`jti`、签发时间、过期时间；每个受保护请求都校验签名、过期和黑名单。
- [x] `/me` 的用户 ID 从已认证用户中读取，不能从前端参数读取。
- [x] 退出时把 `jti` 写入 `auth:token:blacklist:{jti}`，过期时间等于 JWT 剩余时间；之后旧 Token 请求返回 401。

## 5. 用 Postman 手动验收

- [x] 在 Apifox/Postman 创建并保存阶段 1 认证接口集合。
- [x] 已手动验证正确登录、错误密码、禁用账号、未带 Token 的 `/me`、退出后用旧 Token 访问 `/me`。
- [x] 已保存 `baseUrl` 和登录 Token 环境变量；受保护请求使用 `Authorization: Bearer {{token}}`。
- [x] 已手动检查认证主流程的状态码、`code`、`message`、`data`；Git 提交由用户自行处理。

## 阶段收尾状态

- [x] 已添加统一 403 JSON 响应处理器，并以 `/api/admin/ping` 验证：未登录为 401、`USER` 为 403、`ADMIN` 为 200。
- [ ] 自动化测试暂缓；本项目阶段 1 以 Apifox/Postman 手动验收为准。
- [ ] 命令行 `JAVA_HOME` 暂不修复；后端仍可通过 IDEA 启动与手动验证。
