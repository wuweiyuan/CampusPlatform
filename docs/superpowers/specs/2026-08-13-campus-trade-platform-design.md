# 校园二手交易平台：第一期学习规格

## 1. 目标与范围

本项目用于练习 Java 基础、Maven、Spring Boot、MySQL、MyBatis Plus、三层架构、JWT、Redis，以及 Linux、Docker、Nginx 部署。同时练习 Vue 3 前后端分离开发。

第一期交付一个校园二手交易平台：学生注册登录后发布、浏览、搜索、收藏和购买二手商品；管理员在同一个前端项目中管理分类、用户、商品与订单。

第一期不包含真实支付、校园邮箱域名限制、聊天、评价、商品审核、订单超时自动取消、对象存储和微服务。

## 2. 技术与仓库结构

- 后端：Java、Maven、Spring Boot、MyBatis Plus、MySQL、Redis、JWT、Spring Security 或等效的 JWT 鉴权实现。
- 前端：Vue 3、Vite、Element Plus、Axios、Vue Router。
- 部署：Docker、Docker Compose、Nginx。

```text
CampusPlatform/
├── campus-trade-server/       Spring Boot REST API
├── campus-trade-web/          Vue 3 单页应用
├── deploy/                    Dockerfile、Compose、Nginx 配置
└── docs/                      设计与学习记录
```

后端采用模块化单体。每个业务模块保持 `Controller → Service → Mapper → MySQL` 分层，公共内容放在 `common` 包。前端只有一个 Vue 项目，登录后根据用户角色动态显示路由和菜单；后端是唯一的权限裁决者。

## 3. 角色、认证与邮箱验证

角色只有两种：`USER`（普通学生）和 `ADMIN`（管理员）。

- 注册字段：用户名、邮箱、密码、邮箱验证码。
- 用户名与邮箱都必须唯一；密码使用 BCrypt 哈希存储，绝不存明文。
- 开发环境：发送验证码接口把 6 位验证码记录到后端日志。
- 部署环境：通过 SMTP 发送真实邮件，账号与授权码从环境变量读取，不能写入仓库。
- Redis 验证码键：`auth:email:code:{email}`，有效期 5 分钟；同一邮箱的发送频率需要限制。
- 注册接口验证验证码后创建用户，`email_verified` 置为真。
- 登录成功签发带有用户 ID、用户名、角色、唯一 `jti` 和过期时间的 JWT。
- 退出登录时将 `jti` 写入 `auth:token:blacklist:{jti}`，TTL 等于该 JWT 的剩余有效期；请求认证时先检查黑名单。
- 未认证访问受保护接口返回 401；角色不足返回 403。

## 4. 数据模型

| 表 | 关键字段 | 规则 |
|---|---|---|
| `sys_user` | `id`、`username`、`email`、`password`、`role`、`status`、`email_verified`、`created_at` | 用户名、邮箱唯一；管理员账号通过初始化数据或数据库手工创建。 |
| `category` | `id`、`name`、`sort`、`status` | 管理员维护；停用分类不能用于发布。 |
| `product` | `id`、`seller_id`、`category_id`、`title`、`description`、`price`、`image_base64`、`status`、`view_count`、`created_at`、`updated_at` | 图片使用 `LONGTEXT`；每件商品库存默认为 1。 |
| `favorite` | `id`、`user_id`、`product_id`、`created_at` | `(user_id, product_id)` 必须唯一。 |
| `orders` | `id`、`order_no`、`buyer_id`、`seller_id`、`product_id`、`amount`、`status`、`created_at`、`paid_at`、`completed_at` | `order_no` 唯一；保存金额快照，不能依赖商品后续修改的价格。 |

建议所有表都有主键、创建时间、更新时间；根据实际查询给外键字段与状态字段建立索引。是否声明物理外键由实现时决定，但 Service 层必须校验关联对象存在与归属权限。

## 5. 商品与订单状态

商品状态：

```text
ON_SALE（在售） → LOCKED（已锁定） → SOLD（已售出）
                   ↓
                 ON_SALE（待付款订单取消后恢复）

ON_SALE / LOCKED → OFF_SHELF（卖家或管理员下架；具体接口必须避免破坏已有订单）
```

订单状态：

```text
PENDING_PAYMENT（待付款） → PAID（已付款） → COMPLETED（已完成）
            ↓
       CANCELLED（已取消）
```

业务规则：

- 用户不能购买自己的商品。
- 只能对 `ON_SALE` 商品创建订单；创建订单必须以事务完成“校验商品状态、更新为 `LOCKED`、插入订单”，避免重复购买。
- 仅 `PENDING_PAYMENT` 订单可以由买家取消或执行模拟付款。取消后商品恢复 `ON_SALE`。
- 模拟付款将订单改为 `PAID`、商品改为 `SOLD`；已售商品不可再次购买。
- 仅 `PAID` 订单可由买家确认完成。
- 状态变更必须由 Service 层根据数据库当前状态校验，前端按钮显示不构成安全保障。
- 第一期不做超时自动取消；买家手动取消待付款订单。

## 6. 后端功能

### 用户与认证

