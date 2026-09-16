package com.campus.trade.campustradeserver.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.user.enums.UserRole;
import com.campus.trade.campustradeserver.user.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理员用户信息")
public class AdminUserResponse {
    @Schema(description = "用户 ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "邮箱地址")
    private String email;
    @Schema(description = "用户角色：USER 普通用户，ADMIN 管理员")
    private UserRole role;
    @Schema(description = "用户状态：1 启用、0 禁用", type = "integer", allowableValues = {"0", "1"}, example = "1")
    private UserStatus status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;
}
