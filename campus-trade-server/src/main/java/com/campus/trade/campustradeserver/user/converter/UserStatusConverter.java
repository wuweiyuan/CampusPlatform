package com.campus.trade.campustradeserver.user.converter;

import com.campus.trade.campustradeserver.user.enums.UserStatus;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserStatusConverter implements Converter<String, UserStatus> {

    @Override
    public UserStatus convert(String source) {
        try{
            return UserStatus.fromCode(Integer.valueOf(source));
        }catch (NumberFormatException exception){
            throw new IllegalArgumentException("用户状态只能是0或1");
        }
    }
}
