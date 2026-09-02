package com.campus.trade.campustradeserver.admin.dto;

import com.campus.trade.campustradeserver.user.enums.UserStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "用户状态不能为空")
    private UserStatus status;
}
