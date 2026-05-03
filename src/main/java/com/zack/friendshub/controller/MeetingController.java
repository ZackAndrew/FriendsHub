package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meeting")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Meeting", description = "Operations related with meetings")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @Operation(summary = "Send meeting request")
    public ResponseEntity<MeetingResponseDto> sendMeetingRequest(
            @Valid @RequestBody MeetingRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User: {} is sending a meeting request to User: {}",
                currentUser.getUsername(), requestDto.participantUsername());

        MeetingResponseDto response = meetingService.sendMeetingRequest(requestDto, currentUser);

        log.info("Meeting request created successfully with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/accept/{meetingId}")
    @Operation(summary = "Accept meeting")
    public ResponseEntity<MeetingResponseDto> acceptMeetingRequest(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User: {} is attempting to accept meeting ID: {}",
                currentUser.getUsername(), meetingId);

        MeetingResponseDto response = meetingService.acceptMeetingRequest(meetingId, currentUser);

        log.info("Meeting ID: {} was successfully accepted by User: {}",
                meetingId, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

}
