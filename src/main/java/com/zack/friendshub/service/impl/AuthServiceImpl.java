package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.AuthRequestDto;
import com.zack.friendshub.dto.request.LoginRequestDto;
import com.zack.friendshub.dto.request.PasswordResetConfirmDto;
import com.zack.friendshub.dto.request.PasswordResetRequestDto;
import com.zack.friendshub.dto.response.AuthResponseDto;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.exception.BadRequestException;
import com.zack.friendshub.exception.InvalidTokenException;
import com.zack.friendshub.mapper.UserMapper;
import com.zack.friendshub.model.PasswordResetToken;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.PasswordResetTokenRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.JwtUtil;
import com.zack.friendshub.service.AuthService;
import com.zack.friendshub.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepo userRepo;
    private final EmailService emailService;
    private final PasswordResetTokenRepo passwordResetTokenRepo;

    @Override
    public AuthResponseDto register(AuthRequestDto request) {
        if (userRepo.existsByUsername(request.username())) {
            throw new BadRequestException("Username '" + request.username() + "' is already taken");
        }

        if (userRepo.existsByEmail(request.email())) {
            throw new BadRequestException("Email '" + request.email() + "' is already taken");
        }

        User user = User.builder()
                .username(request.username())
                .name(request.name())
                .email(request.email())
                .role(Role.USER)
                .status(UserStatus.CREATED)
                .dateOfRegistration(LocalDateTime.now())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepo.save(user);

        String token = jwtUtil.generateToken(savedUser.getUsername());

        return new AuthResponseDto(token, userMapper.toResponseDto(savedUser));
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepo.findByUsername(request.login())
                .or(() -> userRepo.findByEmail(request.login()))
                .orElseThrow(() -> new BadRequestException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid username/email or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponseDto(token, userMapper.toResponseDto(user));
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDto request) {
        Optional<User> userOpt = userRepo.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryTime(expiryTime)
                .build();

        passwordResetTokenRepo.save(passwordResetToken);

        emailService.sendPasswordResetTokenEmail(request.email(), token);
    }

    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmDto request) {
        PasswordResetToken token = passwordResetTokenRepo.findByToken(request.token())
                .orElseThrow(()-> new InvalidTokenException("Invalid or missing token"));

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepo.delete(token);
            throw new IllegalArgumentException("Token has expired");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);

        passwordResetTokenRepo.delete(token);
    }
}
