package com.campus.trade.campustradeserver.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录成功返回的数据")
public class LoginResponse {
    @Schema(description = "JWT 访问令牌；用于 Authorization: Bearer <token>，请勿公开分享")
    private String token;
    @Schema(description = "令牌类型，固定为 Bearer", example = "Bearer")
    private String tokenType;
    @Schema(description = "Token 有效时长，单位为秒；实际值由后端配置决定", example = "7200")
    private long expiresIn;
    @Schema(description = "当前登录用户的公开信息")
    private UserInfoResponse user;

}
