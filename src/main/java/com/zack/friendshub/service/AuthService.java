package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.AuthRequestDto;
import com.zack.friendshub.dto.request.LoginRequestDto;
import com.zack.friendshub.dto.request.PasswordResetConfirmDto;
import com.zack.friendshub.dto.request.PasswordResetRequestDto;
import com.zack.friendshub.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto register(AuthRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    void requestPasswordReset(PasswordResetRequestDto request);

    void confirmPasswordReset(PasswordResetConfirmDto request);
}
