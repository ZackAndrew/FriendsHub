package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.model.Meeting;
import com.zack.friendshub.model.User;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {

    public MeetingResponseDto toResponse(Meeting meeting, User requester, User addressee) {
        return new MeetingResponseDto(
                meeting.getId(),
                meeting.getTitle(),
                requester.getId(),
                requester.getName(),
                addressee.getId(),
                addressee.getName(),
                meeting.getStartTime(),
                meeting.getEndTime(),
                meeting.getStatus()
        );
    }
}
