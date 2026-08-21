package com.campus.trade.campustradeserver.category.dto;

import com.campus.trade.campustradeserver.category.enums.CategoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryStatusUpdateRequest {
    @NotNull(message = "分类状态不能为空")
    private CategoryStatus status;
}
