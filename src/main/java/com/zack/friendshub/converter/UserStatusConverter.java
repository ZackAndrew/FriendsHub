package com.zack.friendshub.converter;


import com.zack.friendshub.enums.UserStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserStatusConverter  implements Converter<String, UserStatus> {

    @Override
    public UserStatus convert(String source) {
        return UserStatus.valueOf(source.trim().toUpperCase());
    }
}
