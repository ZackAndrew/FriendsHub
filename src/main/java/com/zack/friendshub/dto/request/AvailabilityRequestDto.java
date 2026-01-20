package com.zack.friendshub.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AvailabilityRequestDto(

        @NotNull(message = "Time can not be null")
        @Future(message = "Start time must be in future")
        LocalDateTime startTime,

        @NotNull(message = "Time can not be null")
        @Future(message = "End time must be in future")
        LocalDateTime endTime
) {
    public AvailabilityRequestDto{
        if(startTime.isAfter(endTime)){
            throw new IllegalArgumentException("Start time can not be after end time");
        }
    }
}
