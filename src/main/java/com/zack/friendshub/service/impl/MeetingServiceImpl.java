package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.MeetingProposalDto;
import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.request.MeetingVoteDto;
import com.zack.friendshub.dto.response.ScoredTimeSlot;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.enums.MeetingRole;
import com.zack.friendshub.enums.MeetingStatus;
import com.zack.friendshub.exception.SelfMeetingRequestException;
import com.zack.friendshub.mapper.MeetingMapper;
import com.zack.friendshub.model.*;
import com.zack.friendshub.repository.*;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.GoogleCalendarService;
import com.zack.friendshub.service.MeetingService;
import com.zack.friendshub.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    private final UserRepo userRepo;
    private final FriendshipRepo friendshipRepo;
    private final MeetingRepo meetingRepo;
    private final MeetingMapper meetingMapper;
    private final MeetingParticipantRepo meetingParticipantRepo;
    private final MeetingOptionRepo meetingOptionRepo;
    private final GoogleCalendarService googleCalendarService;
    private final NotificationService notificationService;

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
                .organizer(requester)
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
                .voted(true)
                .build());

        for (User participant : participants) {
            meetingParticipants.add(MeetingParticipant.builder()
                    .meeting(savedMeeting)
                    .user(participant)
                    .status(MeetingStatus.PENDING)
                    .role(MeetingRole.GUEST)
                    .voted(false)
                    .build());
        }

        List<String> participantEmails = participants.stream()
                .map(User::getEmail)
                .toList();

        try {
            String eventId = googleCalendarService.createMeetingEvent(requester.getId(), savedMeeting, participantEmails);
            savedMeeting.setGoogleEventId(eventId);
            meetingRepo.save(savedMeeting);
        } catch (Exception e) {
            log.error("Warning: Could not create Google event for classic meeting", e);
        }

        meetingParticipantRepo.saveAll(meetingParticipants);

        for (MeetingParticipant participant : meetingParticipants) {
            if (!participant.getRole().equals(MeetingRole.ORGANIZER)) {
                notificationService.createNotification(participant.getUser().getId(),
                        "Meeting request \"%s\" from %s".formatted(
                                StringUtils.abbreviate(savedMeeting.getTitle(), 15),
                                StringUtils.abbreviate(currentUser.getUsername(), 15)),
                        savedMeeting.getId());
            }
        }
        return meetingMapper.toResponse(savedMeeting, requester, meetingParticipants);
    }

    @Override
    @Transactional
    public MeetingResponseDto acceptMeetingRequest(Long meetingId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        if (meeting.getStartTime() == null) {
            throw new IllegalStateException("This is a Smart Meeting poll. Please use the /vote endpoint to accept options.");
        }

        MeetingParticipant currentParticipant = meetingParticipantRepo.findByMeetingIdAndUserId(meetingId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        if (currentParticipant.getStatus().equals(MeetingStatus.ACCEPTED)) {
            throw new IllegalStateException("You have already accepted this meeting request");
        }

        currentParticipant.setStatus(MeetingStatus.ACCEPTED);
        currentParticipant.setVoted(true);
        meetingParticipantRepo.save(currentParticipant);

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);
        return meetingMapper.toResponse(meeting, meeting.getOrganizer(), allParticipants);
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
        currentParticipant.setVoted(true);
        meetingParticipantRepo.save(currentParticipant);

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);

        return meetingMapper.toResponse(meeting, meeting.getOrganizer(), allParticipants);
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

        if (meeting.getGoogleEventId() != null) {
            googleCalendarService.deleteEvent(currentUser.getId(), meeting.getGoogleEventId());
        }
        for (MeetingParticipant participant : meeting.getParticipants()) {
            if (!participant.getRole().equals(MeetingRole.ORGANIZER)) {
                notificationService.createNotification(participant.getUser().getId(),
                        "Meeting request for \"%s\" from %s was canceled".formatted(
                                StringUtils.abbreviate(meeting.getTitle(), 15),
                                StringUtils.abbreviate(currentUser.getUsername(), 15)),
                        meeting.getId());
            }
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

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);

        return meetingMapper.toResponse(meeting, meeting.getOrganizer(), allParticipants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getPendingMeetings(UserPrincipal currentUser) {
        List<MeetingParticipant> pendingParticipations = meetingParticipantRepo
                .findAllByUserIdAndStatus(currentUser.getId(), MeetingStatus.PENDING);

        List<MeetingParticipant> votingParticipations = meetingParticipantRepo
                .findAllByUserIdAndStatus(currentUser.getId(), MeetingStatus.VOTING);

        List<Meeting> pendingMeetings = new ArrayList<>();

        pendingParticipations.forEach(p -> pendingMeetings.add(p.getMeeting()));

        votingParticipations.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getVoted()))
                .forEach(p -> pendingMeetings.add(p.getMeeting()));

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

    @Override
    @Transactional
    public MeetingResponseDto proposeSmartMeeting(MeetingProposalDto dto, UserPrincipal currentUser) {
        if (dto.participantNames().contains(currentUser.getUsername())) {
            throw new SelfMeetingRequestException("You can't propose a meeting with yourself");
        }

        User requester = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        List<User> participants = userRepo.findAllByUsernameIn(dto.participantNames());

        if (participants.size() != dto.participantNames().size()) {
            throw new EntityNotFoundException("Some participants not found");
        }

        for (User participant : participants) {
            if (!friendshipRepo.existsBetweenUsers(participant.getId(), currentUser.getId())) {
                throw new IllegalStateException("You are not friends with: " + participant.getUsername());
            }
        }

        if (dto.proposedOptions() == null || dto.proposedOptions().isEmpty()) {
            throw new IllegalArgumentException("Proposed options cannot be empty");
        }

        Meeting meeting = Meeting.builder()
                .organizer(requester)
                .title(dto.title())
                .build();

        meetingRepo.save(meeting);

        List<MeetingOption> proposedOptions = new ArrayList<>();

        for (ScoredTimeSlot slot : dto.proposedOptions()) {
            MeetingOption opt = new MeetingOption();
            opt.setMeeting(meeting);
            opt.setStartTime(slot.start());
            opt.setEndTime(slot.end());
            opt.setVotesCount(0);
            proposedOptions.add(opt);
        }

        meetingOptionRepo.saveAll(proposedOptions);

        List<MeetingParticipant> participantsToSave = new ArrayList<>();

        MeetingParticipant organizer = MeetingParticipant.builder()
                .meeting(meeting)
                .user(requester)
                .status(MeetingStatus.VOTING)
                .role(MeetingRole.ORGANIZER)
                .voted(true)
                .build();
        participantsToSave.add(organizer);

        for (User guestUser : participants) {
            MeetingParticipant participant = MeetingParticipant.builder()
                    .meeting(meeting)
                    .user(guestUser)
                    .status(MeetingStatus.VOTING)
                    .role(MeetingRole.GUEST)
                    .voted(false)
                    .build();
            participantsToSave.add(participant);
        }

        meetingParticipantRepo.saveAll(participantsToSave);

        for (MeetingParticipant participant : participantsToSave) {
            if (!participant.getRole().equals(MeetingRole.ORGANIZER)) {
                notificationService.createNotification(participant.getUser().getId(),
                        "Meeting request \"%s\" from %s".formatted(
                                StringUtils.abbreviate(meeting.getTitle(), 15),
                                StringUtils.abbreviate(currentUser.getUsername(), 15)),
                        meeting.getId());
            }
        }
        
        return meetingMapper.toResponse(meeting, requester, participantsToSave);
    }

    @Override
    @Transactional
    public MeetingResponseDto voteForOptions(Long meetingId, MeetingVoteDto dto, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        if (meeting.getStartTime() != null) {
            throw new IllegalStateException("Voting for this meeting is already finished");
        }

        MeetingParticipant participant = meetingParticipantRepo.findByMeetingIdAndUserId(meetingId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getVoted())) {
            throw new IllegalStateException("You already voted");
        }

        if (dto.selectedOptionIds() == null || dto.selectedOptionIds().isEmpty()) {
            participant.setVoted(true);
            participant.setStatus(MeetingStatus.DECLINED);
        } else {
            participant.setVoted(true);
            for (Long optId : dto.selectedOptionIds()) {
                MeetingOption option = meetingOptionRepo.findById(optId)
                        .orElseThrow(() -> new EntityNotFoundException("Option not found: " + optId));

                if (!option.getMeeting().getId().equals(meetingId)) {
                    throw new IllegalArgumentException("This option does not belong to the requested meeting");
                }

                MeetingVote vote = MeetingVote.builder()
                        .participant(participant)
                        .option(option)
                        .build();

                option.setVotesCount(option.getVotesCount() + 1);
                participant.getVotes().add(vote);
            }
        }

        meetingParticipantRepo.save(participant);

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);

        return meetingMapper.toResponse(meeting, meeting.getOrganizer(), allParticipants);
    }

    @Override
    @Transactional
    public MeetingResponseDto finalizeSmartMeeting(Long meetingId, Long winningOptionId, UserPrincipal currentUser) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        if (!meeting.getOrganizer().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the organizer can finalize the meeting");
        }

        List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meetingId);

        if (winningOptionId == null) {
            for (MeetingParticipant p : allParticipants) {
                if (!p.getStatus().equals(MeetingStatus.DECLINED)) {
                    p.setStatus(MeetingStatus.DECLINED);
                }
            }
            meetingParticipantRepo.saveAll(allParticipants);
            return meetingMapper.toResponse(meeting, meeting.getOrganizer(), allParticipants);
        }

        MeetingOption winningOption = meetingOptionRepo.findById(winningOptionId)
                .orElseThrow(() -> new EntityNotFoundException("Winning option not found"));

        if (!winningOption.getMeeting().getId().equals(meetingId)) {
            throw new IllegalArgumentException("Option does not belong to this meeting");
        }

        meeting.setStartTime(winningOption.getStartTime());
        meeting.setEndTime(winningOption.getEndTime());
        meetingRepo.save(meeting);

        List<String> participantsEmail = new ArrayList<>();
        for (MeetingParticipant p : allParticipants) {
            if (p.getRole().equals(MeetingRole.ORGANIZER)) {
                p.setStatus(MeetingStatus.ACCEPTED);
                continue;
            }

            boolean votedForWinning = p.getVotes().stream()
                    .anyMatch(v -> v.getOption().getId().equals(winningOptionId));

            if (votedForWinning) {
                p.setStatus(MeetingStatus.ACCEPTED);
                participantsEmail.add(p.getUser().getEmail());
            } else {
                p.setStatus(MeetingStatus.DECLINED);
            }
        }

        String eventId = googleCalendarService.createMeetingEvent(currentUser.getId(), meeting, participantsEmail);
        meetingRepo.save(meeting);

        meetingParticipantRepo.saveAll(allParticipants);

        for (MeetingParticipant participant : allParticipants) {
            if (!participant.getRole().equals(MeetingRole.ORGANIZER)) {
                notificationService.createNotification(participant.getUser().getId(),
                        "Meeting request \"%s\" from %s was set".formatted(
                                StringUtils.abbreviate(meeting.getTitle(), 15),
                                StringUtils.abbreviate(currentUser.getUsername(), 15)),
                        meeting.getId());
            }
        }


        return meetingMapper.toResponse(meeting, meeting.getOrganizer(), allParticipants);
    }

    @NotNull
    private List<MeetingResponseDto> getMeetingDtos(List<Meeting> pendingMeetings) {
        return pendingMeetings.stream()
                .map(meeting -> {
                    User organizer = meeting.getOrganizer();
                    List<MeetingParticipant> allParticipants = meetingParticipantRepo.findAllByMeetingId(meeting.getId());
                    return meetingMapper.toResponse(meeting, organizer, allParticipants);
                }).toList();
    }
}