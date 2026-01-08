package com.zack.friendshub.controller;

import com.zack.friendshub.dto.response.FriendDto;
import com.zack.friendshub.dto.response.FriendshipRequestDecisionResponseDto;
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

import java.util.List;

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
    public ResponseEntity<FriendshipRequestDecisionResponseDto> acceptFriendshipRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User id={} is attempting to accept friendship request id={}",
                currentUser.getId(), requestId);

        FriendshipRequestDecisionResponseDto response = friendshipService.acceptFriendshipRequest(requestId, currentUser);

        log.info("Friendship request id={} successfully accepted by user id={}",
                requestId, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("requests/{requestId}/decline")
    public ResponseEntity<FriendshipRequestDecisionResponseDto> declineFriendshipRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("User id={} is attempting to decline friendship request id={}",
                currentUser.getId(), requestId);

        FriendshipRequestDecisionResponseDto response = friendshipService.declineFriendshipRequest(requestId, currentUser);

        log.info("Friendship request id={} successfully declined by user id={}",
                requestId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<FriendDto>> getAllFriends(@AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("User id={} is attempting to get friends",
                currentUser.getId());
        List<FriendDto> response = friendshipService.getAllFriends(currentUser);

        log.info("User id={} successfully get friends",
                currentUser.getId());
        return ResponseEntity.ok(response);
    }


}
