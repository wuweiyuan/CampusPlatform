package com.campus.trade.campustradeserver.category.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    @Pattern(
            regexp = ".*\\S.*",
            message = "分类名称不能为空或全为空格"
    )
    @Size(max = 30 ,message = "分类名称长度不能超过30个字符")
    private String name;

    private Integer sort;

    @AssertTrue(message = "分类名称和排序不能同时为空")
    public boolean isAtLeastOneFieldPresent(){
        return name != null || sort != null;
    }
}
