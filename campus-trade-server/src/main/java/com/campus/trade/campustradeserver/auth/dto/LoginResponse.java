package com.campus.trade.campustradeserver.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private UserInfoResponse user;

}
