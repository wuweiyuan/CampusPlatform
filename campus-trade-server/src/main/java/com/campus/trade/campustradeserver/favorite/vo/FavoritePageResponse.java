package com.campus.trade.campustradeserver.favorite.vo;

import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoritePageResponse {
    private Long id;

    private Long productId;

    private String title;

    private BigDecimal price;

    private String imageBase64;

    private ProductStatus status;

    private Long categoryId;

    private String categoryName;

    private Long sellerId;

    private String sellerName;

    private LocalDateTime favoriteCreatedAt;
}
