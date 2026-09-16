package com.campus.trade.campustradeserver.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建分类请求")
public class CategoryCreateRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 30 , message = "分类名称长度不能超过30个字符")
    @Schema(description = "分类名称，最多 30 个字符")
    private String name;

    @NotNull(message = "排序值不能为空")
    @Schema(description = "分类排序值，越小越靠前")
    private Integer sort;
}
