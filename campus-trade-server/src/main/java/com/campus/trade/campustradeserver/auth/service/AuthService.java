package com.campus.trade.campustradeserver.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.trade.campustradeserver.auth.dto.RegisterRequest;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.user.entity.SysUser;
import com.campus.trade.campustradeserver.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailCodeService emailCodeService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register (RegisterRequest registerRequest){
        boolean codeValid = emailCodeService.verifyCode(registerRequest.getEmail(),registerRequest.getEmailCode());
        if(!codeValid){
            throw new BusinessException(1001,"验证码无效或过期");
        }
        Long count = sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername,registerRequest.getUsername())
                .or()
                .eq(SysUser::getEmail,registerRequest.getEmail())
        );
        if(count > 0){
                throw new BusinessException(1002,"用户名或邮箱已存在");
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
}
