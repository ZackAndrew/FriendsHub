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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
    public void sendMeetingRequest_ThrowsIllegalArgumentException_WhenStartTimeIsAfterEndTime() {

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
                meetingId, "Project Sync", requesterId, requesterUsername, addresseeId, participantUsername, startTime, endTime, MeetingStatus.PENDING
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

    @Test
    public void acceptMeetingRequest_ShouldThrowEntityNotFoundException_WhenMeetingNotFound() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        UserPrincipal currentUser = new UserPrincipal(user);
        Long meetingId = 100L;

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.acceptMeetingRequest(meetingId, currentUser)
        );

        assertEquals("Meeting not found", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @Test
    public void acceptMeetingRequest_ShouldThrowAccessDeniedException_WhenUserIsNotParticipant() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(2L);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> meetingService.acceptMeetingRequest(meetingId, currentUser)
        );

        assertEquals("You are not allowed to accept this meeting", exception.getMessage());

        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @ParameterizedTest
    @EnumSource(value = MeetingStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    public void acceptMeetingRequest_ShouldThrowIllegalStateException_WhenMeetingIsNotPending(MeetingStatus invalidStatus) {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(user.getId());
        meeting.setStatus(invalidStatus);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> meetingService.acceptMeetingRequest(meetingId, currentUser)
        );

        assertEquals("The meeting is not pending", exception.getMessage());

        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @Test
    public void acceptMeetingRequest_ThrowsEntityNotFoundException_WhenOrganizerNotFound() {
        Long currentUserId = 1L;
        Long organizerId = 2L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(currentUserId);
        meeting.setOrganizerId(organizerId);
        meeting.setStatus(MeetingStatus.PENDING);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(organizerId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.acceptMeetingRequest(meetingId, userPrincipal)
        );

        assertEquals("Requester not found", exception.getMessage());

        verify(meetingRepo, times(1)).findById(meetingId);
        verify(userRepo, times(1)).findById(organizerId);
        verifyNoInteractions(meetingMapper);
    }

    @Test
    public void acceptMeetingRequest_ThrowsEntityNotFoundException_WhenAddresseeNotFound() {
        Long currentUserId = 1L;
        Long organizerId = 2L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User organizer = new User();
        organizer.setId(organizerId);
        organizer.setUsername("organizer_user");

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(currentUserId);
        meeting.setOrganizerId(organizerId);
        meeting.setStatus(MeetingStatus.PENDING);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepo.findById(currentUserId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.acceptMeetingRequest(meetingId, userPrincipal)
        );

        assertEquals("Addressee not found", exception.getMessage());

        verify(meetingRepo, times(1)).findById(meetingId);
        verify(userRepo, times(1)).findById(organizerId);
        verify(userRepo, times(1)).findById(currentUserId);
        verifyNoInteractions(meetingMapper);
    }

    @Test
    public void acceptMeetingRequest_Success() {
        Long currentUserId = 1L;
        Long organizerId = 2L;
        Long meetingId = 100L;

        String currentUsername = "addressee_user";
        String organizerUsername = "organizer_user";

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername(currentUsername);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User organizer = new User();
        organizer.setId(organizerId);
        organizer.setUsername(organizerUsername);
        organizer.setRole(Role.USER);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .organizerId(organizerId)
                .participantId(currentUserId)
                .startTime(startTime)
                .endTime(endTime)
                .status(MeetingStatus.PENDING)
                .title("Project Sync")
                .build();

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepo.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        MeetingResponseDto expectedResponse = new MeetingResponseDto(
                meetingId,
                "Project Sync",
                organizerId,
                organizerUsername,
                currentUserId,
                currentUsername,
                startTime,
                endTime,
                MeetingStatus.ACCEPTED
        );

        when(meetingMapper.toResponse(eq(meeting), eq(organizer), eq(currentUser))).thenReturn(expectedResponse);

        MeetingResponseDto actualResponse = meetingService.acceptMeetingRequest(meetingId, userPrincipal);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.id(), actualResponse.id());
        assertEquals(expectedResponse.title(), actualResponse.title());
        assertEquals(MeetingStatus.ACCEPTED, actualResponse.status());

        verify(meetingRepo, times(1)).findById(meetingId);
        verify(userRepo, times(1)).findById(organizerId);
        verify(userRepo, times(1)).findById(currentUserId);
        verify(meetingMapper, times(1)).toResponse(eq(meeting), eq(organizer), eq(currentUser));
    }

    @Test
    public void declineMeetingRequest_ThrowsEntityNotFoundException_WhenMeetingNotFound() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);
        Long meetingId = 100L;

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.declineMeetingRequest(meetingId, currentUser)
        );

        assertEquals("Meeting not found", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @Test
    public void declineMeetingRequest_ThrowsAccessDeniedException_WhenUserIsNotParticipant() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(2L);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> meetingService.declineMeetingRequest(meetingId, currentUser)
        );

        assertEquals("You are not allowed to decline this meeting", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @ParameterizedTest
    @EnumSource(value = MeetingStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    public void declineMeetingRequest_ThrowsIllegalStateException_WhenMeetingIsNotPending(MeetingStatus invalidStatus) {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(userId);
        meeting.setStatus(invalidStatus);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> meetingService.declineMeetingRequest(meetingId, currentUser)
        );

        assertEquals("The meeting is not pending", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @Test
    public void declineMeetingRequest_ThrowsEntityNotFoundException_WhenOrganizerNotFound() {
        Long currentUserId = 1L;
        Long organizerId = 2L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(currentUserId);
        meeting.setOrganizerId(organizerId);
        meeting.setStatus(MeetingStatus.PENDING);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(organizerId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.declineMeetingRequest(meetingId, userPrincipal)
        );

        assertEquals("Requester not found", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verify(userRepo, times(1)).findById(organizerId);
        verifyNoInteractions(meetingMapper);
    }

    @Test
    public void declineMeetingRequest_ThrowsEntityNotFoundException_WhenAddresseeNotFound() {
        Long currentUserId = 1L;
        Long organizerId = 2L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User organizer = new User();
        organizer.setId(organizerId);
        organizer.setUsername("organizer_user");

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setParticipantId(currentUserId);
        meeting.setOrganizerId(organizerId);
        meeting.setStatus(MeetingStatus.PENDING);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepo.findById(currentUserId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.declineMeetingRequest(meetingId, userPrincipal)
        );

        assertEquals("Addressee not found", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verify(userRepo, times(1)).findById(organizerId);
        verify(userRepo, times(1)).findById(currentUserId);
        verifyNoInteractions(meetingMapper);
    }

    @Test
    public void declineMeetingRequest_Success() {
        Long currentUserId = 1L;
        Long organizerId = 2L;
        Long meetingId = 100L;

        String currentUsername = "addressee_user";
        String organizerUsername = "organizer_user";

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername(currentUsername);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User organizer = new User();
        organizer.setId(organizerId);
        organizer.setUsername(organizerUsername);
        organizer.setRole(Role.USER);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .organizerId(organizerId)
                .participantId(currentUserId)
                .startTime(startTime)
                .endTime(endTime)
                .status(MeetingStatus.PENDING)
                .title("Project Sync")
                .build();

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepo.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        MeetingResponseDto expectedResponse = new MeetingResponseDto(
                meetingId,
                "Project Sync",
                organizerId,
                organizerUsername,
                currentUserId,
                currentUsername,
                startTime,
                endTime,
                MeetingStatus.DECLINED
        );

        when(meetingMapper.toResponse(eq(meeting), eq(organizer), eq(currentUser))).thenReturn(expectedResponse);

        MeetingResponseDto actualResponse = meetingService.declineMeetingRequest(meetingId, userPrincipal);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.id(), actualResponse.id());
        assertEquals(expectedResponse.title(), actualResponse.title());
        assertEquals(MeetingStatus.DECLINED, actualResponse.status());

        verify(meetingRepo, times(1)).findById(meetingId);
        verify(userRepo, times(1)).findById(organizerId);
        verify(userRepo, times(1)).findById(currentUserId);
        verify(meetingMapper, times(1)).toResponse(eq(meeting), eq(organizer), eq(currentUser));
    }

    @Test
    public void cancelMeetingRequest_ThrowsEntityNotFoundException_WhenMeetingNotFound() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);
        Long meetingId = 100L;

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.cancelMeetingRequest(meetingId, currentUser)
        );

        assertEquals("Meeting not found", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @Test
    public void cancelMeetingRequest_ThrowsAccessDeniedException_WhenUserIsNotOrganizer() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setOrganizerId(2L);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> meetingService.cancelMeetingRequest(meetingId, currentUser)
        );

        assertEquals("Only Organizer can cancel meeting", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @ParameterizedTest
    @EnumSource(value = MeetingStatus.class, names = {"DECLINED", "CANCELED"}, mode = EnumSource.Mode.INCLUDE)
    public void cancelMeetingRequest_ThrowsIllegalStateException_WhenMeetingIsAlreadyDeclinedOrCanceled(MeetingStatus invalidStatus) {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setOrganizerId(userId);
        meeting.setStatus(invalidStatus);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> meetingService.cancelMeetingRequest(meetingId, currentUser)
        );

        assertEquals("Cannot cancel a meeting that is already declined or cancelled", exception.getMessage());
        verify(meetingRepo, times(1)).findById(meetingId);
        verifyNoInteractions(userRepo, meetingMapper);
    }

    @Test
    public void cancelMeetingRequest_Success() {
        Long currentUserId = 1L;
        Long addresseeId = 2L;
        Long meetingId = 100L;

        String currentUsername = "organizer_user";
        String addresseeUsername = "addressee_user";

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername(currentUsername);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User addressee = new User();
        addressee.setId(addresseeId);
        addressee.setUsername(addresseeUsername);
        addressee.setRole(Role.USER);

        LocalDateTime startTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0, 0);

        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .organizerId(currentUserId)
                .participantId(addresseeId)
                .startTime(startTime)
                .endTime(endTime)
                .status(MeetingStatus.PENDING)
                .title("Project Sync")
                .build();

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(addresseeId)).thenReturn(Optional.of(addressee));
        when(userRepo.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        MeetingResponseDto expectedResponse = new MeetingResponseDto(
                meetingId, "Project Sync", currentUserId, currentUsername, addresseeId, addresseeUsername, startTime, endTime, MeetingStatus.CANCELED
        );

        when(meetingMapper.toResponse(eq(meeting), eq(currentUser), eq(addressee))).thenReturn(expectedResponse);

        MeetingResponseDto actualResponse = meetingService.cancelMeetingRequest(meetingId, userPrincipal);

        assertNotNull(actualResponse);
        assertEquals(MeetingStatus.CANCELED, actualResponse.status());
        verify(meetingRepo, times(1)).findById(meetingId);
    }

    @Test
    public void getMeetingById_ThrowsEntityNotFoundException_WhenMeetingNotFound() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);
        Long meetingId = 100L;

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> meetingService.getMeetingById(meetingId, currentUser)
        );

        assertEquals("Meeting not found", exception.getMessage());
    }

    @Test
    public void getMeetingById_ThrowsAccessDeniedException_WhenUserIsNotParticipantOrOrganizer() {
        User user = new User();
        user.setId(3L);
        user.setRole(Role.USER);
        UserPrincipal currentUser = new UserPrincipal(user);

        Long meetingId = 100L;
        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setOrganizerId(1L);
        meeting.setParticipantId(2L);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> meetingService.getMeetingById(meetingId, currentUser)
        );

        assertEquals("Only Organizer or participant can get meeting", exception.getMessage());
    }

    @Test
    public void getMeetingById_Success_ForOrganizer() {
        Long currentUserId = 1L;
        Long addresseeId = 2L;
        Long meetingId = 100L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User addressee = new User();
        addressee.setId(addresseeId);
        addressee.setRole(Role.USER);

        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setOrganizerId(currentUserId);
        meeting.setParticipantId(addresseeId);

        when(meetingRepo.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userRepo.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepo.findById(addresseeId)).thenReturn(Optional.of(addressee));

        when(meetingMapper.toResponse(any(), any(), any())).thenReturn(null);

        meetingService.getMeetingById(meetingId, userPrincipal);

        verify(meetingRepo, times(1)).findById(meetingId);
        verify(meetingMapper, times(1)).toResponse(meeting, currentUser, addressee);
    }

    @Test
    public void getOutgoingMeetings_Success() {
        Long currentUserId = 1L;
        Long participantId = 2L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User participant = new User();
        participant.setId(participantId);

        Meeting meeting = new Meeting();
        meeting.setOrganizerId(currentUserId);
        meeting.setParticipantId(participantId);
        List<Meeting> meetings = List.of(meeting);

        when(meetingRepo.findAllByOrganizerIdAndStatus(currentUserId, MeetingStatus.PENDING)).thenReturn(meetings);
        when(userRepo.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepo.findById(participantId)).thenReturn(Optional.of(participant));

        MeetingResponseDto dto = new MeetingResponseDto(1L, "Test", currentUserId, "org", participantId, "part", null, null, MeetingStatus.PENDING);
        when(meetingMapper.toResponse(meeting, currentUser, participant)).thenReturn(dto);

        List<MeetingResponseDto> result = meetingService.getOutgoingMeetings(userPrincipal);

        assertEquals(1, result.size());
        verify(meetingRepo, times(1)).findAllByOrganizerIdAndStatus(currentUserId, MeetingStatus.PENDING);
    }

    @Test
    public void getPendingMeetings_Success() {
        Long currentUserId = 1L;
        Long organizerId = 2L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User organizer = new User();
        organizer.setId(organizerId);
        organizer.setRole(Role.USER);

        Meeting meeting = new Meeting();
        meeting.setOrganizerId(organizerId);
        meeting.setParticipantId(currentUserId);
        List<Meeting> meetings = List.of(meeting);

        when(meetingRepo.findAllByParticipantIdAndStatus(currentUserId, MeetingStatus.PENDING)).thenReturn(meetings);
        when(userRepo.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepo.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        MeetingResponseDto dto = new MeetingResponseDto(1L, "Test", organizerId, "org", currentUserId, "part", null, null, MeetingStatus.PENDING);
        when(meetingMapper.toResponse(meeting, organizer, currentUser)).thenReturn(dto);

        List<MeetingResponseDto> result = meetingService.getPendingMeetings(userPrincipal);

        assertEquals(1, result.size());
        verify(meetingRepo, times(1)).findAllByParticipantIdAndStatus(currentUserId, MeetingStatus.PENDING);
    }

    @Test
    public void getUserMeetings_Success() {
        Long currentUserId = 1L;
        Long otherUserId = 2L;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.USER);
        UserPrincipal userPrincipal = new UserPrincipal(currentUser);

        User otherUser = new User();
        otherUser.setId(otherUserId);

        Meeting meeting = new Meeting();
        meeting.setOrganizerId(currentUserId);
        meeting.setParticipantId(otherUserId);
        List<Meeting> meetings = List.of(meeting);

        when(meetingRepo.findAllUserMeetings(currentUserId)).thenReturn(meetings);
        when(userRepo.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepo.findById(otherUserId)).thenReturn(Optional.of(otherUser));

        MeetingResponseDto dto = new MeetingResponseDto(1L, "Test", currentUserId, "org", otherUserId, "part", null, null, MeetingStatus.ACCEPTED);
        when(meetingMapper.toResponse(meeting, currentUser, otherUser)).thenReturn(dto);

        List<MeetingResponseDto> result = meetingService.getUserMeetings(userPrincipal);

        assertEquals(1, result.size());
        verify(meetingRepo, times(1)).findAllUserMeetings(currentUserId);
    }
}
