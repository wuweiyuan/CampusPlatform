package com.campus.trade.campustradeserver.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.trade.campustradeserver.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private BigDecimal amount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
