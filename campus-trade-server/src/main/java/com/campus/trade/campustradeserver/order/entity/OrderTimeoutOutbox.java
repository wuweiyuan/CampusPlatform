package com.campus.trade.campustradeserver.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_timeout_outbox")
public class OrderTimeoutOutbox {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private LocalDateTime dueAt;
    private String status;
    private Integer attempts;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
