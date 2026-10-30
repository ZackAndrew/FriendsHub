package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.enums.MeetingRole;
import com.zack.friendshub.enums.MeetingStatus;
import com.zack.friendshub.exception.SelfMeetingRequestException;
import com.zack.friendshub.mapper.MeetingMapper;
import com.zack.friendshub.model.Meeting;
import com.zack.friendshub.model.MeetingParticipant;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.MeetingParticipantRepo;
import com.zack.friendshub.repository.MeetingRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.MeetingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final UserRepo userRepo;
    private final FriendshipRepo friendshipRepo;
    private final MeetingRepo meetingRepo;
    private final MeetingMapper meetingMapper;
    private final MeetingParticipantRepo meetingParticipantRepo;

    @Override
    @Transactional
    public MeetingResponseDto sendMeetingRequest(MeetingRequestDto dto, UserPrincipal currentUser) {
        if (dto.participantNames().contains(currentUser.getUsername())) {
            throw new SelfMeetingRequestException("User cannot send meeting request to self");
        }

        if (dto.startTime().isAfter(dto.endTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }

        User requester = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        List<User> participants = userRepo.findAllByUsernameIn(dto.participantNames());

        if (participants.size() != dto.participantNames().size()) {
            throw new EntityNotFoundException("One or more participants not found");
        }

        for (User participant : participants) {
            if (!friendshipRepo.existsBetweenUsers(requester.getId(), participant.getId())) {
                throw new IllegalStateException("You can send meeting requests only to friends. Not friends with: " + participant.getUsername());
            }
        }

        Meeting meeting = Meeting.builder()
                .organizerId(requester.getId())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .title(dto.title())
                .build();

        Meeting savedMeeting = meetingRepo.save(meeting);

        List<MeetingParticipant> meetingParticipants = new ArrayList<>();

        meetingParticipants.add(MeetingParticipant.builder()
                .meeting(savedMeeting)
                .user(requester)
                .status(MeetingStatus.ACCEPTED)
                .role(MeetingRole.ORGANIZER)
                .build());

        for (User participant : participants) {
            meetingParticipants.add(MeetingParticipant.builder()
                    .meeting(savedMeeting)
                    .user(participant)
                    .status(MeetingStatus.PENDING)
                    .role(MeetingRole.GUEST)
                    .build());
        }

        meetingParticipantRepo.saveAll(meetingParticipants);

        return meetingMapper.toResponse(savedMeeting, requester, meetingParticipants);
    }

    @Override
    @Transactional
    public MeetingResponseDto acceptMeetingRequest(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        MeetingParticipant currentParticipant = meetingParticipantRepo.findByMeetingIdAndUserId(meetingId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        if (currentParticipant.getStatus().equals(MeetingStatus.ACCEPTED)) {
            throw new IllegalStateException("You have already accepted this meeting request");
        }

        currentParticipant.setStatus(MeetingStatus.ACCEPTED);
        meetingParticipantRepo.save(currentParticipant);

        User organizer = userRepo.findById(meeting.getOrganizerId())
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);

        return meetingMapper.toResponse(meeting, organizer, allParticipants);
    }

    @Override
    @Transactional
    public MeetingResponseDto declineMeetingRequest(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        MeetingParticipant currentParticipant = meetingParticipantRepo.findByMeetingIdAndUserId(meetingId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        if (currentParticipant.getStatus().equals(MeetingStatus.DECLINED)) {
            throw new IllegalStateException("Your meeting request is already DECLINED");
        }

        currentParticipant.setStatus(MeetingStatus.DECLINED);
        meetingParticipantRepo.save(currentParticipant);

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);

        User organizer = userRepo.findById(meeting.getOrganizerId())
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));

        return meetingMapper.toResponse(meeting, organizer, allParticipants);
    }

    @Override
    @Transactional
    public void cancelMeetingRequest(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        MeetingParticipant currentParticipant = meetingParticipantRepo.findByMeetingIdAndUserId(meetingId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        if (!currentParticipant.getRole().equals(MeetingRole.ORGANIZER)) {
            throw new IllegalStateException("You do not have permission to cancel this meeting");
        }

        meetingRepo.delete(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDto getMeetingById(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        meetingParticipantRepo.findByMeetingIdAndUserId(meetingId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        User organizer = userRepo.findById(meeting.getOrganizerId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);

        return meetingMapper.toResponse(meeting, organizer, allParticipants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getPendingMeetings(UserPrincipal currentUser) {
        List<MeetingParticipant> pendingParticipations = meetingParticipantRepo
                .findAllByUserIdAndStatus(currentUser.getId(), MeetingStatus.PENDING);

        List<Meeting> pendingMeetings = pendingParticipations.stream()
                .map(MeetingParticipant::getMeeting)
                .toList();

        return getMeetingDtos(pendingMeetings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getOutgoingMeetings(UserPrincipal currentUser) {
        List<Meeting> outgoingMeetings = meetingRepo.findAllByOrganizerId(currentUser.getId());
        return getMeetingDtos(outgoingMeetings);
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
                    User organizer = userRepo.findById(meeting.getOrganizerId())
                            .orElseThrow(() -> new EntityNotFoundException("Requester not found"));
                    List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meeting.getId());
                    return meetingMapper.toResponse(meeting, organizer, allParticipants);
                }).toList();
    }
}