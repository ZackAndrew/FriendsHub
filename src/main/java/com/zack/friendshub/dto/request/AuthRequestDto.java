package com.zack.friendshub.dto.request;

public record AuthRequestDto(
        String username,
        String name,
        String email,
        String password
) {
}
