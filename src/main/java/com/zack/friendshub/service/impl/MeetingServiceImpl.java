package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.enums.MeetingStatus;
import com.zack.friendshub.exception.SelfMeetingRequestException;
import com.zack.friendshub.mapper.MeetingMapper;
import com.zack.friendshub.model.Meeting;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.AvailabilityRepo;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.MeetingRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.MeetingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final UserRepo userRepo;
    private final FriendshipRepo friendshipRepo;
    private final AvailabilityRepo availabilityRepo;
    private final MeetingRepo meetingRepo;
    private final MeetingMapper meetingMapper;

    @Override
    public MeetingResponseDto sendMeetingRequest(MeetingRequestDto dto, UserPrincipal currentUser) {
        if (currentUser.getUsername().equals(dto.participantUsername())) {
            throw new SelfMeetingRequestException(
                    "User cannot send meeting request to himself"
            );
        }

        User requester = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        User addressee = userRepo.findByUsername(dto.participantUsername())
                .orElseThrow(() -> new EntityNotFoundException("Addressee not found"));


        if (!friendshipRepo.existsBetweenUsers(requester.getId(), addressee.getId())) {
            throw new IllegalStateException("You can send meeting request only with friends");
        }

        if (dto.startTime().isAfter(dto.endTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }

        boolean requesterAvailable = availabilityRepo.isFullyAvailable(requester.getId(), dto.startTime(), dto.endTime());
        boolean addresseeAvailable = availabilityRepo.isFullyAvailable(addressee.getId(), dto.startTime(), dto.endTime());

        if (!requesterAvailable || !addresseeAvailable) {
            throw new IllegalStateException("The requested time is outside the available slots for one or both users");
        }

        Meeting meeting = Meeting.builder()
                .organizerId(requester.getId())
                .participantId(addressee.getId())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .status(MeetingStatus.PENDING)
                .title(dto.title())
                .build();

        Meeting savedMeeting = meetingRepo.save(meeting);
        return meetingMapper.toResponse(savedMeeting, requester, addressee);
    }

    @Override
    public MeetingResponseDto acceptMeetingRequest(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        if (!currentUser.getId().equals(meeting.getParticipantId())) {
            throw new AccessDeniedException("You are not allowed to accept this meeting");
        }

        if (!meeting.getStatus().equals(MeetingStatus.PENDING)) {
            throw new IllegalStateException("The meeting is not pending");
        }

        meeting.setStatus(MeetingStatus.ACCEPTED);

        User requester = userRepo.findById(meeting.getOrganizerId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));
        User addressee = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Addressee not found"));

        return meetingMapper.toResponse(meeting, requester, addressee);
    }

    @Override
    public MeetingResponseDto declineMeetingRequest(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        if (!meeting.getParticipantId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to decline this meeting");
        }
        if (!meeting.getStatus().equals(MeetingStatus.PENDING)) {
            throw new IllegalStateException("The meeting is not pending");
        }

        meeting.setStatus(MeetingStatus.DECLINED);

        User requester = userRepo.findById(meeting.getOrganizerId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));
        User addressee = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Addressee not found"));

        return meetingMapper.toResponse(meeting, requester, addressee);
    }

    @Override
    public MeetingResponseDto cancelMeetingRequest(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        if (!currentUser.getId().equals(meeting.getOrganizerId())) {
            throw new AccessDeniedException("Only Organizer can cancel meeting");
        }

        if (meeting.getStatus() == MeetingStatus.DECLINED || meeting.getStatus() == MeetingStatus.CANCELED) {
            throw new IllegalStateException("Cannot cancel a meeting that is already declined or cancelled");
        }

        meeting.setStatus(MeetingStatus.CANCELED);

        User addressee = userRepo.findById(meeting.getParticipantId())
                .orElseThrow(() -> new EntityNotFoundException("Addressee not found"));

        User requester = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        return meetingMapper.toResponse(meeting, requester, addressee);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDto getMeetingById(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        if (!currentUser.getId().equals(meeting.getParticipantId()) &&
                !currentUser.getId().equals(meeting.getOrganizerId())) {
            throw new AccessDeniedException("Only Organizer or participant can get meeting");
        }

        User requester = userRepo.findById(meeting.getOrganizerId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));
        User addressee = userRepo.findById(meeting.getParticipantId())
                .orElseThrow(() -> new EntityNotFoundException("Addressee not found"));

        return meetingMapper.toResponse(meeting, requester, addressee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getOutgoingMeetings(UserPrincipal currentUser) {
        List<Meeting> outgoingMeetings = meetingRepo.findAllByOrganizerIdAndStatus((currentUser.getId()), MeetingStatus.PENDING);

        return getMeetingDtos(outgoingMeetings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getPendingMeetings(UserPrincipal currentUser) {
        List<Meeting> pendingMeetings = meetingRepo.findAllByParticipantIdAndStatus(currentUser.getId(), MeetingStatus.PENDING);

        return getMeetingDtos(pendingMeetings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getUserMeetings(UserPrincipal currentUser) {
        List<Meeting> meetings = meetingRepo.findAllUserMeetings(currentUser.getId());

        return getMeetingDtos(meetings);
    }

    @NotNull
    private List<MeetingResponseDto> getMeetingDtos(List<Meeting> pendingMeetings) {
        return pendingMeetings.stream()
                .map(meeting -> {
                    User requester = userRepo.findById(meeting.getOrganizerId())
                            .orElseThrow(() -> new EntityNotFoundException("Requester not found"));
                    User addressee = userRepo.findById(meeting.getParticipantId())
                            .orElseThrow(() -> new EntityNotFoundException("Addressee not found"));
                    return meetingMapper.toResponse(meeting, requester, addressee);
                }).toList();
    }
}
