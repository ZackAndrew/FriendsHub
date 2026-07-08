package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.VerificationToken;

public interface VerificationService {
    VerificationToken createVerificationToken(User user);

    UserResponseDto verifyEmail(String tokenCode);
}
