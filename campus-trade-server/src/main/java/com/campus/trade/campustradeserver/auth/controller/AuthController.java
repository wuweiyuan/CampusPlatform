package com.campus.trade.campustradeserver.auth.controller;

import com.campus.trade.campustradeserver.auth.dto.SendEmailCodeRequest;
import com.campus.trade.campustradeserver.auth.service.EmailCodeService;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final EmailCodeService emailCodeService;
    @PostMapping("/email-code")
    public ApiResponse<Void> sendEmailCode(
            @Valid @RequestBody SendEmailCodeRequest request
    ){
        emailCodeService.sendCode(request.getEmail());
        return new ApiResponse<>(0,"验证码已发送",null);
    }
}
