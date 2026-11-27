package com.zack.friendshub.dto.response;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        String message,
        Boolean isRead,
        Long relatedMeetingId,
        LocalDateTime createdAt
) {
}
