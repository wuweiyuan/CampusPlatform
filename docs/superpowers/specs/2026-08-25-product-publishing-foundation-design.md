# 商品发布基础结构设计

## 目标

为阶段 3 的商品发布功能建立持久化对象、状态定义、数据访问入口和请求数据边界；本小步不实现商品发布业务接口。

## 范围

本小步新增以下四类 Java 对象：

- `product.entity.Product`：映射既有 `product` 表的全部列。
- `product.enums.ProductStatus`：统一声明 `ON_SALE`、`LOCKED`、`SOLD`、`OFF_SHELF` 四种商品状态。
- `product.mapper.ProductMapper`：继承 MyBatis-Plus `BaseMapper<Product>`，作为后续商品写入和读取的数据访问入口。
- `product.dto.CreateProductRequest`：定义发布商品时客户端可提交的字段。

不新增或修改 Flyway 迁移；`V3__create_product.sql` 已在数据库中执行。也不创建 Service、Controller、图片解码逻辑或接口测试。

## 对象边界

`Product` 是数据库实体，字段对应 `product` 表：`id`、`sellerId`、`categoryId`、`title`、`description`、`price`、`imageBase64`、`status`、`viewCount`、`createdAt` 与 `updatedAt`。时间字段使用 `LocalDateTime`，价格使用 `BigDecimal`；状态字段在实体中先保持 `String`，通过 `ProductStatus.xxx.name()` 写入数据库，和既有 `Category` 实现保持一致。

`CreateProductRequest` 仅允许客户端提供 `categoryId`、`title`、`description`、`price` 和可选的 `imageBase64`。它不含 `sellerId`、`status`、`viewCount` 或任何时间字段，因此这些字段只能在后续 Service 中由当前登录用户和服务端规则赋值。

## 数据流与后续衔接

后续发布 Service 会接收 `CreateProductRequest`，从认证上下文取得卖家 ID，验证分类与图片，然后创建 `Product`：状态固定为 `ON_SALE`、浏览量固定为 `0`，再由 `ProductMapper` 持久化。Controller 仅将请求交给 Service，并返回单独定义的商品响应对象。

## 验证

本小步完成后，项目应能通过 Maven 编译。自动化测试从下一步引入发布 Service 行为时先写：请求字段校验、卖家 ID 和初始状态只能由服务端设置、以及商品写入结果。
