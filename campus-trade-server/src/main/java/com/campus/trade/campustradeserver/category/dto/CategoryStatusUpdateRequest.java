package com.campus.trade.campustradeserver.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.category.enums.CategoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "分类状态更新请求")
public class CategoryStatusUpdateRequest {
    @NotNull(message = "分类状态不能为空")
    @Schema(description = "分类状态：ENABLED 启用、DISABLED 停用")
    private CategoryStatus status;
}
