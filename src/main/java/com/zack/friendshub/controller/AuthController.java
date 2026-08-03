package com.zack.friendshub.controller;

import com.zack.friendshub.bot.FriendsHubBot;
import com.zack.friendshub.dto.request.AuthRequestDto;
import com.zack.friendshub.dto.request.LoginRequestDto;
import com.zack.friendshub.dto.response.AuthResponseDto;
import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.VerificationToken;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.AuthService;
import com.zack.friendshub.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;
    private final UserRepo userRepo;
    private final FriendsHubBot friendsHubBot;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> resister(@RequestBody @Valid AuthRequestDto request) {
        log.info("auth.register username={}, email={}", request.username(), request.email());

        AuthResponseDto response = authService.register(request);
        User createdUser = userRepo.findByUsername(response.user().username())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        verificationService.createVerificationToken(createdUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        log.info("auth.login login={}", request.login());

        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<UserResponseDto> verifyEmail(@PathVariable("token") String token) {

        return ResponseEntity.ok(verificationService.verifyEmail(token));
    }

    @GetMapping("/verify-telegram/{token}")
    public ResponseEntity<UserResponseDto> verifyTelegramEmail(@PathVariable("token") String token) {
        UserResponseDto response = verificationService.verifyTelegramEmail(token);

        friendsHubBot.sendTextMessage(response.telegramChatID(),
                "Вітаємо! 🎉 Твій email успішно підтверджено, а Telegram-акаунт прив'язано до профілю " + response.username() + ".");
        return ResponseEntity.ok(response);
    }
}
