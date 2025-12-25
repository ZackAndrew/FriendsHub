package com.zack.friendshub.controller;

import com.zack.friendshub.constant.HttpStatuses;
import com.zack.friendshub.dto.request.UpdateUserStatusRequestDto;
import com.zack.friendshub.dto.response.PageableDto;
import com.zack.friendshub.dto.response.PageableUserResponseDto;
import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.mapper.PageableMapper;
import com.zack.friendshub.mapper.UserMapper;
import com.zack.friendshub.model.User;
import com.zack.friendshub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Users", description = "Operations related to users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Get user by ID", description = "Returns user details by their unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        log.info("user.fetch id={}", userId);

        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }

    @Operation(summary = "Get user by username", description = "Returns user details by their unique username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
    })
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        log.info("user.fetch-by-username name={}", username);

        User user = userService.getUserByUsername(username);

        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }

    @Operation(summary = "Get all users", description = "Get all users details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
                    content = @Content(schema = @Schema(implementation = PageableUserResponseDto.class))),
            @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @GetMapping
    public ResponseEntity<PageableDto<UserResponseDto>> getAllUsers(@ParameterObject @ApiIgnore Pageable pageable) {
        log.info("user.fetch-all");
        PageableDto<User> usersPage = userService.findByPage(pageable);
        PageableDto<UserResponseDto> dtoPage = PageableMapper.map(
                usersPage,
                userMapper::toResponseDto
        );
        return ResponseEntity.ok(dtoPage);
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
