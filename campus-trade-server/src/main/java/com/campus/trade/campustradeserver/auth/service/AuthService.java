package com.campus.trade.campustradeserver.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.trade.campustradeserver.auth.dto.LoginRequest;
import com.campus.trade.campustradeserver.auth.dto.LoginResponse;
import com.campus.trade.campustradeserver.auth.dto.RegisterRequest;
import com.campus.trade.campustradeserver.auth.dto.UserInfoResponse;
import com.campus.trade.campustradeserver.auth.security.JwtService;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.user.entity.SysUser;
import com.campus.trade.campustradeserver.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailCodeService emailCodeService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest registerRequest) {
        boolean codeValid = emailCodeService.verifyCode(registerRequest.getEmail(), registerRequest.getEmailCode());
        if (!codeValid) {
            throw new BusinessException(1001, "验证码无效或过期");
        }
        Long count = sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, registerRequest.getUsername())
                .or()
                .eq(SysUser::getEmail, registerRequest.getEmail())
        );
        if (count > 0) {
            throw new BusinessException(1002, "用户名或邮箱已存在");
        }
        SysUser sysUser = new SysUser();
        sysUser.setUsername(registerRequest.getUsername());
        sysUser.setEmail(registerRequest.getEmail());
        sysUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        sysUser.setRole("USER");
        sysUser.setStatus(1);
        sysUser.setEmailVerified(true);

        sysUserMapper.insert(sysUser);
        emailCodeService.deleteCode(registerRequest.getEmail());

    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(1004, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(1003, "账号已禁用");
        }

        String token = jwtService.generateToken(user);

        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setId(user.getId());
        userInfoResponse.setUsername(user.getUsername());
        userInfoResponse.setEmail(user.getEmail());
        userInfoResponse.setRole(user.getRole());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn(jwtService.getExpireSeconds());
        loginResponse.setUser(userInfoResponse);

        return loginResponse;
    }

    public UserInfoResponse getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在或登录已失效");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(1003, "账号已禁用");
        }
        UserInfoResponse response = new UserInfoResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setId(user.getId());
        return response;
    }
}
