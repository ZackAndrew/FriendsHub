package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.friendship.FriendshipRequestResponseDto;
import com.zack.friendshub.enums.FriendshipStatus;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.exception.FriendshipRequestAlreadyExistsException;
import com.zack.friendshub.exception.SelfFriendshipRequestException;
import com.zack.friendshub.mapper.FriendshipMapper;
import com.zack.friendshub.model.Friendship;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.impl.FriendshipServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    @Mock
    UserRepo userRepo;

    @Mock
    FriendshipRepo friendshipRepo;

    @Mock
    FriendshipMapper friendshipMapper;

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    @Test
    public void shouldThrowException_WhenUserSendRequestToHimself() {
        Long sameId = 1L;
        User user = User.builder().id(sameId).role(Role.USER).build();
        UserPrincipal currentUser = new UserPrincipal(user);

        assertThrows(SelfFriendshipRequestException.class,
                () -> friendshipService.sendFriendshipRequest(sameId, currentUser));
    }

    @Test
    public void shouldThrowException_WhenRequesterNotFound() {
        Long userId = 1L;
        User user = User.builder().id(userId).role(Role.USER).build();
        UserPrincipal currentUser = new UserPrincipal(user);

        when(userRepo.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> friendshipService.sendFriendshipRequest(2L, currentUser));
    }

    @Test
    public void shouldThrowException_WhenAddresseeNotFound() {
        Long requesterId = 1L;
        Long addresseeId = 2L;
        User user = User.builder().id(requesterId).role(Role.USER).build();
        UserPrincipal currentUser = new UserPrincipal(user);
        when(userRepo.findById(requesterId)).thenReturn(Optional.of(user));
        when(userRepo.findById(addresseeId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> friendshipService.sendFriendshipRequest(2L, currentUser));
    }

    @ParameterizedTest
    @EnumSource(value = FriendshipStatus.class, names = {"PENDING", "ACCEPTED"})
    public void shouldThrowException_WhenFriendshipRequestActive(FriendshipStatus status) {
        User requester = User.builder().id(1L).role(Role.USER).build();
        User addressee = User.builder().id(2L).role(Role.USER).build();
        UserPrincipal currentUser = new UserPrincipal(requester);

        Friendship friendship = Friendship.builder()
                .id(100L)
                .requester(requester)
                .addressee(addressee)
                .status(status)
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepo.findById(2L)).thenReturn(Optional.of(addressee));

        when(friendshipRepo.findFriendshipBetween(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(FriendshipRequestAlreadyExistsException.class,
                () -> friendshipService.sendFriendshipRequest(2L, currentUser));
    }

    @Test
    public void shouldSendFriendshipRequest_WhenAddresseePreviouslyDeclined() {
        User requester = User.builder().id(1L).role(Role.USER).build();
        User addressee = User.builder().id(2L).role(Role.USER).build();
        UserPrincipal currentUser = new UserPrincipal(requester);

        Friendship previouslyDeclinedFriendship = Friendship.builder()
                .id(100L)
                .requester(addressee)
                .addressee(requester)
                .status(FriendshipStatus.DECLINED)
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepo.findById(2L)).thenReturn(Optional.of(addressee));

        when(friendshipRepo.findFriendshipBetween(1L, 2L)).thenReturn(Optional.of(previouslyDeclinedFriendship));

        when(friendshipRepo.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FriendshipRequestResponseDto expectedResponse = new FriendshipRequestResponseDto(
                100L,
                requester.getId(),
                requester.getUsername(),
                addressee.getUsername(),
                addressee.getId(),
                addressee.getUsername(),
                addressee.getName(),
                FriendshipStatus.PENDING,
                LocalDateTime.now()
        );
        when(friendshipMapper.toResponse(any(Friendship.class))).thenReturn(expectedResponse);

        FriendshipRequestResponseDto actualResponse = friendshipService.sendFriendshipRequest(2L, currentUser);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(FriendshipStatus.PENDING, previouslyDeclinedFriendship.getStatus());
    }
}