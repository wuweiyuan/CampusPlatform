# 商品图片校验 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在发布商品前验证单张 JPEG、PNG 或 WebP Data URL Base64 图片的声明类型、真实格式与解码后大小。

**Architecture:** `ProductImageValidator` 是无状态 Spring 组件，只负责解析和验证图片，不访问数据库或修改商品。`ProductService` 在创建 `Product` 前调用它；校验失败统一使用既有 `BusinessException(3004, "商品图片不合法")`。

**Tech Stack:** Java 17 ImageIO、TwelveMonkeys ImageIO WebP 3.12.0、Spring Boot、Jakarta `@PostConstruct`。

---

## 文件结构

- Modify: `campus-trade-server/pom.xml` —— 增加 WebP ImageIO 解析器。
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/support/ProductImageValidator.java` —— Data URL、Base64、大小和实际图片格式验证。
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/service/ProductService.java` —— 在写入数据库前调用校验器。

此计划遵从用户要求：不新建或运行测试类、不执行 Maven 编译；每步仅由代码审阅验证。

### Task 1: 加入 WebP 实际解码能力

**Files:**
- Modify: `campus-trade-server/pom.xml`

- [ ] **Step 1: 在现有 `<dependencies>` 内添加 TwelveMonkeys WebP 依赖**

```xml
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-webp</artifactId>
    <version>3.12.0</version>
</dependency>
```

将它放在 MyBatis、MySQL 等业务依赖附近即可，不能放到 `<dependencies>` 外，也不要修改任何已有依赖或 Flyway 迁移。

- [ ] **Step 2: 代码审阅依赖坐标**

确认 groupId 为 `com.twelvemonkeys.imageio`、artifactId 为 `imageio-webp`、版本为 `3.12.0`，且依赖没有 `<scope>test</scope>`；它必须在生产环境可用。

### Task 2: 创建图片校验组件

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/support/ProductImageValidator.java`

- [ ] **Step 1: 创建 `support` 包和校验器类**

```java
package com.campus.trade.campustradeserver.product.support;

import com.campus.trade.campustradeserver.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductImageValidator {
    private static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final int MAX_BASE64_LENGTH = ((MAX_IMAGE_BYTES + 2) / 3) * 4;
    private static final Pattern DATA_URL_PATTERN = Pattern.compile(
            "^data:(image/(?:jpeg|png|webp));base64,([A-Za-z0-9+/]+={0,2})$"
    );

    @PostConstruct
    void registerImageReaders() {
        ImageIO.scanForPlugins();
    }

    public void validate(String imageBase64) {
        if (imageBase64 == null) {
            return;
        }

        Matcher matcher = DATA_URL_PATTERN.matcher(imageBase64);
        if (!matcher.matches()) {
            throw invalidImage();
        }

        String declaredMimeType = matcher.group(1);
        String encodedContent = matcher.group(2);
        if (encodedContent.length() > MAX_BASE64_LENGTH) {
            throw invalidImage();
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(encodedContent);
        } catch (IllegalArgumentException exception) {
            throw invalidImage();
        }

        if (imageBytes.length > MAX_IMAGE_BYTES) {
            throw invalidImage();
        }

        validateActualImage(declaredMimeType, imageBytes);
    }

    private void validateActualImage(String declaredMimeType, byte[] imageBytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(imageBytes)
        )) {
            if (input == null) {
                throw invalidImage();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String actualMimeType = toMimeType(reader.getFormatName());
                if (!declaredMimeType.equals(actualMimeType)) {
                    throw invalidImage();
                }
                reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw invalidImage();
        }
    }

    private String toMimeType(String formatName) {
        return switch (formatName.toLowerCase(Locale.ROOT)) {
            case "jpeg", "jpg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> throw invalidImage();
        };
    }

    private BusinessException invalidImage() {
        return new BusinessException(3004, "商品图片不合法");
    }
}
```

`MAX_BASE64_LENGTH` 先限制编码字符串长度，避免超大字符串在解码时占用过多内存；之后仍以 `imageBytes.length` 强制执行“原始大小最多 2 MB”的业务规则。

- [ ] **Step 2: 代码审阅校验顺序**

确认代码按以下顺序执行：`null` 通过 → Data URL 格式 → 编码长度 → Base64 解码 → 原始大小 → ImageIO 实际解码 → MIME 与真实格式一致。所有失败分支必须抛 `3004`，没有返回 `true` 或保留未验证数据的旁路。

### Task 3: 接入商品发布服务

**Files:**
- Modify: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/service/ProductService.java`

- [ ] **Step 1: 注入校验器**

添加 import：

```java
import com.campus.trade.campustradeserver.product.support.ProductImageValidator;
```

在两个 Mapper 字段后添加：

```java
private final ProductImageValidator productImageValidator;
```

`@RequiredArgsConstructor` 会将它加入 Spring 构造方法注入，无需手写构造方法。

- [ ] **Step 2: 在创建 `Product` 前执行校验**

在 `createProduct` 中、`getEnabledCategory(request.getCategoryId());` 的下一行添加：

```java
productImageValidator.validate(request.getImageBase64());
```

修改后，完整方法应为：

```java
public Product createProduct(Long sellerId, CreateProductRequest request) {
    getEnabledCategory(request.getCategoryId());
    productImageValidator.validate(request.getImageBase64());

    Product product = new Product();
    product.setSellerId(sellerId);
    product.setCategoryId(request.getCategoryId());
    product.setTitle(request.getTitle().trim());
    product.setDescription(request.getDescription().trim());
    product.setPrice(request.getPrice());
    product.setImageBase64(request.getImageBase64());
    product.setStatus(ProductStatus.ON_SALE.name());
    product.setViewCount(0L);
    productMapper.insert(product);
    return productMapper.selectById(product.getId());
}
```

- [ ] **Step 3: 最终代码审阅**

确认发布路径是“分类有效 → 图片合法 → 组装服务端控制字段 → 插入数据库 → 查询并返回”，并确认 `ProductImageValidator` 不包含数据库 Mapper、Controller 或文件写入代码。

不要运行 `git add`、`git commit`、Maven 编译或测试；完成后由用户自行审阅 Git 状态。
