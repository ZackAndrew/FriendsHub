package com.zack.friendshub.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MeetingRequestDto(
        @NotBlank(message = "Participant ID is required")
        String participantUsername,

        @NotNull(message = "Start time is required")
        @Future(message = "Meeting has start in the future")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        @Future(message = "Meeting has start in the future")
        LocalDateTime endTime,

        @NotBlank(message = "Title cannot be empty")
        String title
) {
}
