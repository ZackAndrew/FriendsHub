package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.AvailabilityResponseDto;
import com.zack.friendshub.model.Availability;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityMapper {
    public AvailabilityResponseDto toResponse(Availability availability) {
        return new AvailabilityResponseDto(
                availability.getId(),
                availability.getUser().getId(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }
}
