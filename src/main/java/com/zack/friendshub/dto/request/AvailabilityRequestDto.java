package com.zack.friendshub.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
public record AvailabilityRequestDto(

        List<String> participantNames,

        @NotNull
        @Future
        LocalDateTime windowStart,

        @NotNull
        @Future
        LocalDateTime windowEnd,

        List<Integer> preferredDaysOfWeek,
        LocalTime dailyStartTime,
        LocalTime dailyEndTime,
        int durationMinutes
) {
}