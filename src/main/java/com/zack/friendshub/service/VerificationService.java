package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.VerificationToken;

public interface VerificationService {
    void createVerificationToken(User user);

    VerificationToken createTelegramVerificationToken(User user, Long telegramChatId);

    UserResponseDto verifyEmail(String tokenCode);

    UserResponseDto verifyTelegramEmail(String tokenCode);
}
