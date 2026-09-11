# 新电脑 Docker 部署指南

适用于首次克隆本项目后，在另一台电脑上构建和运行。当前版本已在 Mac 本机验收；其他操作系统和 CPU 架构需要按本文重新验证。Linux 公网部署还需要域名、HTTPS、备份及服务器访问控制等配置，本文主要说明单机启动。

## 1. 先理解需要准备什么

- **镜像**：运行程序所需的环境和程序文件。
- **容器**：根据镜像创建的运行实例。
- **Compose**：按 YAML 配置统一管理多个容器。
- **数据卷**：独立保存数据库文件，容器删除后仍可保留。

本项目会启动四个服务：

```text
浏览器 → Nginx
           ├─ Vue 页面
           └─ /api → Spring Boot → MySQL、Redis
```

本机不需要另装 Java、Node、Maven、MySQL 或 Redis，它们在镜像中准备。需要安装 Git、Docker 引擎及 Docker Compose v2，并能下载镜像和 Maven/npm 依赖。

## 2. 安装并启动 Docker

| 系统 | 准备方式 |
| --- | --- |
| macOS | 安装适合 Intel 或 Apple 芯片的 Docker Desktop，启动并等待引擎就绪 |
| Windows | 安装 Docker Desktop，按提示启用 WSL 2，使用 Linux 容器；推荐在已启用 Docker 集成的 WSL 终端执行本文命令 |
| Linux | 按发行版官方安装步骤安装 Docker Engine 和 Compose 插件，启动 Docker 服务并配置当前用户访问权限 |

官方安装入口：https://docs.docker.com/get-started/get-docker/

本文命令使用 Bash/zsh 语法，适用于 Mac、Linux 和 Windows WSL，不直接面向 PowerShell。还需要终端可用的 `openssl` 和 `curl`。

Mac 可用终端启动 Desktop：

```bash
open -g -a Docker
```

Linux 使用 systemd 时，安装完成后可启动引擎：

```bash
sudo systemctl start docker
```

验证客户端、引擎和 Compose：

```bash
docker info
docker compose version
docker run --rm hello-world
```

`docker info` 应正常显示 Server；`hello-world` 应输出 `Hello from Docker!`。关闭 Desktop 窗口可以，但退出 Desktop 会停止它提供的引擎。

## 3. 克隆项目并进入部署目录

从仓库页面复制真实克隆地址，替换下面的示例地址再执行：

```bash
git clone https://你的Git服务/你的账号/你的仓库.git CampusPlatform
cd CampusPlatform/deploy
```

如果已经克隆，直接进入它的 `deploy` 目录即可。需要使用包含 Docker 部署文件和修复的最新代码版本。

**从本节开始，除非特别注明，所有命令都在 `CampusPlatform/deploy` 中执行。**

该目录应有 `docker-compose.yml`、`nginx.conf` 和 `.env.example`。Compose 构建还依赖同级的前后端项目，不能只复制 deploy 文件夹。

## 4. 配置密码和密钥

第一次复制模板，已有 `.env` 时不要覆盖：

```bash
cp -n .env.example .env
```

`.env` 是隐藏文件，使用编辑器打开它。保留或按需修改这些普通配置：

```dotenv
HTTP_PORT=80
MYSQL_DATABASE=campus_trade
MYSQL_USER=campus_app
JWT_EXPIRE_SECONDS=7200
```

在终端执行下面的命令三次，每次生成不同的密码：

```bash
openssl rand -hex 24
```

分别填入 `.env` 的以下三项，替换右侧占位值：

```dotenv
MYSQL_PASSWORD=第一次生成的值
MYSQL_ROOT_PASSWORD=第二次生成的值
REDIS_PASSWORD=第三次生成的值
```

生成 JWT 密钥：

```bash
openssl rand -base64 32
```

将完整输出填入 `APP_JWT_SECRET`，末尾的 `=` 也要保留：

```dotenv
APP_JWT_SECRET=生成的完整值
```

保存 `.env`。只修改真实配置文件，`.env.example` 保留占位值。真实密码和密钥不要提交 Git 或粘贴到公开日志中。

新电脑可以生成新密码；原电脑的数据和 `.env` 不会随 Git 克隆自动迁移。已有 MySQL 数据卷时，修改 `.env` 不会自动修改数据库内已创建的账号密码，不要通过重新生成密码解决已有数据库的连接问题。

## 5. 检查、构建并启动

先检查配置：

```bash
docker compose config --quiet
```

没有报错、直接返回提示符表示配置可解析，并不表示数据库连接已经成功。

首次构建启动：

```bash
docker compose up -d --build
```

- `up`：创建并启动服务，已有服务按需更新。
- `-d`：后台运行。
- `--build`：构建前后端镜像，可复用缓存。

Docker 会下载基础镜像，在构建阶段安装依赖、编译前后端，然后创建网络、数据卷和容器。启动顺序为 MySQL/Redis 健康 → 后端健康 → Nginx 启动。

首次可能需要几分钟或更长，取决于网络和电脑性能。后端构建使用 `-DskipTests`，会编译测试代码但不执行测试；前端构建包含类型检查。

