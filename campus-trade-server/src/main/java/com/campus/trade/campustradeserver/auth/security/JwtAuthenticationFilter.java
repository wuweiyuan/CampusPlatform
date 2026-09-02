package com.campus.trade.campustradeserver.auth.security;

import com.campus.trade.campustradeserver.user.entity.SysUser;
import com.campus.trade.campustradeserver.user.enums.UserStatus;
import com.campus.trade.campustradeserver.user.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SysUserMapper sysUserMapper;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtService.parseClaims(token);

            String jti = claims.getId();
            if(jti == null || tokenBlacklistService.isBlacklisted(jti)){
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request,response);
                return;
            }

            Number userId = claims.get("userId", Number.class);

            if(userId == null){
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            SysUser user = sysUserMapper.selectById(userId.longValue());
            if(user == null || user.getStatus() != UserStatus.ENABLED || user.getRole() == null){
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            AuthenticatedUser principal = new AuthenticatedUser(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().getCode()
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" +  user.getRole().getCode()
                                    )
                            )
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}