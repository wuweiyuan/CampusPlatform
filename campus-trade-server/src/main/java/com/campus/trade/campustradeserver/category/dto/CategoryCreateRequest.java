package com.campus.trade.campustradeserver.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 30 , message = "分类名称长度不能超过30个字符")
    private String name;

    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
