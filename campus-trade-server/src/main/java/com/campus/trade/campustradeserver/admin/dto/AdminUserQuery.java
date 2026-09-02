package com.campus.trade.campustradeserver.admin.dto;

import com.campus.trade.campustradeserver.user.enums.UserRole;
import com.campus.trade.campustradeserver.user.enums.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.Data;

@Data
public class AdminUserQuery {
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    private Integer pageSize = 12;

    private String username;

    private String email;

    private UserRole role;
    private UserStatus status;
}
