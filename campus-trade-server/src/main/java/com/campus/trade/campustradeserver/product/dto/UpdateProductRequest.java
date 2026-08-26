package com.campus.trade.campustradeserver.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProductRequest {
    @NotNull(message = "分类不能为空")
    @Positive(message = "分类ID必须为正整数")
    private Long categoryId;

    @NotBlank(message = "商品标题不能为空")
    @Size(min = 2,max = 60,message = "商品标题长度必须在2到60个字符之间")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(min = 10,max = 2000,message = "商品描述长度必须在10到2000个字符之间")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.00",inclusive = false,message = "商品价格必须大于0")
    @Digits(integer = 8,fraction = 2,message = "商品价格最多两位小数")
    private BigDecimal price;

    private String imageBase64;
}
