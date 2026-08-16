package com.campus.trade.campustradeserver.auth.controller;

import com.campus.trade.campustradeserver.auth.dto.*;
import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.auth.service.AuthService;
import com.campus.trade.campustradeserver.auth.service.EmailCodeService;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final EmailCodeService emailCodeService;
    private final AuthService authService;
    @PostMapping("/email-code")
    public ApiResponse<Void> sendEmailCode(
            @Valid @RequestBody SendEmailCodeRequest request
    ){
        emailCodeService.sendCode(request.getEmail());
        return new ApiResponse<>(0,"验证码已发送",null);
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return new ApiResponse<>(0,"注册成功！",null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        LoginResponse loginResponse= authService.login(request);
        return new ApiResponse<>(0,"登录成功",loginResponse);
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        UserInfoResponse user = authService.getCurrentUser(currentUser.id());
        return new ApiResponse<>(0,"操作成功",user);
    }
}
