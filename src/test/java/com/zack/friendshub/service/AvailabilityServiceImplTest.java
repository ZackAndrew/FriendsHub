package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.availability.AvailabilityResponseDto;
import com.zack.friendshub.mapper.AvailabilityMapper;
import com.zack.friendshub.model.Availability;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.AvailabilityRepo;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.impl.AvailabilityServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvailabilityServiceImplTest {

    @Mock
    private AvailabilityRepo availabilityRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private AvailabilityMapper availabilityMapper;
    @Mock
    private FriendshipRepo friendshipRepo;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    @Test
    public void saveAvailability_ShouldThrowIllegalStateException_WhenOverlapExists() {
        Long userId = 1L;
        UserPrincipal currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);
        AvailabilityRequestDto request = new AvailabilityRequestDto(startTime, endTime);

        when(availabilityRepo.existsOverlapping(userId, startTime, endTime)).thenReturn(true);
        IllegalStateException exception = assertThrows
                (IllegalStateException.class,
                        () -> availabilityService.saveAvailability(request, currentUser)
                );
        assertEquals("Availability overlap", exception.getMessage());

        verify(availabilityRepo, never()).save(any());
    }

    @Test
    public void saveAvailability_ShouldSaveAndReturnResponse_WhenNoOverlapExists() {
        Long userId = 1L;
        UserPrincipal currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);
        AvailabilityRequestDto request = new AvailabilityRequestDto(startTime, endTime);

        when(availabilityRepo.existsOverlapping(userId, startTime, endTime)).thenReturn(false);

        User mockUser = new User();
        mockUser.setId(userId);
        when(userRepo.getReferenceById(userId)).thenReturn(mockUser);

        Availability savedAvailability = Availability.builder()
                .id(100L)
                .startTime(startTime)
                .endTime(endTime)
                .user(mockUser)
                .build();

        when(availabilityRepo.save(any(Availability.class))).thenReturn(savedAvailability);

        AvailabilityResponseDto expected = new AvailabilityResponseDto(100L, userId, startTime, endTime);
        when(availabilityMapper.toResponse(savedAvailability)).thenReturn(expected);

        AvailabilityResponseDto actual = availabilityService.saveAvailability(request, currentUser);

        assertNotNull(actual);
        assertEquals(expected, actual);

        verify(availabilityRepo).existsOverlapping(userId, startTime, endTime);
        verify(userRepo).getReferenceById(userId);
        verify(availabilityRepo).save(any(Availability.class));
        verify(availabilityMapper).toResponse(savedAvailability);
    }

    @Test
    public void getUserAvailability_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        Long userId = 1L;
        UserPrincipal currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        String friendUsername = "TestFriend";

        LocalDateTime from = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, Month.JANUARY, 14, 15, 0, 0);

        when(userRepo.findByUsername(friendUsername)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> availabilityService.getUserAvailability(from, to, friendUsername, currentUser));

        assertEquals("User not found: " + friendUsername, exception.getMessage());

        verify(availabilityRepo, never()).findAllByUserIdAndDateRange(any(), any(), any());
    }

    @Test
    public void getUserAvailability_ShouldThrowAccessDeniedException_WhenFriendshipNotFound() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        String friendUsername = "TestFriend";

        LocalDateTime from = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, Month.JANUARY, 14, 15, 0, 0);

        UserPrincipal currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(currentUserId);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setUsername(friendUsername);
        when(userRepo.findByUsername(friendUsername)).thenReturn(Optional.of(targetUser));

        when(friendshipRepo.existsBetweenUsers(targetUserId, currentUserId)).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> availabilityService.getUserAvailability(from, to, friendUsername, currentUser));

        assertEquals("You are not friends", exception.getMessage());
        verify(availabilityRepo, never()).findAllByUserIdAndDateRange(any(), any(), any());
    }

    @Test
    public void getUserAvailability_ShouldReturnList_WhenViewingFriendAvailabilityAndTheyAreFriends() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        String friendUsername = "TestFriend";

        LocalDateTime from = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, Month.JANUARY, 14, 15, 0, 0);

        UserPrincipal currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(currentUserId);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setUsername(friendUsername);
        when(userRepo.findByUsername(friendUsername)).thenReturn(Optional.of(targetUser));

        when(friendshipRepo.existsBetweenUsers(2L, 1L)).thenReturn(true);

        Availability availability = Availability.builder()
                .id(100L)
                .user(targetUser)
                .startTime(from)
                .endTime(to)
                .build();

        when(availabilityRepo.findAllByUserIdAndDateRange(targetUserId, from, to))
                .thenReturn(List.of(availability));
        AvailabilityResponseDto expected = new AvailabilityResponseDto(100L, targetUserId, from, to);
        when(availabilityMapper.toResponse(availability)).thenReturn(expected);

        List<AvailabilityResponseDto> actual = availabilityService.getUserAvailability(from, to, friendUsername, currentUser);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));

        verify(friendshipRepo).existsBetweenUsers(targetUserId, currentUserId);
        verify(availabilityRepo).findAllByUserIdAndDateRange(targetUserId, from, to);
    }

    @Test
    public void getUserAvailability_ShouldReturnList_WhenViewingOwnAvailability() {
        Long userId = 1L;
        String myUsername = "zack";

        LocalDateTime from = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, Month.JANUARY, 14, 15, 0, 0);

        UserPrincipal currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        User myUser = new User();
        myUser.setId(userId);
        myUser.setUsername(myUsername);
        when(userRepo.findByUsername(myUsername)).thenReturn(Optional.of(myUser));

        Availability availability = Availability.builder()
                .id(100L)
                .user(myUser)
                .startTime(from)
                .endTime(to)
                .build();

        when(availabilityRepo.findAllByUserIdAndDateRange(userId, from, to))
                .thenReturn(List.of(availability));

        AvailabilityResponseDto expected = new AvailabilityResponseDto(100L, userId, from, to);
        when(availabilityMapper.toResponse(availability)).thenReturn(expected);

        List<AvailabilityResponseDto> actual = availabilityService.getUserAvailability(from, to, myUsername, currentUser);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));

        verify(friendshipRepo, never()).existsBetweenUsers(any(), any());
        verify(availabilityRepo).findAllByUserIdAndDateRange(userId, from, to);
    }
}