发送邮箱验证码、注册、登录、退出、获取当前登录用户；管理员可分页查看和启停用户。

### 分类与商品

管理员可创建、编辑、排序、启停分类。普通用户可分页浏览商品、按分类筛选、按标题/描述关键字搜索、查看详情。卖家可发布、编辑自己的在售商品、查看自己的发布并主动下架；管理员可查看和下架任意商品。

图片暂存 Base64：前后端均限制 JPEG、PNG、WebP，原始文件最大 2 MB；后端需要解码后再次判断大小和格式。Base64 是学习阶段方案，后续可迁移为对象存储 URL。

### 收藏与订单

用户可收藏、取消收藏并分页查看我的收藏。用户可创建订单、取消待付款订单、模拟付款、确认完成，并分页查看自己的买入/卖出订单。管理员可分页查看订单。

## 7. 前端页面与路由

前端优先做到清晰可用、基础响应式，不做第一期视觉精修。

公共页面：登录、注册（验证码倒计时）、商品广场、商品详情、403、404。

普通用户菜单：商品广场、发布商品、我的发布、我的收藏、我的订单、个人中心。

管理员追加菜单：商品管理、分类管理、用户管理、订单管理。

Axios 请求拦截器附加 JWT；响应拦截器统一处理 401（清除会话并跳转登录）和常见错误提示。路由守卫限制未登录访问，并根据角色控制管理路由。商品列表、收藏、订单、管理列表使用分页、加载态、空状态和错误提示。

## 8. Redis 缓存与失效

```text
category:list                    已启用分类列表
product:hot                      热门商品列表
auth:email:code:{email}          邮箱验证码，5 分钟 TTL
auth:token:blacklist:{jti}       已退出 Token，TTL 为 Token 剩余时间
```

分类变更时删除 `category:list`。商品发布、更新、下架、付款、取消订单或浏览量更新时，删除或重建 `product:hot`。热门商品的第一期算法可以取浏览量高、状态为 `ON_SALE` 的前 N 条；具体排序规则须在实现时固定并写入 README。缓存未命中时从 MySQL 查询并回填 Redis。

## 9. 开发阶段与验收

| 阶段 | 后端重点 | 前端重点 | 验收 |
|---|---|---|---|
| 0. 初始化 | 项目、依赖、MySQL/Redis 配置、统一返回、全局异常、参数校验 | Vite、Element Plus、Axios、路由、基础布局 | 前后端启动并成功调用测试 API。 |
| 1. 注册登录 | 验证码、注册、BCrypt、JWT、黑名单退出 | 注册、登录、倒计时、Token 持久化 | 邮箱验证后才能注册；登录可进入商品页。 |
| 2. 权限与个人中心 | JWT 认证、角色授权、当前用户 | 守卫、动态菜单、个人中心 | 未登录被引导登录；学生无法访问管理员页面/API。 |
| 3. 分类与商品 | 分类 CRUD、商品 CRUD、搜索、分页、图片校验 | 广场、搜索、详情、发布/编辑、我的发布 | 卖家只能编辑自己的在售商品。 |
| 4. 收藏 | 收藏、取消、收藏分页、唯一约束 | 收藏按钮、收藏列表 | 重复收藏不产生重复记录。 |
| 5. 订单闭环 | 事务下单、取消、付款、完成 | 下单确认、订单列表、状态操作 | 状态不可越级流转，也不能重复购买。 |
| 6. 管理台 | 分类、用户、商品、订单管理 API | 管理表格、筛选、上下架、分类维护 | 管理员有权限，学生无权限。 |
| 7. Redis | 分类、热门商品、验证码、黑名单、失效策略 | 无额外核心页面 | 能验证缓存命中和更新后失效。 |
| 8. 质量 | Service/API 测试、日志、错误码、README | 空/加载/错误状态、基础移动适配 | 从注册到完成订单的全流程可复现。 |
| 9. 部署 | Dockerfile、Compose、环境变量、数据卷 | 构建静态文件 | Nginx 同域代理 `/api`，`docker compose up -d` 后可访问。 |

每阶段执行相同的学习循环：先画表结构与接口清单；自行完成 Controller、Service、Mapper；用 Apifox/Postman 测试正常、未登录、越权、参数错误四类情形；记录问题；完成阶段后提交 Git。前端在对应后端接口稳定后接入，避免只做静态页面。

## 10. 部署结构

Docker Compose 编排后端、MySQL、Redis、Nginx 四项服务。MySQL 与 Redis 挂载命名卷持久化数据；敏感 SMTP、数据库与 JWT 配置由 `.env` 或部署环境变量提供。Nginx 负责提供 Vue 构建产物，并把 `/api` 反向代理到后端。前端始终调用相对路径 `/api`，从而避免开发/生产环境跨域差异。

## 11. 完成标准

普通用户可以通过邮箱验证码注册、登录、浏览/搜索/发布/下架自己的商品、收藏商品、完成一笔模拟交易；管理员能安全管理分类、用户、商品与订单。登录退出、缓存失效、越权和非法状态转换均得到正确处理。平台可由 Docker Compose 在 Linux 环境启动，并由 Nginx 对外提供统一入口。
