package com.zack.friendshub.controller;

import com.zack.friendshub.dto.response.FriendshipRequestAcceptResponseDto;
import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.FriendshipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Friendship", description = "Operations related with friendship")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("request/{userId}")
    public ResponseEntity<FriendshipRequestResponseDto> sendFriendRequest(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal requester
    ) {
        log.info("REST request to send friend request to user ID: {}", userId);

        FriendshipRequestResponseDto response = friendshipService.sendFriendshipRequest(userId, requester);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("requests/{requestId}/accept")
    public ResponseEntity<FriendshipRequestAcceptResponseDto> acceptFriendshipRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal requester
    ) {
        log.info("User id={} is attempting to accept friendship request id={}",
                requester.getId(), requestId);

        FriendshipRequestAcceptResponseDto response = friendshipService.acceptFriendshipRequest(requestId, requester);

        log.info("Friendship request id={} successfully accepted by user id={}",
                requestId, requester.getId());

        return ResponseEntity.ok(response);
    }

}
