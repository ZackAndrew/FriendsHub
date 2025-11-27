package com.zack.friendshub.dto.response;

import com.zack.friendshub.enums.UserStatus;

public record RegisterResponseDto(
        String username,
        String name,
        String email,
        UserStatus status
) {
}
