package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.exception.BadRequestException;
import com.zack.friendshub.exception.InvalidTokenException;
import com.zack.friendshub.exception.TokenExpiredException;
import com.zack.friendshub.mapper.UserMapper;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.VerificationToken;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.repository.VerificationTokenRepo;
import com.zack.friendshub.service.EmailService;
import com.zack.friendshub.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationTokenRepo verificationTokenRepo;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Override
    public void createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .expiryDate(expiryDate)
                .user(user)
                .build();

        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
        verificationTokenRepo.save(verificationToken);
    }

    @Override
    public VerificationToken createTelegramVerificationToken(User user, Long telegramChatId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .expiryDate(expiryDate)
                .user(user)
                .telegramChatId(telegramChatId)
                .build();

        return verificationTokenRepo.save(verificationToken);
    }


    @Override
    public UserResponseDto verifyEmail(String tokenCode) {
        VerificationToken token = verificationTokenRepo.findByToken(tokenCode)
                .orElseThrow(() -> new InvalidTokenException("Token is invalid"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new TokenExpiredException("Token is expired");

        User user = token.getUser();
        user.setStatus(UserStatus.ACTIVATED);
        userRepo.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto verifyTelegramEmail(String tokenCode) {
        VerificationToken token = verificationTokenRepo.findByToken(tokenCode)
                .orElseThrow(() -> new InvalidTokenException("Token is invalid"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new TokenExpiredException("Token is expired");

        User user = token.getUser();
        user.setTelegramChatId(token.getTelegramChatId());
        userRepo.save(user);
        return userMapper.toResponseDto(user);
    }
}
