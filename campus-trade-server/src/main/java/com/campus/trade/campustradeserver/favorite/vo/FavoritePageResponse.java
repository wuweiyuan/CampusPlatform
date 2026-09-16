package com.campus.trade.campustradeserver.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "收藏记录与商品信息")
public class FavoritePageResponse {
    @Schema(description = "收藏记录 ID")
    private Long id;

    @Schema(description = "商品 ID，正整数")
    private Long productId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品价格，单位：元")
    private BigDecimal price;

    @Schema(description = "商品图片 Data URL（包含 MIME 类型和 Base64 内容），可能为空")
    private String imageBase64;

    @Schema(description = "商品状态：ON_SALE 在售、LOCKED 订单锁定、SOLD 已售、OFF_SHELF 已下架")
    private ProductStatus status;

    @Schema(description = "分类 ID，正整数")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "卖家用户 ID，正整数")
    private Long sellerId;

    @Schema(description = "卖家用户名")
    private String sellerName;

    @Schema(description = "当前用户收藏该商品的时间")
    private LocalDateTime favoriteCreatedAt;
}
