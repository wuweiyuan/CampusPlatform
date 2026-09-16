package com.campus.trade.campustradeserver.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.user.enums.UserStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户状态更新请求")
public class UserStatusUpdateRequest {
    @NotNull(message = "用户状态不能为空")
    @Schema(description = "用户状态：1 启用、0 禁用", type = "integer", allowableValues = {"0", "1"}, example = "1")
    private UserStatus status;
}
