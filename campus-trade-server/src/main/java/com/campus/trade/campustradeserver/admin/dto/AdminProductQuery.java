package com.campus.trade.campustradeserver.admin.dto;

import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminProductQuery {
    @Min(value = 1,message = "页码最小为1")
    private Integer page = 1;
    @Min(value = 1,message = "每页数量最小为1")
    @Max(value = 50,message = "每页数量最大为50")
    private Integer pageSize = 12;
    @Positive(message = "sellerId应该为正整数")
    private Long sellerId;
    @Positive(message = "categoryId应该为正整数")
    private Long categoryId;
    private ProductStatus status;
    @Size(max = 60,message = "keyword最多输入60个字符")
    private String keyword;
}
