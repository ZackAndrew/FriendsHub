package com.zack.friendshub.dto.response;

public record AuthResponseDto(
        String token,
        UserResponseDto user
) {
}
