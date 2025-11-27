package com.zack.friendshub.dto.request;

import com.zack.friendshub.enums.UserStatus;

public record UpdateUserStatusRequestDto(
        UserStatus status
) {
}
