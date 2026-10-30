package com.zack.friendshub.dto.response.meeting;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingResponseDto(
        Long id,
        String title,
        Long organizerId,
        String organizerName,
        List<ParticipantDto> participants,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}