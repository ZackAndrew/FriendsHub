package com.zack.friendshub.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.List;

public record RoutineDto(
        @NotBlank(message = "Name cannot be empty")
        String name,

        @NotEmpty(message = "Days of week must be provided")
        List<@Min(1) @Max(7) Integer> daysOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime
) {
}
