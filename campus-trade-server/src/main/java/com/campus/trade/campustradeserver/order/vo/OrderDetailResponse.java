package com.campus.trade.campustradeserver.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单详情")
public class OrderDetailResponse {
    @Schema(description = "订单 ID")
    private Long id;
    @Schema(description = "订单编号，由后端生成")
    private String orderNo;

    @Schema(description = "买家用户 ID，正整数")
    private Long buyerId;
    @Schema(description = "买家用户名")
    private String buyerName;

    @Schema(description = "卖家用户 ID，正整数")
    private Long sellerId;
    @Schema(description = "卖家用户名")
    private String sellerName;

    @Schema(description = "商品 ID，正整数")
    private Long productId;
    @Schema(description = "关联商品标题")
    private String productTitle;
    @Schema(description = "关联商品图片 Data URL，可能为空")
    private String productImageBase64;

    @Schema(description = "订单金额，单位：元，下单时按商品价格确定")
    private BigDecimal amount;
    @Schema(description = "订单状态：PENDING_PAYMENT 待付款、CANCELLED 已取消、PAID 已付款、COMPLETED 已完成")
    private OrderStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "付款时间，未付款时为空")
    private LocalDateTime paidAt;
    @Schema(description = "交易完成时间，未完成时为空")
    private LocalDateTime completedAt;
    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;
}
