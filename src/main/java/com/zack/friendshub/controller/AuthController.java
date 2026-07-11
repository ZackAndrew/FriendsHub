package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.AuthRequestDto;
import com.zack.friendshub.dto.request.LoginRequestDto;
import com.zack.friendshub.dto.response.AuthResponseDto;
import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.model.User;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid AuthRequestDto request) {
        log.info("auth.register username={}, email={}", request.username(), request.email());

        AuthResponseDto result = authService.register(request);

        // Send verification email (non-blocking, best-effort)
        try {
            User createdUser = userRepo.findByUsername(result.user().username())
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));
            verificationService.createVerificationToken(createdUser);
        } catch (Exception e) {
            log.warn("Failed to send verification email (non-critical): {}", e.getMessage());
        }

        return ResponseEntity.ok(result);
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
}
