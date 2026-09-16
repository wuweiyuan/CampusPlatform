package com.campus.trade.campustradeserver.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改分类请求，名称和排序至少填写一个")
public class CategoryUpdateRequest {
    @Pattern(
            regexp = ".*\\S.*",
            message = "分类名称不能为空或全为空格"
    )
    @Size(max = 30 ,message = "分类名称长度不能超过30个字符")
    @Schema(description = "分类名称，最多 30 个字符")
    private String name;

    @Schema(description = "分类排序值，越小越靠前")
    private Integer sort;

    @Schema(hidden = true)
    @AssertTrue(message = "分类名称和排序不能同时为空")
    public boolean isAtLeastOneFieldPresent(){
        return name != null || sort != null;
    }
}
