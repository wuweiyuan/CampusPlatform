package com.campus.trade.campustradeserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3,max = 32,message = "用户名长度必须在3到32位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8,max = 64,message = "密码长度必须在8到64位之间")
    private String password;

}
