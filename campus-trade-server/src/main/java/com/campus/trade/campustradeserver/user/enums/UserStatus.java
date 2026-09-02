package com.campus.trade.campustradeserver.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    ENABLED(1),
    DISABLED(0);

    @EnumValue
    @JsonValue
    private final Integer code;
    UserStatus(Integer code){
        this.code = code;
    }

    @JsonCreator
    public static UserStatus fromCode(Integer code){
            for (UserStatus status : values()){
                if (status.code.equals(code)){
                    return status;
                }
            }
            throw new IllegalArgumentException("用户状态只能是0或1");
    }

}
