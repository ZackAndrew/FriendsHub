package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.availability.AvailabilityResponseDto;
import com.zack.friendshub.dto.response.availability.GroupedCommonSlotResponseDto;
import com.zack.friendshub.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilityService {
    AvailabilityResponseDto saveAvailability(AvailabilityRequestDto requestDto, UserPrincipal currentUser);

    List<AvailabilityResponseDto> getUserAvailability(LocalDateTime from, LocalDateTime to, String friendUsername, UserPrincipal currentUser);

    List<GroupedCommonSlotResponseDto> findCommonSlots(LocalDateTime from, LocalDateTime to, UserPrincipal currentUser);
}
