package com.campus.trade.campustradeserver.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderRequest {
    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID必须为正整数")
    private Long productId;
}
