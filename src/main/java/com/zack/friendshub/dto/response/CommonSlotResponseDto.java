package com.zack.friendshub.dto.response;

import java.time.LocalDateTime;

public record CommonSlotResponseDto(
        LocalDateTime startTime,
        LocalDateTime endTime,
        String friendUsername
) {
}
