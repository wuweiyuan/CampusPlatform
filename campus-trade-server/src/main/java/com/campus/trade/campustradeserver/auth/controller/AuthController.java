package com.campus.trade.campustradeserver.auth.controller;

import com.campus.trade.campustradeserver.auth.dto.*;
import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.auth.service.AuthService;
import com.campus.trade.campustradeserver.auth.service.EmailCodeService;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "认证管理", description = "邮箱验证码、注册、登录、退出和当前用户信息")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final EmailCodeService emailCodeService;
    private final AuthService authService;
    @Operation(summary = "发送注册验证码", description = "向邮箱发送六位数字验证码，有效期 5 分钟；同一邮箱 60 秒内不能重复申请。无需登录。")
    @PostMapping("/email-code")
    public ApiResponse<Void> sendEmailCode(
            @Valid @RequestBody SendEmailCodeRequest request
    ){
        emailCodeService.sendCode(request.getEmail());
        return new ApiResponse<>(0,"验证码已发送",null);
    }

    @Operation(summary = "注册普通用户", description = "使用邮箱验证码注册；用户名和邮箱不能重复，注册角色固定为 USER。成功后需调用登录接口获取 Token。无需登录。")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return new ApiResponse<>(0,"注册成功！",null);
    }

    @Operation(summary = "用户名密码登录", description = "使用用户名（不是邮箱）和密码登录，成功后返回 JWT Token、有效期和用户信息；被禁用的账号不能登录。")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        LoginResponse loginResponse= authService.login(request);
        return new ApiResponse<>(0,"登录成功",loginResponse);
    }

    @Operation(summary = "获取当前用户", description = "返回当前登录用户的公开信息，不包含密码。先在 Authorize 中填写登录返回的 Token，无需手动添加 Bearer 前缀。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        UserInfoResponse user = authService.getCurrentUser(currentUser.id());
        return new ApiResponse<>(0,"操作成功",user);
    }

    @Operation(summary = "退出当前登录", description = "将当前 Token 加入黑名单，使其在剩余有效期内不可再用；不是退出该账号的所有会话。需要在 Authorize 中填写 Token。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Parameter(hidden = true) @RequestHeader("Authorization") String authorization){
        String token = authorization.substring(7);
        authService.logout(token);
        return new ApiResponse<>(0,"退出登录",null);
    }
}
