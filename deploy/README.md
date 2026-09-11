# 校园交易平台部署

首次克隆或换电脑部署，请按 [新电脑 Docker 部署指南](新电脑Docker部署指南.md) 操作，包含环境准备、完整命令、验收和排错说明。

## 部署目标与当前进度

先在 Mac 本机使用 Docker Compose 验证，再部署到 Linux 服务器。

截至 2026-09-11，本机四服务构建与启动、健康接口、注册登录、发布商品、容器重建后的 MySQL 数据保留和 Nginx 路由验收已通过。未知 API 的已验证结果为匿名请求返回后端 JSON 401，尚未验证登录后的 404。
SMTP 邮件发送、备份恢复和 Linux 部署仍待完成；当前服务状态以 `docker compose ps` 为准。
详细进度以 [2026-09-11 交接记录](../docs/学习清单/阶段-9-今日交接记录-2026-09-11.md) 为准；下方原始待办尚未逐项同步。

在项目根目录停止服务：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml stop
```

下次启动（Docker 引擎须运行）：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d
docker compose --env-file deploy/.env -f deploy/docker-compose.yml ps
```

## 服务组成

| 服务 | 用途 |
| --- | --- |
| mysql | MySQL 8，保存业务数据 |
| redis | Redis 7，保存验证码和缓存 |
| server | Spring Boot 后端，Java 17 |
| nginx | 提供 Vue 页面，并代理 `/api` 请求到后端 |

前端构建使用项目声明的 Node 20.19.5。
Linux 目标主机需要安装 Docker Engine 和 Docker Compose 插件，并能够下载所需镜像。

## 网络与数据

- 默认只对外开放 Nginx 的 80 端口；主机需确保该端口可用。
- MySQL、Redis 和后端通过 Compose 内部网络通信，不发布宿主机端口。
- `mysql_data` 命名卷保存数据库数据。
- `redis_data` 命名卷保存 Redis 持久化数据，持久化方式在 Compose 中配置。
- 浏览器通过同一地址访问页面和 `/api`，前端请求使用相对路径 `/api`。
- Nginx 将 `/api/` 请求代理到 `http://server:8080/api/`。

## 环境变量

先填写 `.env.example` 模板，再在 `deploy` 目录中执行：

```bash
cp .env.example .env
```

如果 `.env` 已存在，不要重复复制覆盖实际配置。
在 `.env` 中填写实际值；真实密码和密钥不得提交到 Git。
项目的 `.gitignore` 已忽略 `.env`，`.env.example` 只保存占位值。

计划使用以下变量，后续由 Compose 映射到对应服务：

| 变量 | 用途 |
| --- | --- |
| `HTTP_PORT` | Nginx 对外端口，默认 80 |
| `MYSQL_DATABASE` | 业务数据库名称 |
| `MYSQL_USER` | 后端连接数据库使用的普通用户 |
| `MYSQL_PASSWORD` | 业务数据库用户密码 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 用户密码 |
| `REDIS_PASSWORD` | Redis 密码，同时传给 Redis 和后端 |
| `APP_JWT_SECRET` | JWT 签名密钥，使用至少 32 字节随机值的 Base64 编码 |
| `JWT_EXPIRE_SECONDS` | Token 有效期，单位为秒，默认 7200 |

这些变量不会仅因写入 `.env` 就自动配置所有服务，需要在 Compose 中显式引用。
后端 JWT 配置项为 `app.jwt.secret`，对应环境变量为 `APP_JWT_SECRET`。

## 待完成事项

- [ ] 填写 `.env.example`，只使用占位值。
- [ ] 编写后端多阶段 Dockerfile，使用 Java 17，并以非 root 用户运行。
- [ ] 编写前端多阶段 Dockerfile，使用 Node 20.19.5 构建页面。
- [ ] 配置 `.dockerignore`，排除本机私有 `application-local.yml`、真实 `.env`、`node_modules` 和 `target` 等文件。
- [ ] 编写 `nginx.conf`，配置 Vue history fallback、静态资源缓存和 API 代理。
- [ ] 编写 `docker-compose.yml`，配置四个服务、命名卷、密码和健康检查。
- [ ] 明确数据库初始化与迁移方式。
- [ ] 处理 `/api/health` 当前需要登录的问题；该接口只表示应用存活，不代表数据库和 Redis 就绪。
- [ ] 正式开放邮箱注册前实现并验证邮件发送；当前验证码仅存 Redis，设置 `MAIL_MODE=smtp` 不会自动实现发信。
- [ ] 完成配置检查、镜像构建、服务启动及 Linux 验收。
- [ ] 补充实际验证过的启动、查看日志、停止、备份、恢复和更新步骤。

## 后续验收范围

配置完成后验证以下项目，并记录部署日期、镜像标签和结果：

- 四个服务正常启动，健康检查符合预期。
- 浏览器可以打开页面、登录并完成一个需要权限的请求。
- 浏览器 API 请求访问部署地址的 `/api`，不指向 `localhost:8080`。
- Vue 深层路由刷新正常；未知 API 返回后端错误，不返回前端 HTML。
- 停止并重新创建容器后，MySQL 数据仍然保留。

日常停止服务时不要使用 `docker compose down -v`，该选项会删除数据卷。
