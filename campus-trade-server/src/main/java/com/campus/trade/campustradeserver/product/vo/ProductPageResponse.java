package com.campus.trade.campustradeserver.product.vo;

import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductPageResponse {
    private Long id;
    private String title;
    private BigDecimal price;
    private String imageBase64;
    private ProductStatus status;

    private Long categoryId;
    private String categoryName;

    private Long sellerId;
    private String sellerName;

    private Long viewCount;
    private LocalDateTime createdAt;
}
