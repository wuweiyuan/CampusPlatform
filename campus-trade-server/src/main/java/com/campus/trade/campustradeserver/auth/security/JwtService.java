package com.campus.trade.campustradeserver.auth.security;

import com.campus.trade.campustradeserver.user.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;


@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expireSeconds;
    public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expire-seconds}") long expireSeconds){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expireSeconds =expireSeconds;
    }

    public String generateToken(SysUser user){
        Instant now = Instant.now();

        return Jwts.builder().subject(user.getUsername())
                .claim("userId",user.getId())
                .claim("role",user.getRole().getCode())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(signingKey,Jwts.SIG.HS256)
                .compact();
    }
    public Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token){
        try {
            parseClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException exception){
            return false;
        }
    }

    public long getExpireSeconds(){
        return expireSeconds;
    }

}
