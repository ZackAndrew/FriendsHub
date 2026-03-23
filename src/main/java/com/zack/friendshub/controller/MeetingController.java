package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.MeetingService;
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

@RestController
@RequestMapping("/api/meeting")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Meeting", description = "Operations related with meetings")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<MeetingResponseDto> sendMeetingRequest(
            @Valid @RequestBody MeetingRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        MeetingResponseDto response = meetingService.sendMeetingRequest(requestDto, currentUser);

        return ResponseEntity.ok(response);
    }

}
