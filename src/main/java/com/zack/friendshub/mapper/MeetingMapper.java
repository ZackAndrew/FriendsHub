package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.dto.response.meeting.ParticipantDto;
import com.zack.friendshub.model.Meeting;
import com.zack.friendshub.model.MeetingParticipant;
import com.zack.friendshub.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeetingMapper {

    public MeetingResponseDto toResponse(Meeting meeting, User requester, List<MeetingParticipant> meetingParticipants) {

        List<ParticipantDto> participantDtos = meetingParticipants.stream()
                .map(p -> new ParticipantDto(
                        p.getUser().getId(),
                        p.getUser().getUsername(),
                        p.getStatus(),
                        p.getRole()
                ))
                .toList();

        return new MeetingResponseDto(
                meeting.getId(),
                meeting.getTitle(),
                requester.getId(),
                requester.getName(),
                participantDtos,
                meeting.getStartTime(),
                meeting.getEndTime()
        );
    }
}