package com.zack.friendshub.dto.response.availability;

import java.time.LocalDateTime;

public record AvailabilityResponseDto(
        Long id,
        Long userId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
