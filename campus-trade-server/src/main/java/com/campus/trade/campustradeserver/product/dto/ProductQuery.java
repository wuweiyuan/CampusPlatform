package com.campus.trade.campustradeserver.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "公开商品查询条件")
public class ProductQuery {

    @Min(value = 1,message = "页码最小为1")
    @Schema(description = "页码，从 1 开始，默认 1", defaultValue = "1")
    private Integer page = 1;

    @Min(value = 1,message = "每页数量最小为1")
    @Max(value = 50,message = "每页数量最大为50")
    @Schema(description = "每页数量，范围 1～50，默认 12", defaultValue = "12")
    private Integer pageSize = 12;

    @Positive(message = "分类ID必须为正整数")
    @Schema(description = "分类 ID，正整数；可选筛选条件")
    private  Long categoryId;

    @Size(max=60,message = "关键词最多60个字符")
    @Schema(description = "商品标题或描述的模糊搜索关键词，最多 60 个字符；不填则不按关键词筛选；可选筛选条件")
    private String keyword;
}
