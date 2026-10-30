package com.zack.friendshub.dto.response.meeting;

import com.zack.friendshub.enums.MeetingRole;
import com.zack.friendshub.enums.MeetingStatus;

public record ParticipantDto(
        Long userId,
        String username,
        MeetingStatus status,
        MeetingRole role
) {
}