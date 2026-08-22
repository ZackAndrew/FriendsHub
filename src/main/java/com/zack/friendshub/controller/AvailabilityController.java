package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.availability.AvailabilityResponseDto;
import com.zack.friendshub.dto.response.availability.GroupedCommonSlotResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.AvailabilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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

    @GetMapping("/user/{friendUsername}")
    public ResponseEntity<List<AvailabilityResponseDto>> getUserAvailability(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @PathVariable String friendUsername,
            @AuthenticationPrincipal UserPrincipal currentUser

    ) {
        log.info("REST request to get availability for User username: {} by Requester ID: {}",
                friendUsername, currentUser.getId());

        List<AvailabilityResponseDto> result = availabilityService.getUserAvailability(from, to, friendUsername, currentUser);

        if (result.isEmpty()) {
            log.warn("No availability slots found for User username: {}", friendUsername);
        } else {
            log.info("Successfully found {} availability slots for User username: {}", result.size(), friendUsername);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/common")
    public ResponseEntity<List<GroupedCommonSlotResponseDto>> findCommonSlots(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        LocalDate startDate = (from != null) ? from : LocalDate.now();
        LocalDate endDate = (to != null) ? to : startDate.plusDays(7);

        if (ChronoUnit.DAYS.between(startDate, endDate) > 31) {
            throw new IllegalArgumentException("Search range cannot exceed 31 days");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<GroupedCommonSlotResponseDto> response = availabilityService.findCommonSlots(startDateTime, endDateTime, currentUser);

        return ResponseEntity.ok(response);
    }

}
