package com.zack.friendshub.controller;

import com.zack.friendshub.bot.FriendsHubBot;
import com.zack.friendshub.dto.request.AuthRequestDto;
import com.zack.friendshub.dto.request.LoginRequestDto;
import com.zack.friendshub.dto.request.PasswordResetConfirmDto;
import com.zack.friendshub.dto.request.PasswordResetRequestDto;
import com.zack.friendshub.dto.response.AuthResponseDto;
import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.VerificationToken;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.AuthService;
import com.zack.friendshub.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
        try {
            User createdUser = userRepo.findByUsername(response.user().username())
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));

            verificationService.createVerificationToken(createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Failed to send verification email (non-critical): {}", e.getMessage());
        }
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

        friendsHubBot.sendMessageToUser(response.telegramChatID(),
                "Welcome! 🎉 Your email has been successfully verified, and your Telegram account is now linked to your profile." + response.username() + ".");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Request password reset",
            description = "Initiates the password reset process by generating a token and sending an email. " +
                    "Always returns 200 OK to prevent email enumeration attacks."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request processed (email sent if the user exists)"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid email format)")
    })
    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {

        log.info("Received password reset request for email: {}", request.email());

        authService.requestPasswordReset(request);

        log.debug("Password reset process completed for email: {}", request.email());

        return ResponseEntity.ok("If the provided email is registered, a password reset link has been sent to it.");
    }

    @Operation(
            summary = "Confirm password reset",
            description = "Sets a new password using the token sent to the user's email. Token and new password must be provided in the request body."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully reset"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., missing token or weak password)"),
            @ApiResponse(responseCode = "404", description = "Token not found or expired") // Залежить від того, який Exception ви кидаєте в сервісі
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<String> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDto request) {
        log.info("Received request to confirm password reset");

        authService.confirmPasswordReset(request);

        log.info("Password successfully reset");
        return ResponseEntity.ok("Password has been successfully reset");
    }
}
