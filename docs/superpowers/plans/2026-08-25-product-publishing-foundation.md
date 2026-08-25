# 商品发布基础结构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为商品发布接口建立 `Product`、商品状态、Mapper 与安全的发布请求 DTO，并保持项目可编译和可测试。

**Architecture:** `Product` 只负责映射现有 `product` 表，`ProductStatus` 集中声明合法状态，`ProductMapper` 复用 MyBatis-Plus 通用 CRUD。`CreateProductRequest` 是 HTTP 请求与领域对象之间的输入边界，只保留客户端允许提交的字段和格式校验；卖家、商品状态和浏览量在后续 Service 中产生。

**Tech Stack:** Java 17、Spring Boot 4.1、MyBatis-Plus 3.5.14、Jakarta Validation、Lombok、JUnit 5、AssertJ。

---

## 文件结构

- 新建：`campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/enums/ProductStatus.java` —— 商品状态的唯一枚举定义。
- 新建：`campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/entity/Product.java` —— `product` 表实体。
- 新建：`campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/mapper/ProductMapper.java` —— MyBatis-Plus 数据访问入口。
- 新建：`campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/dto/CreateProductRequest.java` —— 发布商品允许的请求字段和字段级校验。
- 新建：`campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/enums/ProductStatusTests.java` —— 枚举值回归测试。
- 新建：`campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/entity/ProductTests.java` —— 表名、主键和访问器映射测试。
- 新建：`campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/mapper/ProductMapperTests.java` —— Mapper 声明测试。
- 新建：`campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/dto/CreateProductRequestTests.java` —— 请求字段、未知字段忽略和校验规则测试。

### Task 1: 商品状态枚举

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/enums/ProductStatus.java`
- Test: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/enums/ProductStatusTests.java`

- [ ] **Step 1: 写出失败的状态值测试**

```java
package com.campus.trade.campustradeserver.product.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductStatusTests {

    @Test
    void declaresExactlyTheFourProductStatuses() {
        assertThat(ProductStatus.values())
                .containsExactly(
                        ProductStatus.ON_SALE,
                        ProductStatus.LOCKED,
                        ProductStatus.SOLD,
                        ProductStatus.OFF_SHELF
                );
    }
}
```

- [ ] **Step 2: 运行测试，确认因 `ProductStatus` 不存在而失败**

Run: `mvn -q -Dtest=ProductStatusTests test`

Expected: 测试编译失败，错误指出找不到 `ProductStatus`。

- [ ] **Step 3: 实现最小枚举**

```java
package com.campus.trade.campustradeserver.product.enums;

public enum ProductStatus {
    ON_SALE,

    LOCKED,

    SOLD,

    OFF_SHELF
}
```

- [ ] **Step 4: 运行测试，确认通过**

Run: `mvn -q -Dtest=ProductStatusTests test`

Expected: `BUILD SUCCESS`。

### Task 2: `Product` 实体

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/entity/Product.java`
- Test: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/entity/ProductTests.java`

- [ ] **Step 1: 写出失败的实体映射与访问器测试**

```java
package com.campus.trade.campustradeserver.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTests {

    @Test
    void mapsProductTableAndUsesAutoIncrementId() throws NoSuchFieldException {
        assertThat(Product.class.getAnnotation(TableName.class).value()).isEqualTo("product");
        assertThat(Product.class.getDeclaredField("id").getAnnotation(TableId.class).type())
                .isEqualTo(IdType.AUTO);
    }

    @Test
    void exposesProductColumnsThroughAccessors() {
        Product product = new Product();
        product.setSellerId(7L);
        product.setCategoryId(2L);
        product.setTitle("九成新高等数学教材");
        product.setPrice(new BigDecimal("25.50"));
        product.setStatus("ON_SALE");
        product.setViewCount(0L);

        assertThat(product.getSellerId()).isEqualTo(7L);
        assertThat(product.getCategoryId()).isEqualTo(2L);
        assertThat(product.getTitle()).isEqualTo("九成新高等数学教材");
        assertThat(product.getPrice()).isEqualByComparingTo("25.50");
        assertThat(product.getStatus()).isEqualTo("ON_SALE");
        assertThat(product.getViewCount()).isZero();
    }
}
```

- [ ] **Step 2: 运行测试，确认因 `Product` 不存在而失败**

Run: `mvn -q -Dtest=ProductTests test`

Expected: 测试编译失败，错误指出找不到 `Product`。

- [ ] **Step 3: 实现与 `V3__create_product.sql` 一致的实体**

