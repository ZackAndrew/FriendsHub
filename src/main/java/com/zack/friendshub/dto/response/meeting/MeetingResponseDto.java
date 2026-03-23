package com.zack.friendshub.dto.response.meeting;

import com.zack.friendshub.enums.MeetingStatus;

import java.time.LocalDateTime;

public record MeetingResponseDto(
        Long id,
        String title,
        Long organizerId,
        String organizerName,
        Long participantId,
        String participantName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        MeetingStatus status
) {
}
