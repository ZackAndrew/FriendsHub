package com.zack.friendshub.dto.response.availability;

import java.time.LocalDateTime;
import java.util.List;

public record GroupedCommonSlotResponseDto(
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> friendUsernames
) {
}