```java
package com.campus.trade.campustradeserver.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal price;

    private String imageBase64;

    private String status;

    private Long viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

- [ ] **Step 4: 运行实体测试，确认通过**

Run: `mvn -q -Dtest=ProductTests test`

Expected: `BUILD SUCCESS`。

### Task 3: `ProductMapper`

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/mapper/ProductMapper.java`
- Test: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/mapper/ProductMapperTests.java`

- [ ] **Step 1: 写出失败的 Mapper 声明测试**

```java
package com.campus.trade.campustradeserver.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTests {

    @Test
    void isAMybatisPlusMapperRegisteredForScanning() {
        assertThat(BaseMapper.class).isAssignableFrom(ProductMapper.class);
        assertThat(ProductMapper.class.isAnnotationPresent(Mapper.class)).isTrue();
    }
}
```

- [ ] **Step 2: 运行测试，确认因 `ProductMapper` 不存在而失败**

Run: `mvn -q -Dtest=ProductMapperTests test`

Expected: 测试编译失败，错误指出找不到 `ProductMapper`。

- [ ] **Step 3: 实现 Mapper**

```java
package com.campus.trade.campustradeserver.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.campustradeserver.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
```

- [ ] **Step 4: 运行 Mapper 测试，确认通过**

Run: `mvn -q -Dtest=ProductMapperTests test`

Expected: `BUILD SUCCESS`。

### Task 4: 发布请求 DTO

**Files:**
- Create: `campus-trade-server/src/main/java/com/campus/trade/campustradeserver/product/dto/CreateProductRequest.java`
- Test: `campus-trade-server/src/test/java/com/campus/trade/campustradeserver/product/dto/CreateProductRequestTests.java`

- [ ] **Step 1: 写出失败的 DTO 字段和校验测试**

```java
package com.campus.trade.campustradeserver.product.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CreateProductRequestTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void ignoresServerControlledFieldsWhenDeserializing() throws Exception {
        String json = """
                {
                  "categoryId": 1,
                  "title": "九成新高等数学教材",
                  "description": "同济版高等数学第七版，笔记很少，适合本学期使用。",
                  "price": 25.50,
                  "sellerId": 99,
                  "status": "SOLD",
                  "viewCount": 100
                }
                """;

        CreateProductRequest request = objectMapper.readValue(json, CreateProductRequest.class);

        assertThat(request.getCategoryId()).isEqualTo(1L);
        assertThat(request.getTitle()).isEqualTo("九成新高等数学教材");
        assertThat(request.getPrice()).isEqualByComparingTo("25.50");
        assertThat(objectMapper.writeValueAsString(request))
                .doesNotContain("sellerId", "status", "viewCount");
    }

    @Test
    void rejectsInvalidPublishFields() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(0L);
        request.setTitle(" ");
        request.setDescription("太短");
        request.setPrice(new BigDecimal("1.999"));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("categoryId", "title", "description", "price");
    }
}
```

- [ ] **Step 2: 运行测试，确认因 `CreateProductRequest` 不存在而失败**

Run: `mvn -q -Dtest=CreateProductRequestTests test`

Expected: 测试编译失败，错误指出找不到 `CreateProductRequest`。

- [ ] **Step 3: 实现仅含客户端允许字段的 DTO**

```java
package com.campus.trade.campustradeserver.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProductRequest {
    @NotNull(message = "分类不能为空")
    @Positive(message = "分类 ID 必须为正整数")
    private Long categoryId;

    @NotBlank(message = "商品标题不能为空")
    @Size(min = 2, max = 60, message = "商品标题长度必须在2到60个字符之间")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(min = 10, max = 2000, message = "商品描述长度必须在10到2000个字符之间")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "商品价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "商品价格最多两位小数")
    private BigDecimal price;

    private String imageBase64;
}
```

- [ ] **Step 4: 运行 DTO 测试，确认通过**

Run: `mvn -q -Dtest=CreateProductRequestTests test`

Expected: `BUILD SUCCESS`。

### Task 5: 全量回归和手动核对

**Files:**
- Modify: 无

- [ ] **Step 1: 运行后端完整测试套件**

Run: `mvn -q test`

Expected: `BUILD SUCCESS`，并包含新建的四组商品基础结构测试。

- [ ] **Step 2: 运行编译，验证生产代码独立通过**

Run: `mvn -q -DskipTests compile`

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: 手动核对边界**

确认 `CreateProductRequest` 中没有 `sellerId`、`status`、`viewCount`、`createdAt` 或 `updatedAt` 字段；确认没有修改 `V3__create_product.sql` 或已执行的其他 Flyway 迁移。

- [ ] **Step 4: 检查变更范围，交由用户处理 Git**

Run: `git status --short && git diff --check`

Expected: 只出现本计划列出的 Product 源码、测试文件和学习文档；`git diff --check` 无输出。

不要运行 `git add` 或 `git commit`。完成检查后，由用户审阅、暂存并提交。
