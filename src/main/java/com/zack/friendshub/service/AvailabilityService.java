package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.AvailabilityResponseDto;
import com.zack.friendshub.dto.response.CommonSlotResponseDto;
import com.zack.friendshub.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;


public interface AvailabilityService {
    AvailabilityResponseDto saveAvailability(AvailabilityRequestDto requestDto, UserPrincipal currentUser);

    List<AvailabilityResponseDto> getUserAvailability(Long userId, UserPrincipal currentUser);

    List<CommonSlotResponseDto> findCommonSlots(LocalDateTime from, LocalDateTime to, UserPrincipal currentUser);
}
