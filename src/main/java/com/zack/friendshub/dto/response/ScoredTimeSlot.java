package com.zack.friendshub.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ScoredTimeSlot(
        LocalDateTime start,
        LocalDateTime end,
        List<Long> availableUserIds,
        int score
) {
}