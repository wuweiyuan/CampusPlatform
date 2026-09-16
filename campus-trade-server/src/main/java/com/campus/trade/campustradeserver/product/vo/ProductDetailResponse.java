package com.campus.trade.campustradeserver.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Schema(description = "商品详情")
public class ProductDetailResponse {
    @Schema(description = "商品 ID")
    private Long id;
    @Schema(description = "商品标题")
    private String title;
    @Schema(description = "商品描述")
    private String description;
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

    @Schema(description = "商品浏览次数")
    private Long viewCount;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "当前登录用户是否已收藏该商品；匿名访问为 false")
    private boolean favorited;

}
