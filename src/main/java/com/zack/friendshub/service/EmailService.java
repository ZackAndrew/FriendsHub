package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.UserResponseDto;
import org.springframework.http.ResponseEntity;

public interface EmailService {
    public ResponseEntity<UserResponseDto> sendVerificationEmail(String toEmail, String token);
}
