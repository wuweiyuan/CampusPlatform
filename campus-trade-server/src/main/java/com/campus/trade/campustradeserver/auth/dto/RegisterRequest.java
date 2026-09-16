package com.campus.trade.campustradeserver.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "普通用户注册请求")
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min=3,max = 32,message = "用户名的长度必须在3到32位之间")
    @Schema(description = "用户名，3～32 个字符，不能与已有用户重复", example = "student_demo")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "接收验证码的邮箱，不能与已有用户重复", example = "student@example.com")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min=8,max = 64,message = "密码长度必须在8到64位之间")
    @Schema(description = "设置登录密码，8～64 个字符；请自行填写测试密码", format = "password")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "\\d{6}",message = "验证码必须是6位数字")
    @Schema(description = "该邮箱收到的六位数字验证码，5 分钟内有效；请填写实际收到的值")
    private String emailCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailCode() {
        return emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }
}
