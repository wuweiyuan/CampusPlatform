package com.campus.trade.campustradeserver.auth.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
}
