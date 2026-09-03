package com.campus.trade.campustradeserver.admin.dto;

import com.campus.trade.campustradeserver.order.enums.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminOrderQuery {
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    private Integer pageSize = 12;

    @Size(max = 32, message = "订单号最多32个字符")
    private String orderNo;

    private OrderStatus status;

    @Positive(message = "买家ID必须为正整数")
    private Long buyerId;

    @Positive(message = "卖家ID必须为正整数")
    private Long sellerId;
}
