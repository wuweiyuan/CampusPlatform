package com.campus.trade.campustradeserver.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理员分类信息")
public class AdminCategoryResponse {

    @Schema(description = "分类 ID")
    private Long id;

    @Schema(description = "分类名称，最多 30 个字符")
    private String name;

    @Schema(description = "分类排序值，越小越靠前")
    private Integer sort;

    @Schema(description = "分类状态：ENABLED 启用、DISABLED 停用", allowableValues = {"ENABLED", "DISABLED"})
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;
}
