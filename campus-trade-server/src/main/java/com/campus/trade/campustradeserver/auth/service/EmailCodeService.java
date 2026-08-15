package com.campus.trade.campustradeserver.auth.service;

import com.campus.trade.campustradeserver.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeService {
    private static final String CODE_KEY_PREFIX = "auth:email-code:";
    private static final String COOLDOWN_KEY_PREFIX ="auth:email-code:cooldown:";

    private final StringRedisTemplate stringRedisTemplate;

    public void sendCode(String email){
        String cooldownKey = COOLDOWN_KEY_PREFIX +email;

        Boolean allowed = stringRedisTemplate.opsForValue().setIfAbsent(cooldownKey,"1", Duration.ofSeconds(60));

        if(!Boolean.TRUE.equals(allowed)){
            throw new BusinessException(400,"请在60秒后再试");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));

        stringRedisTemplate.opsForValue().set(CODE_KEY_PREFIX + email,code,Duration.ofMinutes(5));
        // 当前 MAIL_MODE=log：暂时不发送真实邮件，直接从后端日志取得验证码。
        log.info("发送邮箱验证码：email={}, code={}", email, code);
    }

    public boolean verifyCode(String email, String code){
        String saveCode = stringRedisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);
        return code.equals(saveCode);
    }

    public void deleteCode(String email){
        stringRedisTemplate.delete(CODE_KEY_PREFIX + email);
    }
}
