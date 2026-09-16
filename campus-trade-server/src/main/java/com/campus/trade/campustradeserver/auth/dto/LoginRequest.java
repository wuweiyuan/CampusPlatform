package com.campus.trade.campustradeserver.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户名密码登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3,max = 32,message = "用户名长度必须在3到32位之间")
    @Schema(description = "用户名，3～32 个字符", example = "student_demo")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8,max = 64,message = "密码长度必须在8到64位之间")
    @Schema(description = "登录密码，8～64 个字符；请输入自己的测试账号密码", format = "password")
    private String password;

}
