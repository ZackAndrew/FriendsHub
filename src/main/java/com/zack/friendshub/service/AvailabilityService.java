package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.AvailabilityResponseDto;
import com.zack.friendshub.security.UserPrincipal;

import java.util.List;


public interface AvailabilityService {
    AvailabilityResponseDto saveAvailability(AvailabilityRequestDto requestDto, UserPrincipal currentUser);

    List<AvailabilityResponseDto> getUserAvailability(Long userId, UserPrincipal currentUser);
}