## 6. 确认能访问

```bash
docker compose ps
```

预期 MySQL、Redis、server 显示 healthy，Nginx 显示 Up；Nginx 未配置自身健康探测，不要求显示 healthy。

```bash
curl -i http://localhost/api/health
```

预期 HTTP 200，正文：

```json
{"code":0,"message":"ok","data":{"status":"UP"}}
```

浏览器打开 http://localhost 。如果设置 `HTTP_PORT=8088`，则浏览器和 curl 都改用 `http://localhost:8088`。

`localhost` 表示正在使用浏览器的这台电脑；从另一台机器访问应使用部署机器的 IP 和端口，并确保网络允许访问。

健康接口只表示后端能响应，不等于所有业务功能正常。

## 7. 注册与基本验收

首次部署使用新数据库，不会自动带入原电脑的账号和商品。

**当前项目尚未实现真实 SMTP 发信**：在页面申请验证码后，验证码保存在 Redis，有效期 5 分钟，不会显示在日志或邮箱中。本机学习验收可查询（替换邮箱）：

```bash
docker compose exec redis sh -c 'REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli GET "auth:email-code:你的邮箱@example.com"'
```

返回的六位数字填写到注册页；`(nil)` 表示不存在或已过期。正式开放注册前需要补齐 SMTP。

验收顺序：

1. 注册并登录，发布一条商品。
2. 直接访问并刷新 `/login`，确认页面或前端登录跳转正常。
3. 访问 `/page-not-exist`，确认由 Vue 处理，不显示 Nginx 默认 404 页面。
4. 检查未知 API：

```bash
curl -i http://localhost/api/not-exist
```

未登录时预期后端 JSON 401，不能返回前端 HTML；这不代表已登录情况下的 JSON 404 已验证。

5. 记住已有商品，执行下列命令重建容器，再登录确认商品仍在：

```bash
docker compose down
docker compose up -d
```

`down` 保留命名数据卷；**不要加 `-v`，它会删除数据库所在的数据卷。** 此操作验证数据持久化，不等同于备份和恢复。

## 8. 日常命令速查

均在 `deploy` 目录执行：

| 目的 | 命令 |
| --- | --- |
| 启动项目 | `docker compose up -d` |
| 查看状态 | `docker compose ps` |
| 查看包括已停止的容器 | `docker compose ps -a` |
| 查看后端日志 | `docker compose logs --tail=100 -f server` |
| 查看所有服务日志 | `docker compose logs --tail=100 -f` |
| 停止服务，保留容器和数据 | `docker compose stop` |
| 删除容器和网络，保留命名卷 | `docker compose down` |
| 代码更新后重新构建并启动 | `docker compose up -d --build` |
| 检查配置，不显示展开后的秘密值 | `docker compose config --quiet` |

日志跟踪按 Ctrl+C 退出，不停止服务。更新前先备份重要数据并查看数据库迁移要求；本文尚未提供经过验证的备份恢复流程。

若当前位于项目根目录，等价的完整启动命令是：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d
```

`--env-file` 指定变量文件，`-f` 指定编排文件。在 deploy 目录可以省略，因为 Compose 会自动发现这两个默认文件。

## 9. 常见问题

### Cannot connect to the Docker daemon

引擎未启动或 CLI 连接配置有问题。先启动 Desktop 或 Linux Docker 服务，再运行 `docker info`。引擎显示运行但仍报错时，检查 Docker context 和访问权限。

### 镜像下载或 anonymous token 超时

这是访问镜像仓库或认证服务的网络问题，不是项目 JWT 错误。可以先逐个拉取基础镜像，再重试构建：

```bash
docker pull mysql:8.0
docker pull redis:7-alpine
docker pull node:20.19.5-alpine
docker pull nginx:stable-alpine
docker pull eclipse-temurin:17-jdk-jammy
docker pull eclipse-temurin:17-jre-jammy
```

若仍超时，检查 Docker 引擎/构建器的网络、代理和 DNS；终端能联网不代表 Docker 的联网路径相同。

### 80 端口被占用

将 `.env` 中 `HTTP_PORT` 改成空闲端口，例如 `8088`，再执行 `docker compose up -d`。访问地址改为 `http://localhost:8088`。

### 服务 unhealthy、反复重启或网页 502

先定位报错服务：

```bash
docker compose ps -a
docker compose logs --tail=100 mysql redis server nginx
```

根据具体错误检查密码、启动耗时或数据库迁移；不要直接删除数据卷。

### 构建出现 Java/TypeScript 编译错误

找到日志里第一条具体编译错误，修正后重新构建。`-DskipTests` 不跳过测试编译，不应把所有构建错误都当作 Docker 网络问题。

### 换电脑后原账号和商品不见了

Git 保存代码，不保存原电脑的 Docker 数据卷。新电脑首次部署是新数据库；迁移旧数据需要数据库备份和恢复。保留原电脑数据，另行完成迁移流程。

### ARM/Apple 芯片提示没有匹配镜像

检查具体标签是否支持该机器架构，不要直接套用未知镜像源。当前本机成功记录来自 amd64 环境，其他架构尚需验证。
