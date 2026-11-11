package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.ScoredTimeSlot;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.AvailabilityService;
import com.zack.friendshub.service.GoogleCalendarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Operations related with availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/calculate")
    public ResponseEntity<List<ScoredTimeSlot>> calculateCommonFreeTime(
            @Valid @RequestBody AvailabilityRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<ScoredTimeSlot> topOptions = availabilityService.calculateCommonFreeTime(requestDto, currentUser.getId());
        return ResponseEntity.ok(topOptions);
    }
}
