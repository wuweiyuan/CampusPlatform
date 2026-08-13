# 阶段 9：Docker Compose 与 Nginx 部署

## 目标

在 Linux 上用 Docker Compose 启动 MySQL、Redis、Spring Boot、Nginx；Nginx 同时提供 Vue 页面和 `/api` 反向代理。

## 本阶段文件地图

```text
campus-trade-server/Dockerfile            后端镜像构建文件
campus-trade-web/Dockerfile               前端构建并交给 Nginx 的镜像文件
deploy/docker-compose.yml                 四个服务的编排
deploy/nginx.conf                         Vue 静态文件与 /api 反向代理
deploy/.env.example                       部署变量名示例，可提交
deploy/.env                               真实变量，必须忽略，不能提交
deploy/README.md                          Linux 部署、更新和验收说明
```

先右键项目根目录新建 `deploy` 文件夹，再在其中逐个新建上面标明的文件；Dockerfile 分别放在后端和前端项目根目录，不放入 `src/`。

## 清单

### 1. 部署设计

- [ ] 新建 `deploy/README.md`，写主机要求、端口、数据卷、变量、启动、查看日志、停止、备份、更新步骤。
- [ ] Compose 只有四个服务：`mysql`、`redis`、`server`、`nginx`。
- [ ] 默认只暴露 Nginx 的 `80:80`；MySQL 和 Redis 仅在 Compose 内部网络访问，不暴露宿主机端口。
- [ ] 使用命名卷 `mysql_data` 和 `redis_data`。
- [ ] 真实变量放 Git 忽略的 `deploy/.env`；只提交 `deploy/.env.example`。

### 2. 后端镜像

- [ ] 创建 `campus-trade-server/Dockerfile` 多阶段构建：Maven+JDK 阶段打包，JRE 运行阶段只复制 JAR。
- [ ] 条件允许时使用非 root 用户运行。
- [ ] 数据库、Redis、JWT、SMTP、`MAIL_MODE=smtp` 等从环境变量读取。
- [ ] 配置健康检查；现有 `/api/health` 可用，若要检查依赖可另做 readiness 接口。

### 3. 前端与 Nginx

- [ ] 用与项目声明 Node 版本一致的 Node 镜像构建 Vue `dist/`。
- [ ] Nginx 提供 Vue history fallback：`try_files ... /index.html`，并缓存不可变静态资源。
- [ ] `location /api/` 反向代理至 `http://server:8080/api/`，保留 `Host`、`X-Real-IP`、`X-Forwarded-For`。
- [ ] 前端生产请求始终为相对 `/api`，不需要额外 CORS。

### 4. Compose

- [ ] MySQL 8：环境变量配置数据库/用户/密码，挂数据卷，健康检查。
- [ ] Redis 7：按需要挂数据卷；非本地环境设置密码，并把密码传给后端。
- [ ] server 依赖 MySQL/Redis 健康（Compose 支持时）；应用本身也应妥善等待/重试依赖。
- [ ] 个人服务器可设 `restart: unless-stopped`。
- [ ] 不在 YAML 或 Git 写真实密码、SMTP 授权码、JWT 密钥。

### 5. Linux 验收

- [ ] 只把仓库代码和本机 `.env` 复制到服务器；不复制 `node_modules`、`target`、数据库备份、IDE 文件。
- [ ] 运行 `docker compose config`，先解决变量和配置问题。
- [ ] 运行 `docker compose up -d --build`，再检查 `docker compose ps` 和各服务日志。
- [ ] 运行 `curl -i http://<服务器地址>/api/health`，确认健康响应。
- [ ] 浏览器打开站点，完成登录和一个需要权限的请求；网络请求必须为 `<服务器地址>/api/...`，不能是 `localhost:8080`。
- [ ] 运行 `docker compose down` 后再 `up -d`，确认 MySQL 数据因 `mysql_data` 仍存在。
- [ ] 验证 Vue 不存在路由会回到 `index.html`；未知 API 应返回后端 JSON 错误，不能返回前端 HTML。
- [ ] 在 `deploy/README.md` 记录部署日期、镜像标签、验收结果，并提交 Git。
