package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meeting")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Meeting", description = "Operations related with meetings")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @Operation(summary = "Create a new meeting request", description = "Sends a meeting request to another user based on available time slots.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meeting request successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or time slots overlap", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "Users are not friends", content = @Content)
    })
    public ResponseEntity<MeetingResponseDto> sendMeetingRequest(
            @Valid @RequestBody MeetingRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User ID {} is sending a meeting request to username: {}", currentUser.getId(), requestDto.participantUsername());
        MeetingResponseDto response = meetingService.sendMeetingRequest(requestDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Accept a meeting request", description = "Allows a participant to accept an incoming meeting request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting successfully accepted"),
            @ApiResponse(responseCode = "400", description = "Meeting is not in PENDING state", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not allowed to accept this meeting", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meeting not found", content = @Content)
    })
    public ResponseEntity<MeetingResponseDto> acceptMeetingRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User ID {} is attempting to accept meeting ID {}", currentUser.getId(), id);
        return ResponseEntity.ok(meetingService.acceptMeetingRequest(id, currentUser));
    }

    @PatchMapping("/{id}/decline")
    @Operation(summary = "Decline a meeting request", description = "Allows a participant to decline an incoming meeting request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting successfully declined"),
            @ApiResponse(responseCode = "400", description = "Meeting is not in PENDING state", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not allowed to decline this meeting", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meeting not found", content = @Content)
    })
    public ResponseEntity<MeetingResponseDto> declineMeetingRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User ID {} is attempting to decline meeting ID {}", currentUser.getId(), id);
        return ResponseEntity.ok(meetingService.declineMeetingRequest(id, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a meeting request", description = "Allows an organizer to cancel an existing meeting request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting successfully canceled"),
            @ApiResponse(responseCode = "400", description = "Meeting is already canceled or declined", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not the organizer of this meeting", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meeting not found", content = @Content)
    })
    public ResponseEntity<MeetingResponseDto> cancelMeetingRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User ID {} is attempting to cancel meeting ID {}", currentUser.getId(), id);
        return ResponseEntity.ok(meetingService.cancelMeetingRequest(id, currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meeting details", description = "Retrieves full details of a specific meeting by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not a participant or organizer", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meeting not found", content = @Content)
    })
    public ResponseEntity<MeetingResponseDto> getMeetingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.debug("User ID {} is fetching details for meeting ID {}", currentUser.getId(), id);
        return ResponseEntity.ok(meetingService.getMeetingById(id, currentUser));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending incoming meetings", description = "Retrieves a list of incoming meeting requests awaiting the user's decision.")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    public ResponseEntity<List<MeetingResponseDto>> getPendingMeetings(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.debug("User ID {} is fetching their pending incoming meetings", currentUser.getId());
        return ResponseEntity.ok(meetingService.getPendingMeetings(currentUser));
    }

    @GetMapping("/outgoing")
    @Operation(summary = "Get pending outgoing meetings", description = "Retrieves a list of meeting requests sent by the user that are still pending.")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    public ResponseEntity<List<MeetingResponseDto>> getOutgoingMeetings(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.debug("User ID {} is fetching their pending outgoing meetings", currentUser.getId());
        return ResponseEntity.ok(meetingService.getOutgoingMeetings(currentUser));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all user meetings", description = "Retrieves all meetings where the user is either an organizer or a participant.")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    public ResponseEntity<List<MeetingResponseDto>> getUserMeetings(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.debug("User ID {} is fetching all their meetings", currentUser.getId());
        return ResponseEntity.ok(meetingService.getUserMeetings(currentUser));
    }
}
