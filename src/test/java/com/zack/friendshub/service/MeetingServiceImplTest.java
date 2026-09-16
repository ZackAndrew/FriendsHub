package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.enums.MeetingStatus;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.exception.SelfMeetingRequestException;
import com.zack.friendshub.mapper.MeetingMapper;
import com.zack.friendshub.model.Meeting;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.AvailabilityRepo;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.MeetingRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.impl.MeetingServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceImplTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private FriendshipRepo friendshipRepo;
    @Mock
    private AvailabilityRepo availabilityRepo;
    @Mock
    private MeetingRepo meetingRepo;
    @Mock
    private MeetingMapper meetingMapper;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    @Test
    public void sendMeetingRequest_ThrowsSelfMeetingRequestException_WhenSendingToSelf() {
        String userUsername = "test";

        User user = new User();
        user.setUsername(userUsername);
        user.setRole(Role.USER);

        UserPrincipal userPrincipal = new UserPrincipal(user);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        MeetingRequestDto dto = new MeetingRequestDto(userUsername, startTime, endTime, "Test title");

        SelfMeetingRequestException exception = assertThrows
                (SelfMeetingRequestException.class,
                        () -> meetingService.sendMeetingRequest(dto, userPrincipal)
                );

        assertEquals("User cannot send meeting request to self", exception.getMessage());

        verify(meetingRepo, never()).save(any());
    }

    @Test
    public void sendMeetingRequest_ThrowsEntityNotFoundException_WhenRequesterNotFound() {
        Long requesterId = 1L;
        String requesterUsername = "requester_user";
        String participantUsername = "other_user";

        User user = new User();
        user.setId(requesterId);
        user.setUsername(requesterUsername);
        user.setRole(Role.USER);

        UserPrincipal userPrincipal = new UserPrincipal(user);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        MeetingRequestDto dto = new MeetingRequestDto(participantUsername, startTime, endTime, "Test title");

        when(userRepo.findById(requesterId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.sendMeetingRequest(dto, userPrincipal)
        );

        assertEquals("Requester not found", exception.getMessage());

        verify(userRepo, times(1)).findById(requesterId);
        verifyNoInteractions(friendshipRepo, availabilityRepo, meetingRepo, meetingMapper);
    }

    @Test
    public void sendMeetingRequest_ThrowsEntityNotFoundException_WhenAddresseeNotFound() {
        Long requesterId = 1L;
        String requesterUsername = "requester_user";
        String participantUsername = "other_user";

        User user = new User();
        user.setId(requesterId);
        user.setUsername(requesterUsername);
        user.setRole(Role.USER);

        UserPrincipal userPrincipal = new UserPrincipal(user);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        MeetingRequestDto dto = new MeetingRequestDto(participantUsername, startTime, endTime, "Test title");

        when(userRepo.findById(requesterId)).thenReturn(Optional.of(user));
        when(userRepo.findByUsername(participantUsername)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.sendMeetingRequest(dto, userPrincipal)
        );

        assertEquals("Addressee not found", exception.getMessage());

        verify(userRepo, times(1)).findById(requesterId);
        verify(userRepo, times(1)).findByUsername(participantUsername);
        verifyNoInteractions(friendshipRepo, availabilityRepo, meetingRepo, meetingMapper);
    }

    @Test
    public void sendMeetingRequest_ThrowsIllegalStateException_WhenUsersAreNotFriends() {

        Long requesterId = 1L;
        Long addresseeId = 2L;
        String requesterUsername = "requester_user";
        String participantUsername = "other_user";

        User requester = new User();
        requester.setId(requesterId);
        requester.setUsername(requesterUsername);
        requester.setRole(Role.USER);

        User addressee = new User();
        addressee.setId(addresseeId);
        addressee.setUsername(participantUsername);
        addressee.setRole(Role.USER);


        UserPrincipal userPrincipal = new UserPrincipal(requester);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        MeetingRequestDto dto = new MeetingRequestDto(participantUsername, startTime, endTime, "Test title");
        when(userRepo.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepo.findByUsername(participantUsername)).thenReturn(Optional.of(addressee));
        when(friendshipRepo.existsBetweenUsers(requesterId, addresseeId)).thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> meetingService.sendMeetingRequest(dto, userPrincipal)
        );

        assertEquals("You can send meeting request only with friends", exception.getMessage());

        verify(userRepo, times(1)).findById(requesterId);
        verify(userRepo, times(1)).findByUsername(participantUsername);
        verify(friendshipRepo, times(1)).existsBetweenUsers(requesterId, addresseeId);
        verifyNoInteractions(availabilityRepo, meetingRepo, meetingMapper);
    }

    @Test
    public void sendMeetingRequest_ThrowsIllegalArgumentException_WhenStartTimeIsAfterEndTime(){

        Long requesterId = 1L;
        Long addresseeId = 2L;
        String requesterUsername = "requester_user";
        String participantUsername = "other_user";

        User requester = new User();
        requester.setId(requesterId);
        requester.setUsername(requesterUsername);
        requester.setRole(Role.USER);

        User addressee = new User();
        addressee.setId(addresseeId);
        addressee.setUsername(participantUsername);
        addressee.setRole(Role.USER);


        UserPrincipal userPrincipal = new UserPrincipal(requester);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 16, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        MeetingRequestDto dto = new MeetingRequestDto(participantUsername, startTime, endTime, "Test title");
        when(userRepo.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepo.findByUsername(participantUsername)).thenReturn(Optional.of(addressee));
        when(friendshipRepo.existsBetweenUsers(requesterId, addresseeId)).thenReturn(true);


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.sendMeetingRequest(dto, userPrincipal)
        );

        assertEquals("Start time cannot be after end time", exception.getMessage());
        verify(userRepo, times(1)).findById(requesterId);
        verify(userRepo, times(1)).findByUsername(participantUsername);
        verify(friendshipRepo, times(1)).existsBetweenUsers(requesterId, addresseeId);
        verifyNoInteractions(availabilityRepo, meetingRepo, meetingMapper);
    }

    @Test
    public void sendMeetingRequest_ThrowsIllegalStateException_WhenUserNotAvailable() {
        Long requesterId = 1L;
        Long addresseeId = 2L;
        String requesterUsername = "requester_user";
        String participantUsername = "other_user";

        User requester = new User();
        requester.setId(requesterId);
        requester.setUsername(requesterUsername);
        requester.setRole(Role.USER);

        User addressee = new User();
        addressee.setId(addresseeId);
        addressee.setUsername(participantUsername);
        addressee.setRole(Role.USER);

        UserPrincipal userPrincipal = new UserPrincipal(requester);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        MeetingRequestDto dto = new MeetingRequestDto(participantUsername, startTime, endTime, "Test title");

        when(userRepo.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepo.findByUsername(participantUsername)).thenReturn(Optional.of(addressee));
        when(friendshipRepo.existsBetweenUsers(requesterId, addresseeId)).thenReturn(true);

        when(availabilityRepo.isFullyAvailable(requesterId, startTime, endTime)).thenReturn(true);
        when(availabilityRepo.isFullyAvailable(addresseeId, startTime, endTime)).thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> meetingService.sendMeetingRequest(dto, userPrincipal)
        );

        assertEquals("The requested time is outside the available slots for one or both users", exception.getMessage());

        verify(userRepo, times(1)).findById(requesterId);
        verify(userRepo, times(1)).findByUsername(participantUsername);
        verify(friendshipRepo, times(1)).existsBetweenUsers(requesterId, addresseeId);
        verify(availabilityRepo, times(1)).isFullyAvailable(requesterId, startTime, endTime);
        verify(availabilityRepo, times(1)).isFullyAvailable(addresseeId, startTime, endTime);

        verifyNoInteractions(meetingRepo, meetingMapper);
    }

    @Test
    public void sendMeetingRequest_Success() {
        Long requesterId = 1L;
        Long addresseeId = 2L;
        Long meetingId = 10L;

        String requesterUsername = "requester_user";
        String participantUsername = "other_user";

        User requester = new User();
        requester.setId(requesterId);
        requester.setUsername(requesterUsername);
        requester.setRole(Role.USER);

        User addressee = new User();
        addressee.setId(addresseeId);
        addressee.setUsername(participantUsername);
        addressee.setRole(Role.USER);

        UserPrincipal userPrincipal = new UserPrincipal(requester);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        MeetingRequestDto dto = new MeetingRequestDto(participantUsername, startTime, endTime, "Project Sync");

        when(userRepo.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepo.findByUsername(participantUsername)).thenReturn(Optional.of(addressee));
        when(friendshipRepo.existsBetweenUsers(requesterId, addresseeId)).thenReturn(true);
        when(availabilityRepo.isFullyAvailable(requesterId, startTime, endTime)).thenReturn(true);
        when(availabilityRepo.isFullyAvailable(addresseeId, startTime, endTime)).thenReturn(true);

        Meeting savedMeeting = Meeting.builder()
                .id(meetingId)
                .organizerId(requesterId)
                .participantId(addresseeId)
                .startTime(startTime)
                .endTime(endTime)
                .status(MeetingStatus.PENDING)
                .title("Project Sync")
                .build();

        when(meetingRepo.save(any(Meeting.class))).thenReturn(savedMeeting);

        MeetingResponseDto expectedResponse = new MeetingResponseDto(
                meetingId,"Project Sync", requesterId, requesterUsername, addresseeId,participantUsername, startTime, endTime, MeetingStatus.PENDING
        );
        when(meetingMapper.toResponse(any(Meeting.class), eq(requester), eq(addressee))).thenReturn(expectedResponse);

        MeetingResponseDto actualResponse = meetingService.sendMeetingRequest(dto, userPrincipal);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.id(), actualResponse.id());
        assertEquals(expectedResponse.title(), actualResponse.title());
        assertEquals(MeetingStatus.PENDING, actualResponse.status());

        verify(userRepo, times(1)).findById(requesterId);
        verify(userRepo, times(1)).findByUsername(participantUsername);
        verify(friendshipRepo, times(1)).existsBetweenUsers(requesterId, addresseeId);
        verify(availabilityRepo, times(1)).isFullyAvailable(requesterId, startTime, endTime);
        verify(availabilityRepo, times(1)).isFullyAvailable(addresseeId, startTime, endTime);
        verify(meetingRepo, times(1)).save(any(Meeting.class));
        verify(meetingMapper, times(1)).toResponse(any(Meeting.class), eq(requester), eq(addressee));
    }
}
