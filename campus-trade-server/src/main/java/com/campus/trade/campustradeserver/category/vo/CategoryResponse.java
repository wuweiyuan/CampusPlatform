package com.campus.trade.campustradeserver.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "公开分类信息")
public class CategoryResponse {
    @Schema(description = "分类 ID")
    private Long id;

    @Schema(description = "分类名称，最多 30 个字符")
    private String name;

    @Schema(description = "分类排序值，越小越靠前")
    private Integer sort;
}
