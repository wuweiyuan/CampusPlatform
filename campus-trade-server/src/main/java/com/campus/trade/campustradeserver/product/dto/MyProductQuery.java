package com.campus.trade.campustradeserver.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MyProductQuery {
    @Min(value = 1,message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    private Integer pageSize = 12;

    @Size(max = 60, message = "关键词最多60个字符")
    private String keyword;


    @Pattern(
            regexp = "ON_SALE|LOCKED|SOLD|OFF_SHELF",
            message = "商品状态不合法"
    )
    private String status;
}
