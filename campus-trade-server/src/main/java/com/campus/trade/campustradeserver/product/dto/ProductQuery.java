package com.campus.trade.campustradeserver.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductQuery {

    @Min(value = 1,message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1,message = "每页数量最小为1")
    @Max(value = 50,message = "每页数量最大为50")
    private Integer pageSize = 12;

    @Positive(message = "分类ID必须为正整数")
    private  Long categoryId;

    @Size(max=60,message = "关键词最多60个字符")
    private String keyword;
}
