package com.zack.friendshub.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDto(
        @NotBlank
        String email
) {
}
