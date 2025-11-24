package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getDateOfRegistration()
        );
    }
}
