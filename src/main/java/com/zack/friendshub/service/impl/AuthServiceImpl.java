package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.AuthRequestDto;
import com.zack.friendshub.dto.request.LoginRequestDto;
import com.zack.friendshub.dto.response.AuthResponseDto;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.exception.BadRequestException;
import com.zack.friendshub.mapper.UserMapper;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.JwtUtil;
import com.zack.friendshub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepo userRepo;

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
}
