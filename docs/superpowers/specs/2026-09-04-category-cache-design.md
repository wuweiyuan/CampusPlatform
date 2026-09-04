# 阶段 7：分类列表缓存设计

## 目标

为公开 `GET /api/categories` 增加可观察的 Cache Aside 缓存，减少重复 MySQL 查询；分类新增、编辑、启用、停用后，下一次公开读取必须得到最新数据。

## 缓存契约

- Redis 键固定为 `category:list`，TTL 为 30 分钟。
- 值是启用分类响应 DTO 的 JSON 数组，只包含 `id`、`name`、`sort`，并保持 `sort ASC, id ASC`；不缓存 `Category` Entity。
- `CategoryService` 是唯一的读取、回填和失效入口；公开 Controller 只返回 Service 的响应 DTO。

## 读取流程

1. 尝试从 `category:list` 读取 JSON。
2. 命中时反序列化为 `CategoryResponse[]` 并返回，无需查询 MySQL。
3. 未命中时查询状态为 `ENABLED` 的分类，按排序映射为响应 DTO，写入 Redis 30 分钟后返回。
4. Redis 读、反序列化或写入失败时记录不包含业务数据的 warning，直接返回 MySQL 查询结果；公开分类接口保持可用。

## 失效流程

分类新增、编辑名称/排序、启用、停用的数据库操作成功后删除 `category:list`；不直接修改缓存值。若缓存删除失败，记录 warning 但不回滚已经成功的数据库分类操作。事务失败时不得走成功失效路径。

## 验收

1. 删除 `category:list` 后首次公开查询查 MySQL 并写入缓存；第二次查询命中缓存。
2. 分类新增、编辑、启用、停用后，确认缓存键被删除；下次查询回填并返回最新列表。
3. Redis 不可用时，公开分类列表仍能从 MySQL 返回；日志不含分类 JSON、验证码、JWT、密码或 Base64。
4. 按用户决定，不运行构建、格式化或自动测试，以 Postman、日志和 `redis-cli` 手动验收。
