package com.campus.trade.campustradeserver.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    private final StringRedisTemplate stringRedisTemplate;
    public void addToBlacklist(String jti, long remainingSeconds){
        if(remainingSeconds <= 0){
            return;
        }
        stringRedisTemplate.opsForValue().set(buildKey(jti),"1", Duration.ofSeconds(remainingSeconds));

    }

    public boolean isBlacklisted(String jti){
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(jti)));
    }

    private String buildKey(String jti){
        return TOKEN_BLACKLIST_PREFIX + jti;
    }
}
