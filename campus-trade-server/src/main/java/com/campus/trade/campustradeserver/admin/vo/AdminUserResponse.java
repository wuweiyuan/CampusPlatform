package com.campus.trade.campustradeserver.admin.vo;

import com.campus.trade.campustradeserver.user.enums.UserRole;
import com.campus.trade.campustradeserver.user.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
