package com.zack.friendshub.controller;

import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.mapper.UserMapper;
import com.zack.friendshub.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
public class UserController {

    private final UserServiceImpl userService;
    private final UserMapper userMapper;

    public UserController(UserServiceImpl userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
      return userService.getUserById(userId)
              .map(user -> ResponseEntity.ok(userMapper.toResponseDto(user)))
              .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(userMapper.toResponseDto(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
