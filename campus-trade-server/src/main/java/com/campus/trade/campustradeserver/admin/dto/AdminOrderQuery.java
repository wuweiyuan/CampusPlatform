package com.campus.trade.campustradeserver.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.order.enums.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员订单查询条件")
public class AdminOrderQuery {
    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码，从 1 开始，默认 1", defaultValue = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    @Schema(description = "每页数量，范围 1～50，默认 12", defaultValue = "12")
    private Integer pageSize = 12;

    @Size(max = 32, message = "订单号最多32个字符")
    @Schema(description = "订单号精确查询条件，最多 32 个字符，可选")
    private String orderNo;

    @Schema(description = "订单状态：PENDING_PAYMENT 待付款、CANCELLED 已取消、PAID 已付款、COMPLETED 已完成；可选筛选条件")
    private OrderStatus status;

    @Positive(message = "买家ID必须为正整数")
    @Schema(description = "买家用户 ID，正整数；可选筛选条件")
    private Long buyerId;

    @Positive(message = "卖家ID必须为正整数")
    @Schema(description = "卖家用户 ID，正整数；可选筛选条件")
    private Long sellerId;
}
