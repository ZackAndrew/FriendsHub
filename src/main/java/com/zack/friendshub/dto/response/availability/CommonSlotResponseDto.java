package com.zack.friendshub.dto.response.availability;

import java.time.LocalDateTime;

public record CommonSlotResponseDto(
        LocalDateTime startTime,
        LocalDateTime endTime,
        String friendUsername
) {
}
