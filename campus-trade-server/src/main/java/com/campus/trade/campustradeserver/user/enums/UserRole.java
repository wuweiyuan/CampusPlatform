package com.campus.trade.campustradeserver.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN");

    @EnumValue
    @JsonValue
    private final String code;

    UserRole(String code){
        this.code = code;
    }

    @JsonCreator
    public static UserRole fromCode(String code){
        for (UserRole role : values()){
            if (role.code.equals(code)){
                return role;
            }
        }
        throw new IllegalArgumentException("用户角色不合法");
    }
}
