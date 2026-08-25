# 商品图片校验设计

## 目标

在商品发布前安全校验单张 Data URL Base64 图片，支持 JPEG、PNG 和 WebP，防止客户端伪造 MIME 类型、提交非图片或超过 2 MB 的原始文件。

## 方案

新增 TwelveMonkeys 的 WebP ImageIO 依赖，并创建 `product.support.ProductImageValidator`。Java 标准库可处理 JPEG 和 PNG；该依赖补足 WebP 的实际解码能力。

校验器接收 `String imageBase64`：`null` 表示不上传图片，直接通过。非空字符串必须为一个 Data URL，声明类型仅限 `image/jpeg`、`image/png` 或 `image/webp`，且必须带有 `;base64,`。

校验器 Base64 解码后检查字节长度不超过 `2 * 1024 * 1024`。随后使用 ImageIO 读取实际图片，必须能完整解码，并将读取到的真实格式与 Data URL MIME 类型比对。格式无法识别、解码失败、声明与真实格式不一致或超过大小上限时，统一抛出 `BusinessException(3004, "商品图片不合法")`。

## 数据流

`ProductService.createProduct` 在组装 `Product` 前调用校验器；校验通过后保留原始 Data URL 到 `Product.imageBase64`。校验器不保存文件、不访问数据库，也不接受多张图片。Controller 尚未创建，因此该规则在接口公开前已被服务层保护。

## 边界

本小步不实现图片压缩、对象存储、图片数组、编辑接口或 Controller。前端图片校验只能作为体验优化，不能替代本校验器。
