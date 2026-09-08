package com.campus.trade.campustradeserver.auth.service;

import com.campus.trade.campustradeserver.common.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeService {
    private static final String CODE_KEY_PREFIX = "auth:email-code:";
    private static final String COOLDOWN_KEY_PREFIX ="auth:email-code:cooldown:";

    private final StringRedisTemplate stringRedisTemplate;

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    private void sendVerificationEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("校园交易平台注册验证码");
            helper.setText("""
                  <p>你的注册验证码是：</p>
                  <h2 style="letter-spacing: 4px">%s</h2>
                  <p>验证码 5 分钟内有效，请勿向任何人泄露。</p>
                  """.formatted(code), true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new BusinessException(500, "验证码邮件发送失败，请稍后重试");
        }
    }

    public void sendCode(String email){
        String cooldownKey = COOLDOWN_KEY_PREFIX +email;

        Boolean allowed = stringRedisTemplate.opsForValue().setIfAbsent(cooldownKey,"1", Duration.ofSeconds(60));

        if(!Boolean.TRUE.equals(allowed)){
            throw new BusinessException(400,"请在60秒后再试");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));

        String codeKey = CODE_KEY_PREFIX + email;
        try {
            stringRedisTemplate.opsForValue().set(codeKey, code, Duration.ofMinutes(5));
            sendVerificationEmail(email, code);
        } catch (RuntimeException e) {
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(cooldownKey);
            throw e;
        }

        // Local development: read the code from Redis; never include it in logs.
        log.info("邮箱验证码已生成并缓存");
    }

    public boolean verifyCode(String email, String code){
        String saveCode = stringRedisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);
        return code.equals(saveCode);
    }

    public void deleteCode(String email){
        stringRedisTemplate.delete(CODE_KEY_PREFIX + email);
    }
}
