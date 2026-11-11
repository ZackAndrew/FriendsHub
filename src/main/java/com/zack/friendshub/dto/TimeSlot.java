package com.zack.friendshub.dto;

import java.time.LocalDateTime;

public record TimeSlot(
        LocalDateTime start,
        LocalDateTime end
) {
}
