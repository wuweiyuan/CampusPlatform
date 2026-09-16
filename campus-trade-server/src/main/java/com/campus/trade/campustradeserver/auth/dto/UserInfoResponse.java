package com.campus.trade.campustradeserver.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.campus.trade.campustradeserver.user.enums.UserRole;
import lombok.Data;

@Data
@Schema(description = "用户公开信息")
public class UserInfoResponse {
    @Schema(description = "用户 ID", example = "1")
    private Long id;
    @Schema(description = "用户名", example = "student_demo")
    private String username;
    @Schema(description = "用户邮箱", example = "student@example.com")
    private String email;
    @Schema(description = "用户角色：USER 为普通用户，ADMIN 为管理员", example = "USER")
    private UserRole role;
}
