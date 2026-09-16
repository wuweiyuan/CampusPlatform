package com.campus.trade.campustradeserver.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "修改在售商品请求")
public class UpdateProductRequest {
    @NotNull(message = "分类不能为空")
    @Positive(message = "分类ID必须为正整数")
    @Schema(description = "启用分类的 ID，正整数，必填")
    private Long categoryId;

    @NotBlank(message = "商品标题不能为空")
    @Size(min = 2,max = 60,message = "商品标题长度必须在2到60个字符之间")
    @Schema(description = "商品标题，2～60 个字符，必填")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(min = 10,max = 2000,message = "商品描述长度必须在10到2000个字符之间")
    @Schema(description = "商品描述，10～2000 个字符，必填")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.00",inclusive = false,message = "商品价格必须大于0")
    @Digits(integer = 8,fraction = 2,message = "商品价格最多两位小数")
    @Schema(description = "商品价格，单位：元；必须大于 0，整数最多 8 位，小数最多 2 位")
    private BigDecimal price;

    @Schema(description = "可选图片；传入完整 data:image/jpeg、png 或 webp 的 Base64 Data URL，解码后不超过 2 MiB，不接受空字符串；无图片可不传或为 null")
    private String imageBase64;
}
