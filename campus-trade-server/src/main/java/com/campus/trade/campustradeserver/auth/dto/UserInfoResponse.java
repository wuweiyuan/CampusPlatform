package com.campus.trade.campustradeserver.auth.dto;

import com.campus.trade.campustradeserver.user.enums.UserRole;
import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
}
