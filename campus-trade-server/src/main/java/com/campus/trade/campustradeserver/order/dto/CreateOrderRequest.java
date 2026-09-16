package com.campus.trade.campustradeserver.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "创建订单请求，买家和价格由后端确定")
public class CreateOrderRequest {
    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID必须为正整数")
    @Schema(description = "商品 ID，正整数")
    private Long productId;
}
