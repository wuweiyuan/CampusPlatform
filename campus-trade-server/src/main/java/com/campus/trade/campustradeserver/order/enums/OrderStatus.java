package com.campus.trade.campustradeserver.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING_PAYMENT("PENDING_PAYMENT","待付款"),
    CANCELLED("CANCELLED","已取消"),
    PAID("PAID","已付款"),
    COMPLETED("COMPLETED","已完成");

    @EnumValue
    private final String code;
    private final String description;

    OrderStatus(String code, String description){
            this.code = code;
            this.description = description;
    }
}
