package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.AuthRequestDto;
import com.zack.friendshub.dto.response.AuthResponseDto;
import com.zack.friendshub.dto.response.UserResponseDto;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.exception.BadRequestException;
import com.zack.friendshub.mapper.UserMapper;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.JwtUtil;
import com.zack.friendshub.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void register_ShouldThrowBadRequestException_WhenUsernameIsAlreadyTaken() {
        String username = "TestUser";
        AuthRequestDto request = new AuthRequestDto(username, "Andrew", "test@gmail.com", "Test_123");
        when(userRepo.existsByUsername(username)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));

        verify(userRepo, never()).save(any());
    }

    @Test
    public void register_ShouldThrowBadRequestException_WhenEmailIsAlreadyTaken() {
        String email = "test@gmail.com";
        AuthRequestDto request = new AuthRequestDto("TestUser", "Andrew", email, "Test_123");
        when(userRepo.existsByEmail(email)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));

        verify(userRepo, never()).save(any());
    }

    @Test
    public void register_ShouldSuccessfullyRegisterUser_WhenRequestIsValid() {
        AuthRequestDto request = new AuthRequestDto("TestUser", "Andrew", "test@gmail.com", "Test_123");
        when(userRepo.existsByUsername(request.username())).thenReturn(false);
        when(userRepo.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");


        User savedUser = User.builder()
                .id(1L)
                .username("TestUser")
                .name("Andrew")
                .email("test@gmail.com")
                .build();

        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser.getUsername())).thenReturn("jwt_token");
        UserResponseDto responseDto = new UserResponseDto(
                1L,
                "TestUser",
                "Andrew",
                "test@gmail.com",
                Role.USER,
                UserStatus.CREATED,
                LocalDateTime.now(),
                null
        );
        when(userMapper.toResponseDto(savedUser)).thenReturn(responseDto);
        AuthResponseDto actual = authService.register(request);

        assertNotNull(actual);
        assertEquals("jwt_token", actual.token());
        assertEquals(responseDto, actual.user());


        verify(userRepo, times(1)).save(any(User.class));
    }
}
