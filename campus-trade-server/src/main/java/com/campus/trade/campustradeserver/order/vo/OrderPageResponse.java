package com.campus.trade.campustradeserver.order.vo;

import com.campus.trade.campustradeserver.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderPageResponse {
    private Long id;
    private String orderNo;

    private Long buyerId;
    private String buyerName;

    private Long sellerId;
    private String sellerName;

    private Long productId;
    private String productTitle;
    private String productImageBase64;

    private BigDecimal amount;
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
