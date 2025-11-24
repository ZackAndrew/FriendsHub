package com.zack.friendshub.dto.response;

import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String username,
        String name,
        String email,
        Role role,
        UserStatus status,
        LocalDateTime dateOfRegistration
) {
}
