package com.campus.trade.campustradeserver.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.user.enums.UserRole;
import com.campus.trade.campustradeserver.user.enums.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.Data;

@Data
@Schema(description = "管理员用户查询条件")
public class AdminUserQuery {
    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码，从 1 开始，默认 1", defaultValue = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    @Schema(description = "每页数量，范围 1～50，默认 12", defaultValue = "12")
    private Integer pageSize = 12;

    @Schema(description = "按用户名模糊查询，可选")
    private String username;

    @Schema(description = "按邮箱模糊查询，可选")
    private String email;

    @Schema(description = "用户角色：USER 普通用户，ADMIN 管理员；可选筛选条件")
    private UserRole role;
    @Schema(description = "用户状态：1 启用、0 禁用；可选筛选条件", type = "integer", allowableValues = {"0", "1"}, example = "1")
    private UserStatus status;
}
