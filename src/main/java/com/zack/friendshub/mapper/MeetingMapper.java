package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.MeetingOptionDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.dto.response.meeting.ParticipantDto;
import com.zack.friendshub.model.Meeting;
import com.zack.friendshub.model.MeetingOption;
import com.zack.friendshub.model.MeetingParticipant;
import com.zack.friendshub.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MeetingMapper {

    public MeetingResponseDto toResponse(Meeting meeting, User requester, List<MeetingParticipant> meetingParticipants) {

        List<ParticipantDto> participantDtos = new ArrayList<>();
        for (MeetingParticipant participant : meetingParticipants) {
            ParticipantDto participantDto = new ParticipantDto(
                    participant.getId(),
                    participant.getUser().getUsername(),
                    participant.getStatus(),
                    participant.getRole()
            );
            participantDtos.add(participantDto);
        }

                List<MeetingOptionDto> optionDtos = new ArrayList<>();
        if (meeting.getOptions() != null) {
            for (MeetingOption option : meeting.getOptions()) {
                optionDtos.add(new MeetingOptionDto(
                        option.getId(),
                        option.getStartTime(),
                        option.getEndTime(),
                        option.getVotesCount()
                ));
            }
        }

        return new MeetingResponseDto(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getOrganizer().getId(),
                requester.getName(),
                participantDtos,
                meeting.getStartTime(),
                meeting.getEndTime(),
                optionDtos
        );
    }
}