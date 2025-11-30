package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.UpdateUserStatusRequestDto;
import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.mapper.UserMapper;
import com.zack.friendshub.model.User;
import com.zack.friendshub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;


    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        log.info("user.fetch id={}", userId);

        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        log.info("user.fetch-by-username name={}", username);

        User user = userService.getUserByUsername(username);

        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("user.fetch-all");

        List<UserResponseDto> users = userService.getAllUsers()
                .stream()
                .map(userMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserStatusRequestDto request) {
        log.info("user.update-status id={}, newStatus={}", userId, request.status());

        User updated = userService.updateUserStatus(userId, request.status());

        return ResponseEntity.ok(userMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long userId) {
        log.info("user.delete id={}", userId);

        userService.deleteUserById(userId);

        return ResponseEntity.noContent().build();
    }

}
