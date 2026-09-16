package com.campus.trade.campustradeserver.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员商品查询条件")
public class AdminProductQuery {
    @Min(value = 1,message = "页码最小为1")
    @Schema(description = "页码，从 1 开始，默认 1", defaultValue = "1")
    private Integer page = 1;
    @Min(value = 1,message = "每页数量最小为1")
    @Max(value = 50,message = "每页数量最大为50")
    @Schema(description = "每页数量，范围 1～50，默认 12", defaultValue = "12")
    private Integer pageSize = 12;
    @Positive(message = "sellerId应该为正整数")
    @Schema(description = "卖家用户 ID，正整数；可选筛选条件")
    private Long sellerId;
    @Positive(message = "categoryId应该为正整数")
    @Schema(description = "分类 ID，正整数；可选筛选条件")
    private Long categoryId;
    @Schema(description = "商品状态：ON_SALE 在售、LOCKED 订单锁定、SOLD 已售、OFF_SHELF 已下架；可选筛选条件")
    private ProductStatus status;
    @Size(max = 60,message = "keyword最多输入60个字符")
    @Schema(description = "商品标题或描述的模糊搜索关键词，最多 60 个字符；不填则不按关键词筛选；可选筛选条件")
    private String keyword;
}
