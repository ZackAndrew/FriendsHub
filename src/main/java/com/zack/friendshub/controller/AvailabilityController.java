package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.AvailabilityResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.AvailabilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Operations related with availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> addAvailability(
            @Valid @RequestBody AvailabilityRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("REST request to add availability for user ID: {}. Period: {} - {}",
                currentUser.getId(), requestDto.startTime(), requestDto.endTime());

        AvailabilityResponseDto result = availabilityService.saveAvailability(requestDto, currentUser);

        log.info("Successfully added availability with ID: {} for user: {}",
                result.id(), currentUser.getUsername());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AvailabilityResponseDto>> getUserAvailability(
            @PathVariable long userId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("REST request to get availability for User ID: {} by Requester ID: {}",
                userId, currentUser.getId());

        List<AvailabilityResponseDto> result = availabilityService.getUserAvailability(userId, currentUser);

        if (result.isEmpty()) {
            log.warn("No availability slots found for User ID: {}", userId);
        } else {
            log.info("Successfully found {} availability slots for User ID: {}", result.size(), userId);
        }

        return ResponseEntity.ok(result);
    }
}
